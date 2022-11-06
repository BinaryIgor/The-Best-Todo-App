package com.igor101.thebesttodoapp;

import com.igor101.thebesttodoapp.application.ApiErrors;
import com.igor101.thebesttodoapp.application.ApiResponse;
import com.igor101.thebesttodoapp.application.JsonMapper;
import com.igor101.thebesttodoapp.core.Todo;
import com.igor101.thebesttodoapp.core.TodoData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class TheBestTodoAppIntegrationTest extends IntegrationTest {

    private static final int PORT = 9090;
    private TheBestTodoApp app;
    private HttpClient httpClient;

    @BeforeEach
    void setup() {
        var config = new TheBestTodoAppConfig(PORT,
                POSTGRES.getUsername(),
                POSTGRES.getPassword(),
                POSTGRES.getJdbcUrl());

        app = new TheBestTodoApp(config);
        app.start();

        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3000))
                .build();
    }

    @Override
    protected void afterEach() {
        app.stop();
    }

    @Test
    void shouldCreateTodosAndGetThem() throws Exception {
        var firstTodo = new TodoData("first-todo");
        var secondTodo = new TodoData("second-todo", "some-description");

        var firstTodoCreateResponse = createTodo(firstTodo);
        var secondTodoCreateResponse = createTodo(secondTodo);

        Assertions.assertEquals(201, firstTodoCreateResponse.statusCode());
        Assertions.assertEquals(201, secondTodoCreateResponse.statusCode());

        var firstTodoId = todoIdFromCreateResponse(firstTodoCreateResponse);
        var secondTodoId = todoIdFromCreateResponse(secondTodoCreateResponse);

        var response = getTodos();

        Assertions.assertEquals(200, response.statusCode());
        assertTodosResponse(response,
                new Todo(firstTodoId, firstTodo.name(), firstTodo.description()),
                new Todo(secondTodoId, secondTodo.name(), secondTodo.description()));
    }

    private void assertTodosResponse(HttpResponse<String> response, Todo... todos) {
        var expectedBody = JsonMapper.toJson(ApiResponse.ofSuccess(List.of(todos)));
        Assertions.assertEquals(expectedBody, response.body());
    }

    @Test
    void shouldReturnMeaningfulExceptionGivenTodoWithInvalidSchemaWhileCreatingIt() throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(todosUri())
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                            "name": "ab",
                            "descriptionX": 45
                        }
                        """))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertInvalidRequestResponse(response, ApiErrors.INVALID_BODY);
    }

    private void assertInvalidRequestResponse(HttpResponse<String> response,
                                              String expectedError) {
        var expectedResponse = JsonMapper.toJson(ApiResponse.ofFailure(expectedError));

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertEquals(expectedResponse, response.body());
    }

    @Test
    void shouldUpdateExistingTodo() throws Exception {
        var todo = new TodoData("some-todo", "some-description");

        var createTodoResponse = createTodo(todo);
        var todoId = todoIdFromCreateResponse(createTodoResponse);

        var todoUpdate = new TodoData("updated-todo", "updated-description");

        var updateRequest = HttpRequest.newBuilder()
                .uri(todosUri("/" + todoId))
                .PUT(HttpRequest.BodyPublishers.ofString(JsonMapper.toJson(todoUpdate)))
                .build();

        var updateTodoResponse = httpClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, updateTodoResponse.statusCode());
        assertEmptyBodyResponse(updateTodoResponse);

        var getTodosResponse = getTodos();

        assertTodosResponse(getTodosResponse,
                new Todo(todoId, todoUpdate.name(), todoUpdate.description()));
    }

    private void assertEmptyBodyResponse(HttpResponse<String> response) {
        Assertions.assertEquals(JsonMapper.toJson(ApiResponse.ofSuccess()), response.body());
    }

    @Test
    void shouldReturnMeaningfulExceptionGivenInvalidTodoIdWhileUpdatingIt() throws Exception {
        var todoUpdate = new TodoData("todo");

        var request = HttpRequest.newBuilder()
                .uri(todosUri("/xD"))
                .PUT(HttpRequest.BodyPublishers.ofString(JsonMapper.toJson(todoUpdate)))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertInvalidRequestResponse(response, ApiErrors.INVALID_PATH_PARAM);
    }

    @Test
    void shouldDeleteExistingTodo() throws Exception {
        var todo = new TodoData("some-todo", "some-description");

        var createTodoResponse = createTodo(todo);
        var todoId = todoIdFromCreateResponse(createTodoResponse);

        var getTodosResponseBefore = getTodos();

        assertTodosResponse(getTodosResponseBefore,
                new Todo(todoId, todo.name(), todo.description()));

        var deleteTodoRequest = HttpRequest.newBuilder()
                .uri(todosUri("/" + todoId))
                .DELETE()
                .build();

        var deleteTodoResponse = httpClient.send(deleteTodoRequest, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, deleteTodoResponse.statusCode());
        assertEmptyBodyResponse(deleteTodoResponse);

        var getTodosResponseAfter = getTodos();
        assertTodosResponse(getTodosResponseAfter);
    }

    @Test
    void shouldReturnMeaningfulExceptionGivenInvalidTodoIdWhileDeletingIt() throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(todosUri("/xD2"))
                .DELETE()
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertInvalidRequestResponse(response, ApiErrors.INVALID_PATH_PARAM);
    }

    private HttpResponse<String> createTodo(TodoData todo) throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(todosUri())
                .POST(HttpRequest.BodyPublishers.ofString(JsonMapper.toJson(todo)))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getTodos() throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(todosUri())
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI todosUri() throws Exception {
        return todosUri("");
    }


    private URI todosUri(String toAppendPart) throws Exception {
        return new URI("http://localhost:%d/todos%s".formatted(PORT, toAppendPart));
    }

    /*
    {
        "success": true,
        "data": 1,
        "errors": []
    }
     */
    private long todoIdFromCreateResponse(HttpResponse<String> createResponse) {
        var apiResponse = JsonMapper.toObject(createResponse.body(), ApiResponse.class);
        return Long.parseLong(apiResponse.data().toString());
    }
}
