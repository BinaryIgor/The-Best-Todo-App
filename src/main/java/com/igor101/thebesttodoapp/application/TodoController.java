package com.igor101.thebesttodoapp.application;

import com.igor101.thebesttodoapp.core.TodoData;
import com.igor101.thebesttodoapp.core.TodoService;
import io.javalin.Javalin;

public class TodoController {

    static final String PATH = "/todos";
    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    public void init(Javalin app) {
        //TODO: test filters!
        app.get(PATH, ctx -> {
            var nameFilter = HttpFunctions.queryParam(ctx, "nameFilter", String.class, null);
            var descriptionFilter = HttpFunctions.queryParam(ctx, "descriptionFilter", String.class, null);

            var todos = service.todos(nameFilter, descriptionFilter);
            HttpFunctions.writeJsonResponse(ctx, ApiResponse.ofSuccess(todos), 200);
        });

        app.post(PATH, ctx -> {
            var newTodo = HttpFunctions.jsonFromBody(ctx, TodoData.class);
            var newTodoId = service.create(newTodo);

            HttpFunctions.writeJsonResponse(ctx, ApiResponse.ofSuccess(newTodoId), 201);
        });

        app.put(PATH + "/{id}", ctx -> {
            var todoId = HttpFunctions.pathParam(ctx, "id", Long.class);
            var todoData = HttpFunctions.jsonFromBody(ctx, TodoData.class);

            service.update(todoId, todoData);

            HttpFunctions.writeJsonResponse(ctx, ApiResponse.ofSuccess(), 200);
        });

        app.delete(PATH + "/{id}", ctx -> {
            var todoId = HttpFunctions.pathParam(ctx, "id", Long.class);
            service.delete(todoId);

            HttpFunctions.writeJsonResponse(ctx, ApiResponse.ofSuccess(), 200);
        });
    }
}
