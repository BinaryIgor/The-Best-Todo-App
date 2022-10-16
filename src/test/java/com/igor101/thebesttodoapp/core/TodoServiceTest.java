package com.igor101.thebesttodoapp.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TodoServiceTest {

    private TodoService service;
    private FakeTodoRepository todoRepository;

    @BeforeEach
    void setup() {
        todoRepository = new FakeTodoRepository();
        service = new TodoService(todoRepository);
    }

    @Test
    void todos_givenArguments_shouldDelegateCallToRepository() {
        var todos = List.of(new Todo(1, "some-name", "some-description"),
                new Todo(2, "some-name2"));

        todoRepository.setToReturnTodos(todos);

        var nameFilter = "some-filter1";
        var descriptionFilter = "some-filter2";

        Assertions.assertEquals(todos, service.todos(nameFilter, descriptionFilter));

        Assertions.assertEquals(nameFilter, todoRepository.capturedNameFilter());
        Assertions.assertEquals(descriptionFilter, todoRepository.capturedDescriptionFilter());
    }

    @ParameterizedTest
    @MethodSource("invalidTodosCases")
    void create_givenInvalidTodo_shouldThrowException(TodoData invalidTodo, TheBestTodoAppException exception) {
        var actualException = Assertions.assertThrows(TheBestTodoAppException.class,
                () -> service.create(invalidTodo));

        Assertions.assertEquals(exception.errors(), actualException.errors());
    }

    @ParameterizedTest
    @MethodSource("validTodos")
    void create_givenValidTodo_shouldCreateItAndReturnItsId(TodoData validTodo) {
        var nextTodoId = 11;

        todoRepository.setNextId(nextTodoId);

        Assertions.assertEquals(nextTodoId, service.create(validTodo));
        Assertions.assertEquals(validTodo, todoRepository.createdTodo());
    }

    @ParameterizedTest
    @MethodSource("invalidTodosCases")
    void update_givenInvalidTodo_shouldThrowException(TodoData invalidTodo, TheBestTodoAppException exception) {
        var todoId = 22;

        var actualException = Assertions.assertThrows(TheBestTodoAppException.class,
                () -> service.update(todoId, invalidTodo));

        Assertions.assertEquals(exception.errors(), actualException.errors());
    }

    @ParameterizedTest
    @MethodSource("validTodos")
    void update_givenValidTodo_shouldUpdateIt(TodoData validTodo) {
        var todoId = 33;

        service.update(todoId, validTodo);

        Assertions.assertEquals(todoId, todoRepository.updatedTodoId());
        Assertions.assertEquals(validTodo, todoRepository.updatedTodo());
    }

    @Test
    void delete_givenTodoId_shouldDelegateCallToRepository() {
        var todoId = 101;

        service.delete(todoId);

        Assertions.assertEquals(todoId, todoRepository.deletedTodoId());
    }

    static Stream<Arguments> invalidTodosCases() {
        var invalidNameException = new TheBestTodoAppException(Errors.INVALID_TODO_NAME);

        var tooLongName = Stream.generate(() -> "b")
                .limit(TodoService.MAX_NAME_LENGTH + 1)
                .collect(Collectors.joining());

        var tooLongDescription = Stream.generate(() -> "d")
                .limit(TodoService.MAX_DESCRIPTION_LENGTH + 1)
                .collect(Collectors.joining());

        return Stream.of(
                Arguments.of(new TodoData(null), invalidNameException),
                Arguments.of(new TodoData(" "), invalidNameException),
                Arguments.of(new TodoData("a", "some description"), invalidNameException),
                Arguments.of(new TodoData(tooLongName, "some description"), invalidNameException),
                Arguments.of(new TodoData("some name", tooLongDescription),
                        new TheBestTodoAppException(Errors.INVALID_TODO_DESCRIPTION)),
                Arguments.of(new TodoData(" ", tooLongDescription),
                        new TheBestTodoAppException(Errors.INVALID_TODO_NAME, Errors.INVALID_TODO_DESCRIPTION)));
    }

    static Stream<TodoData> validTodos() {
        return Stream.of(new TodoData("some todo"),
                new TodoData("a1", "Let's have some description"));
    }
}
