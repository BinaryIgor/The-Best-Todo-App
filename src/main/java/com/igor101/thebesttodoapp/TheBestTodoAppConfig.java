package com.igor101.thebesttodoapp;

import java.util.Optional;

public record TheBestTodoAppConfig(int httpPort,
                                   String dbUser,
                                   String dbPassword,
                                   String dbUrl,
                                   String staticFilesPath) {

    public TheBestTodoAppConfig(int httpPort,
                                String dbUser,
                                String dbPassword,
                                String dbUrl) {
        this(httpPort, dbUser, dbPassword, dbUrl, "");
    }

    public static TheBestTodoAppConfig fromEnvVariables() {
        int httpPort;
        try {
            httpPort = Integer.parseInt(envVariableOrThrow("HTTP_PORT"));
        } catch (Exception e) {
            throw new RuntimeException("Invalid HTTP_PORT, integer is required", e);
        }

        var dbUser = envVariableOrThrow("DB_USER");
        var dbPassword = envVariableOrThrow("DB_PASSWORD");
        var dbUrl = envVariableOrThrow("DB_URL");
        var staticFilesPath = Optional.ofNullable(System.getenv("STATIC_FILES_PATH")).orElse("");

        return new TheBestTodoAppConfig(httpPort, dbUser, dbPassword, dbUrl, staticFilesPath);
    }

    private static String envVariableOrThrow(String key) {
        return Optional.ofNullable(System.getenv(key))
                .orElseThrow(() -> new RuntimeException(
                        "Env variable of %s key is required, but was null".formatted(key)));
    }
}
