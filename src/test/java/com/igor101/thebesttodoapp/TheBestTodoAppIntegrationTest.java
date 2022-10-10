package com.igor101.thebesttodoapp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class TheBestTodoAppIntegrationTest {

    private static final int PORT = 9090;
    private TheBestTodoApp app;

    @BeforeEach
    void setup() {
        app = new TheBestTodoApp();
        app.start(PORT);
    }

    @AfterEach
    void tearDown() {
        app.stop();
    }
}
