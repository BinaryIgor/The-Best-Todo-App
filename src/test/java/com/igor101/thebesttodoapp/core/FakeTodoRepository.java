package com.igor101.thebesttodoapp.core;

import java.util.List;

public class FakeTodoRepository implements TodoRepository {

    private String capturedNameFilter;
    private String capturedDescriptionFilter;
    private List<Todo> toReturnTodos;
    private TodoData createdTodo;
    private long nextId;
    private long updatedTodoId;
    private TodoData updatedTodo;
    private long deletedTodoId;

    @Override
    public List<Todo> todos(String nameFilter, String descriptionFilter) {
        capturedNameFilter = nameFilter;
        capturedDescriptionFilter = descriptionFilter;
        return toReturnTodos;
    }

    @Override
    public long create(TodoData todo) {
        createdTodo = todo;
        return nextId;
    }

    @Override
    public void update(long id, TodoData todo) {
        updatedTodoId = id;
        updatedTodo = todo;
    }

    @Override
    public void delete(long id) {
        deletedTodoId = id;
    }

    public String capturedNameFilter() {
        return capturedNameFilter;
    }

    public String capturedDescriptionFilter() {
        return capturedDescriptionFilter;
    }

    public TodoData createdTodo() {
        return createdTodo;
    }

    public long updatedTodoId() {
        return updatedTodoId;
    }

    public TodoData updatedTodo() {
        return updatedTodo;
    }

    public long deletedTodoId() {
        return deletedTodoId;
    }

    public void setToReturnTodos(List<Todo> todos) {
        toReturnTodos = todos;
    }

    public void setNextId(long id) {
        nextId = id;
    }
}
