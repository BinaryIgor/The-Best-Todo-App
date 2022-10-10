package com.igor101.thebesttodoapp.core;

public record TodoData(String name, String description) {

    public TodoData(String name) {
        this(name, null);
    }
}
