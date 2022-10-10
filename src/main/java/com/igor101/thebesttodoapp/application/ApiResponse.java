package com.igor101.thebesttodoapp.application;

import java.util.List;

public record ApiResponse<T>(boolean success,
                             T data,
                             List<String> errors) {

    public static <T> ApiResponse<T> ofSuccess(T data) {
        return new ApiResponse<>(true, data, List.of());
    }

    public static ApiResponse<Empty> ofSuccess() {
        return ofSuccess(Empty.INSTANCE);
    }

    public static <T> ApiResponse<T> ofFailure(List<String> errors) {
        return new ApiResponse<>(false, null, errors);
    }

    public static <T> ApiResponse<T> ofFailure(String... errors) {
        return ofFailure(List.of(errors));
    }

    public record Empty() {
        static final Empty INSTANCE = new Empty();
    }
}
