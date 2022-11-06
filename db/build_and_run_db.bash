#!/bin/bash

docker rm the-best-todo-app-db

docker build . -t the-best-todo-app-db

docker run -p "5555:5432" \
  -v "$HOME/the-best-todo-app-db-volume:/var/lib/postgresql/data" \
  --name the-best-todo-app-db the-best-todo-app-db