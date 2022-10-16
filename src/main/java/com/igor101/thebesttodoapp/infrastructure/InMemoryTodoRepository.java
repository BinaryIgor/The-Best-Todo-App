package com.igor101.thebesttodoapp.infrastructure;

import com.igor101.thebesttodoapp.core.Todo;
import com.igor101.thebesttodoapp.core.TodoData;
import com.igor101.thebesttodoapp.core.TodoRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryTodoRepository implements TodoRepository {

    private final Map<Long, Todo> todos = new LinkedHashMap<>();
    private final AtomicLong nextId = new AtomicLong(0);

    @Override
    public List<Todo> todos(String nameFilter, String descriptionFilter) {
        return new ArrayList<>(todos.values());
    }

    @Override
    public long create(TodoData todo) {
        var id = nextId.getAndIncrement();
        todos.put(id, todoFromData(id, todo));
        return id;
    }

    private Todo todoFromData(long id, TodoData todo) {
        return new Todo(id, todo.name(), todo.description());
    }

    @Override
    public void update(long id, TodoData todo) {
        todos.put(id, todoFromData(id, todo));
    }

    @Override
    public void delete(long id) {
        todos.remove(id);
    }
}
