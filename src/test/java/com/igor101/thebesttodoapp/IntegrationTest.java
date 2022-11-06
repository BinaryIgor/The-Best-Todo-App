package com.igor101.thebesttodoapp;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;

public abstract class IntegrationTest {

    private static final String POSTGRES_VERSION = "postgres:14.3";
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGRES_VERSION);
    protected static DSLContext CONTEXT;

    @BeforeAll
    static void allSetup() throws Exception {
        POSTGRES.start();
        CONTEXT = context();
        initSchema();
    }

    private static DSLContext context() throws Exception {
        var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword());

        return DSL.using(connection);
    }

    private static void initSchema() throws Exception {
        var cwd = Path.of("").toAbsolutePath();
        var schemaPath = Path.of(cwd.toString(), "db", "schema.sql");
        var schema = Files.readString(schemaPath);

        CONTEXT.execute(schema);
    }

    @AfterEach
    void tearDown() {
        afterEach();
        CONTEXT.truncate("todo")
                .execute();
    }

    protected void afterEach() {

    }

    @AfterAll
    static void allTearDown() {
        POSTGRES.stop();
    }
}
