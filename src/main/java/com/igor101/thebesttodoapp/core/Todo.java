package com.igor101.thebesttodoapp.core;

public record Todo(long id, String name, String description) {

    public Todo(long id, String name) {
        this(id, name, null);
    }
}
