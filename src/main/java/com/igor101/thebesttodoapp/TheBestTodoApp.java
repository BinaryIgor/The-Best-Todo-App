package com.igor101.thebesttodoapp;

/*
We need to create The Best Todo App.

Endpoints to implement:
* GET /todos?nameFilter={}&descriptionFilter={}
    * without params, return all todos
    * with nameFilter returns all Todos that contains that phrase in name (case-insensitive)
    * with descriptionFilter returns all Todos that contains that phrase in description (case-insensitive)
* POST /todos - adds new TodoData returning id
* PUT /todos/{id} - updates TodoData returning empty
* DELETE /todos/{id} - deletes Todo returning empty

POST/PUT endpoint should validate TodoData:
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

ALL code (excluding frontend) needs to be thoroughly tested, including database and http layer!
 */


public class TheBestTodoApp {


    public void start(int port) {

    }

    public void stop() {

    }

    public static void main(String[] args) {
        var app = new TheBestTodoApp();
        app.start(8080);
    }
}
