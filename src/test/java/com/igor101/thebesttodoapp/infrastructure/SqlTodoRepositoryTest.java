package com.igor101.thebesttodoapp.infrastructure;

import com.igor101.thebesttodoapp.IntegrationTest;
import com.igor101.thebesttodoapp.core.Todo;
import com.igor101.thebesttodoapp.core.TodoData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

public class SqlTodoRepositoryTest extends IntegrationTest {

    private SqlTodoRepository repository;

    @BeforeEach
    void setup() {
        repository = new SqlTodoRepository(CONTEXT);
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
