package com.igor101.thebesttodoapp.infrastructure;

import com.igor101.thebesttodoapp.core.Todo;
import com.igor101.thebesttodoapp.core.TodoData;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.List;

public class SqlTodoRepositoryTest {

    private static final String POSTGRES_VERSION = "postgres:14.3";
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGRES_VERSION);
    private static DSLContext CONTEXT;

    private SqlTodoRepository repository;

    @BeforeAll
    static void allSetup() throws Exception {
        POSTGRES.start();
        CONTEXT = context();
        initSchema();
    }

    @BeforeEach
    void setup() {
        repository = new SqlTodoRepository(CONTEXT);
    }

    private static DSLContext context() throws Exception {
        var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword());

        return DSL.using(connection);
    }

    private static void initSchema() throws Exception {
        var cwd = Path.of("").toAbsolutePath();
        var schemaPath = Path.of(cwd.toString(), "db", "schema.sql");
        var schema = Files.readString(schemaPath);

        CONTEXT.execute(schema);
    }

    @AfterEach
    void tearDown() {
        CONTEXT.truncate(SqlTodoRepository.TODO_TABLE)
                .execute();
    }

    @AfterAll
    static void allTearDown() {
        POSTGRES.stop();
    }

    @Test
    void todos_withEmptyDb_shouldReturnEmpty() {
        Assertions.assertEquals(List.of(), repository.todos(null, null));
    }

    @ParameterizedTest
    @EnumSource(TodosTestCase.class)
    void todos_withFilters_shouldReturnFilteredTodos(TodosTestCase testCase) {
        var testCaseData = prepareTodosTestCase(testCase);

        Assertions.assertEquals(testCaseData.expectedTodos(),
                repository.todos(testCaseData.nameFilter(), testCaseData.descriptionFilter()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void create_givenTodo_shouldCreateItReturningId(boolean nullDescription) {
        var todo = new TodoData("todo1", nullDescription ? null : "description2");

        var todoId = repository.create(todo);

        var expectedTodo = toTodo(todoId, todo);

        Assertions.assertEquals(expectedTodo, todoById(todoId));
    }

    @Test
    void update_givenNonExistingTodo_shouldDoNothing() {
        Assertions.assertDoesNotThrow(() -> repository.update(2, new TodoData("some todo")));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void update_givenExistingTodo_shouldUpdateIt(boolean nullDescription) {
        var todoId = 11;
        var todo = new Todo(todoId, "some-next-todo", "description22");
        var anotherTodo = new Todo(todoId + 1, "another-todo");

        createTodo(todo);
        createTodo(anotherTodo);

        var updatedTodo = new TodoData(todo.name() + "_", nullDescription ? null : "some another description");

        repository.update(todoId, updatedTodo);

        var expectedTodo = toTodo(todoId, updatedTodo);

        Assertions.assertEquals(expectedTodo, todoById(todoId));
        Assertions.assertEquals(anotherTodo, todoById(anotherTodo.id()));
    }

    @Test
    void delete_givenNonExistingTodo_shouldDoNothing() {
        Assertions.assertDoesNotThrow(() -> repository.delete(22));
    }

    @Test
    void delete_givenExistingTodo_shouldDeleteIt() {
        var firstTodoId = 101;
        var secondTodoId = 202;
        var firstTodo = new Todo(firstTodoId, "some-next-todo");
        var secondTodo = new Todo(secondTodoId, "another-todo");

        createTodo(firstTodo);
        createTodo(secondTodo);

        repository.delete(firstTodoId);

        Assertions.assertNull(todoById(firstTodoId));
        Assertions.assertEquals(secondTodo, todoById(secondTodoId));
    }

    private TodosTestCaseData prepareTodosTestCase(TodosTestCase testCase) {
        return switch (testCase) {
            case NULL_FILTERS, EMPTY_FILTERS -> prepareNullOrEmptyTestCase(testCase);
            case ONLY_NAME_FILTER -> prepareOnlyNameFilterTestCase();
            case ONLY_DESCRIPTION_FILTER -> prepareOnlyDescriptionFilterTestCase();
            case NAME_AND_DESCRIPTION_FILTERS -> prepareNameAndDescriptionFiltersTestCase();
        };
    }

    private TodosTestCaseData prepareNullOrEmptyTestCase(TodosTestCase testCase) {
        var todos = List.of(new Todo(1, "some-todo"),
                new Todo(2, "some-todo2", "some-description"));

        createTodos(todos);

        var nameFilter = testCase == TodosTestCase.NULL_FILTERS ? null : "";
        var descriptionFilter = testCase == TodosTestCase.NULL_FILTERS ? null : "";

        return new TodosTestCaseData(nameFilter, descriptionFilter, todos);
    }

    private TodosTestCaseData prepareOnlyNameFilterTestCase() {
        var todos = List.of(new Todo(1, "Some-todo"),
                new Todo(2, "todo2-some", "some-description"),
                new Todo(3, "3todo", "3todo"));

        createTodos(todos);

        var nameFilter = "SOME";

        var expectedTodos = todos.subList(0, 2);

        return new TodosTestCaseData(nameFilter, null, expectedTodos);
    }

    private TodosTestCaseData prepareOnlyDescriptionFilterTestCase() {
        var todos = List.of(new Todo(1, "Some-todo"),
                new Todo(2, "todo2-some", "some-description"),
                new Todo(3, "3todo", "3todo desc"),
                new Todo(4, "desc in title", "some another data"));

        createTodos(todos);

        var descriptionFilter = "desc";

        var expectedTodos = todos.subList(1, 3);

        return new TodosTestCaseData(" ", descriptionFilter, expectedTodos);
    }

    private TodosTestCaseData prepareNameAndDescriptionFiltersTestCase() {
        var todos = List.of(new Todo(1, "Some-todo"),
                new Todo(2, "todo2-some", "some-description"),
                new Todo(3, "3 todo", "3todo desc"),
                new Todo(4, "desc in title", "some another description"));

        createTodos(todos);

        var nameFilter = "todo";
        var descriptionFilter = "desc";

        var expectedTodos = todos.subList(1, 3);

        return new TodosTestCaseData(nameFilter, descriptionFilter, expectedTodos);
    }

    private void createTodos(List<Todo> todos) {
        todos.forEach(this::createTodo);
    }

    private void createTodo(Todo todo) {
        CONTEXT.insertInto(SqlTodoRepository.TODO_TABLE)
                .columns(SqlTodoRepository.ID_FIELD,
                        SqlTodoRepository.NAME_FIELD,
                        SqlTodoRepository.DESCRIPTION_FIELD)
                .values(todo.id(), todo.name(), todo.description())
                .execute();
    }

    private Todo todoById(long id) {
        return CONTEXT.selectFrom(SqlTodoRepository.TODO_TABLE)
                .where(SqlTodoRepository.ID_FIELD.eq(id))
                .fetchOne(r ->
                        new Todo(r.get(SqlTodoRepository.ID_FIELD),
                                r.get(SqlTodoRepository.NAME_FIELD),
                                r.get(SqlTodoRepository.DESCRIPTION_FIELD)));
    }

    private Todo toTodo(long id, TodoData data) {
        return new Todo(id, data.name(), data.description());
    }

    enum TodosTestCase {
        NULL_FILTERS, EMPTY_FILTERS,
        ONLY_NAME_FILTER,
        ONLY_DESCRIPTION_FILTER,
        NAME_AND_DESCRIPTION_FILTERS
    }

    record TodosTestCaseData(String nameFilter,
                             String descriptionFilter,
                             List<Todo> expectedTodos) {
    }
}
