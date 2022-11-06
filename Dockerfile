FROM eclipse-temurin:17

COPY target/the-best-todo-app-jar-with-dependencies.jar todo-app.jar

ENTRYPOINT ["java", "-jar", "todo-app.jar"]