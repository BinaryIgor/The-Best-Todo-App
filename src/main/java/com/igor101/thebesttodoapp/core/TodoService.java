package com.igor101.thebesttodoapp.core;

import java.util.ArrayList;
import java.util.List;

public class TodoService {

    static final int MIN_NAME_LENGTH = 2;
    static final int MAX_NAME_LENGTH = 50;
    static final int MAX_DESCRIPTION_LENGTH = 1000;
    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<Todo> todos(String nameFilter, String descriptionFilter) {
        return todoRepository.todos(nameFilter, descriptionFilter);
    }

    public long create(TodoData todo) {
        validateTodoData(todo);
        return todoRepository.create(todo);
    }

    private void validateTodoData(TodoData todo) {
        var errors = new ArrayList<String>();

        if (todo.name() == null
                || todo.name().strip().length() < MIN_NAME_LENGTH
                || todo.name().length() > MAX_NAME_LENGTH) {
            errors.add(Errors.INVALID_TODO_NAME);
        }

        if (todo.description() != null
                && todo.description().length() > MAX_DESCRIPTION_LENGTH) {
            errors.add(Errors.INVALID_TODO_DESCRIPTION);
        }

        if (!errors.isEmpty()) {
            throw new TheBestTodoAppException(errors);
        }
    }

    public void update(long id, TodoData todo) {
        validateTodoData(todo);
        todoRepository.update(id, todo);
    }

    public void delete(long id) {
        todoRepository.delete(id);
    }
}
