package com.igor101.thebesttodoapp.core;

import java.util.List;

public interface TodoRepository {

    List<Todo> todos(String nameFilter, String descriptionFilter);

    long create(TodoData todo);

    void update(long id, TodoData todo);

    void delete(long id);
}
