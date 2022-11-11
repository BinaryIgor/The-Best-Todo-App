const TODO_ID_ATTRIBUTE = "data-todo-id";
const todosDiv = document.getElementById("todos-div");
const newTodoNameInput = document.querySelector(".new-todo-name-input");
const newTodoDescriptionTextarea = document.querySelector(".new-todo-description-textarea");
const newTodoAddButton = document.querySelector(".new-todo-add-button");

getTodos();
newTodoAddButton.onclick = addNewTodo;

async function getTodos() {
    try {
        const response = await fetch("todos");
        const jsonResponse = await response.json();
        if (jsonResponse.success) {
            const todos = jsonResponse.data;
            if (todos.length > 0) {
                todos.forEach(t => renderTodo(t));
            } else {
                renderNoTodosHeader();
            }
        } else {
            alert(`Fail to fetch todos!: ${JSON.stringify(jsonResponse)}`);
        }
    } catch (e) {
        alert(`Fail to fetch todos!: ${e}`);
        console.log(e);
    }
}

function renderNoTodosHeader() {
    const noTodosHeader = document.createElement("h2");
    noTodosHeader.classList.add("no-todos-header");
    noTodosHeader.appendChild(document.createTextNode("Nothing TO DO, let's plan something!"));

    todosDiv.appendChild(noTodosHeader);
}

function renderTodo(todo) {
    removeNoTodosHeader();

    const todoDiv = document.createElement("div");
    todoDiv.setAttribute(TODO_ID_ATTRIBUTE, todo.id);
    todoDiv.classList.add("todo-div");

    const todoNameHeader = document.createElement("h2");
    todoNameHeader.classList.add("todo-name-header");
    todoNameHeader.appendChild(document.createTextNode(todo.name));

    todoDiv.appendChild(todoNameHeader);

    const todoXElement = document.createElement("span");
    todoXElement.classList.add("todo-x");
    todoXElement.innerHTML = "&#10005;";
    todoDiv.appendChild(todoXElement);

    if (todo.description) {
        const todoDescriptionParagraph = document.createElement("p");
        todoDescriptionParagraph.appendChild(document.createTextNode(todo.description));

        todoDiv.appendChild(todoDescriptionParagraph);
    }

    todosDiv.appendChild(todoDiv);

    todoXElement.onclick = () => deleteTodo(todo.id);
}

function removeNoTodosHeader() {
    const noTodosHeader = document.querySelector(".no-todos-header");
    if (noTodosHeader) {
        todosDiv.removeChild(noTodosHeader);
    }
}

function addNewTodo() {
    const name = newTodoNameInput.value;
    const description = newTodoDescriptionTextarea.value;
    if (name) {
        createTodo(name, description);
    } else {
        alert("Name is required!");
    }
}

async function createTodo(name, description) {
    try {
        const response = await fetch("todos", { method: "POST", body: JSON.stringify({ name, description }) });
        const jsonResponse = await response.json();
        if (jsonResponse.success) {
            const todoId = jsonResponse.data;
            renderTodo({ id: todoId, name, description });
            newTodoNameInput.value = "";
            newTodoDescriptionTextarea.value = "";
        } else {
            alert(`Fail to create todo!: ${JSON.stringify(jsonResponse)}`);
        }
    } catch (e) {
        alert(`Fail to create todo!: ${e}`);
        console.log(e);
    }
}

async function deleteTodo(todoId) {
    if (!confirm("Are you sure that you are done with that TODO?")) {
        return;
    }

    try {
        const response = await fetch(`todos/${todoId}`, { method: "DELETE" });
        const jsonResponse = await response.json();
        if (jsonResponse.success) {
            deleteTodoFromUI(todoId);
        } else {
            alert(`Fail to delete todo!: ${JSON.stringify(jsonResponse)}`);
        }
    } catch (e) {
        alert(`Fail to delete todo!: ${e}`);
        console.log(e);
    }
}

function deleteTodoFromUI(todoId) {
    const toDeleteTodo = document.querySelector(`[${TODO_ID_ATTRIBUTE}="${todoId}"]`);
    todosDiv.removeChild(toDeleteTodo);

    if (todosDiv.children.length == 0) {
        renderNoTodosHeader();
    }
}