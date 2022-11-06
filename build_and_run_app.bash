#!/bin/bash

if [ -z "$DB_PASSWORD" ]; then
  echo "DB_PASSWORD is required, but it's not defined!"
  exit 1
fi

mvn clean install

docker rm the-best-todo-app

docker build . -t the-best-todo-app

docker run --network host \
  -e "HTTP_PORT=8080" \
  -e "DB_USER=todo-app" \
  -e "DB_PASSWORD=$DB_PASSWORD" \
  -e "DB_URL=jdbc:postgresql://localhost:5555/todo-app" \
  --name the-best-todo-app the-best-todo-app