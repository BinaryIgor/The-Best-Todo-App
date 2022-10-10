package com.igor101.thebesttodoapp.application;

import com.igor101.thebesttodoapp.core.Todo;
import com.igor101.thebesttodoapp.core.TodoData;
import io.javalin.Javalin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class TodoController {

    static final String PATH = "/todos";
    private final List<Todo> inMemoryTodos = new ArrayList<>();
    private final AtomicLong nextTodoId = new AtomicLong(0);

    public void init(Javalin app) {
        app.get(PATH, ctx -> HttpFunctions.writeJsonResponse(ctx, ApiResponse.ofSuccess(inMemoryTodos), 200));

        app.post(PATH, ctx -> {
            var newTodo = HttpFunctions.jsonFromBody(ctx, TodoData.class);
            var newTodoId = nextTodoId.incrementAndGet();
            inMemoryTodos.add(new Todo(newTodoId, newTodo.name(), newTodo.description()));

            HttpFunctions.writeJsonResponse(ctx, ApiResponse.ofSuccess(newTodoId), 201);
        });

        app.put(PATH + "/{id}", ctx -> {
            var todoId = HttpFunctions.pathParam(ctx, "id", Long.class);
            var todoData = HttpFunctions.jsonFromBody(ctx, TodoData.class);

            var todoIdx = -1;

            for (int i = 0; i < inMemoryTodos.size(); i++) {
                if (inMemoryTodos.get(i).id() == todoId) {
                    todoIdx = i;
                    break;
                }
            }

            if (todoIdx >= 0) {
                inMemoryTodos.set(todoIdx, new Todo(todoId, todoData.name(), todoData.description()));
            }

            HttpFunctions.writeJsonResponse(ctx, ApiResponse.ofSuccess(), 200);
        });

        app.delete(PATH + "/{id}", ctx -> {
            var todoId = HttpFunctions.pathParam(ctx, "id", Long.class);

            var todoIdx = -1;

            for (int i = 0; i < inMemoryTodos.size(); i++) {
                if (inMemoryTodos.get(i).id() == todoId) {
                    todoIdx = i;
                    break;
                }
            }

            if (todoIdx >= 0) {
                inMemoryTodos.remove(todoIdx);
            }

            HttpFunctions.writeJsonResponse(ctx, ApiResponse.ofSuccess(), 200);
        });
    }
}
