CREATE database "todo-app";
--specify different password in runtime, when deploying or whatever!
CREATE USER "todo-app" WITH PASSWORD 'todo-password';
GRANT CONNECT ON DATABASE "todo-app" TO "todo-app";
GRANT ALL ON DATABASE "todo-app" TO "todo-app";

