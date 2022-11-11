package com.igor101.thebesttodoapp;

/*
We need to create The Best Todo App.

Endpoints to implement:
* GET /todos?nameFilter={}&descriptionFilter={}
    * without params (both are optional), returns all todos
    * with nameFilter returns all Todos that contain that phrase in name (case-insensitive)
    * with descriptionFilter returns all Todos that contain that phrase in description (case-insensitive)
* POST /todos - adds new TodoData returning id
* PUT /todos/{id} - updates TodoData returning Empty
* DELETE /todos/{id} - deletes Todo returning Empty

POST/PUT endpoints should validate TodoData:
* name can't be null and should have between 2 to 50 characters
* description is optional, but it can have max 1000 characters

ALL responses should have the following format:
{
    "success": boolean - whether request was successful or not,
    "data": <Object/Collection of objects for a given endpoint, null if success=false>,
    "errors": [String] - list of meaningful errors
}

Todos should be stored in the relational database.

We must also create a simple frontend that will show how the whole API works.
It needs to be available under /(root) path.
It doesn't need to be tested.

ALL code (excluding frontend) needs to be thoroughly tested including database and http layer!
 */

import com.igor101.thebesttodoapp.application.ApiErrors;
import com.igor101.thebesttodoapp.application.ApiResponse;
import com.igor101.thebesttodoapp.application.HttpFunctions;
import com.igor101.thebesttodoapp.application.TodoController;
import com.igor101.thebesttodoapp.core.TheBestTodoAppException;
import com.igor101.thebesttodoapp.core.TodoService;
import com.igor101.thebesttodoapp.infrastructure.SqlTodoRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheBestTodoApp {

    private static final Logger LOG = LoggerFactory.getLogger(TheBestTodoApp.class);
    private final TheBestTodoAppConfig config;
    private Javalin app;

    public TheBestTodoApp(TheBestTodoAppConfig config) {
        this.config = config;
    }

    public void start() {
        app = Javalin.create(c -> {
            if (config.staticFilesPath().isEmpty()) {
                c.staticFiles.add("public", Location.CLASSPATH);
            } else {
                c.staticFiles.add(config.staticFilesPath(), Location.EXTERNAL);
            }
        });

        app.exception(Exception.class, (exception, ctx) -> {
            if (exception instanceof TheBestTodoAppException appException) {
                LOG.warn("Handling AppException...", appException);
                HttpFunctions.writeJsonResponse(ctx, ApiResponse.ofFailure(appException.errors()), 400);
            } else {
                LOG.error("Handling unknown exception...", exception);
                HttpFunctions.writeJsonResponse(ctx, ApiResponse.ofFailure(ApiErrors.UNKNOWN_ERROR), 500);
            }
        });

        var todoRepository = new SqlTodoRepository(dslContext());
        var todoService = new TodoService(todoRepository);

        var todoController = new TodoController(todoService);
        todoController.init(app);

        app.start(config.httpPort());
    }

    private DSLContext dslContext() {
        var hikariConfig = new HikariConfig();
        hikariConfig.setUsername(config.dbUser());
        hikariConfig.setPassword(config.dbPassword());
        hikariConfig.setJdbcUrl(config.dbUrl());

        return DSL.using(new HikariDataSource(hikariConfig), SQLDialect.POSTGRES);
    }

    public void stop() {
        if (app != null) {
            app.stop();
        }
    }

    public static void main(String[] args) {
        var config = TheBestTodoAppConfig.fromEnvVariables();

        var app = new TheBestTodoApp(config);
        app.start();
    }
}
