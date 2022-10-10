package com.igor101.thebesttodoapp.core;

import java.util.List;

public class TheBestTodoAppException extends RuntimeException {

    private final List<String> errors;

    public TheBestTodoAppException(List<String> errors) {
        this.errors = errors;
    }

    public TheBestTodoAppException(String... errors) {
        this(List.of(errors));
    }

    public List<String> errors() {
        return errors;
    }
}
