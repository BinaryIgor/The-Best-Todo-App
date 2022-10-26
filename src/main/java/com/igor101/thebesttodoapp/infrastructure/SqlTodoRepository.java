package com.igor101.thebesttodoapp.infrastructure;

import com.igor101.thebesttodoapp.core.Todo;
import com.igor101.thebesttodoapp.core.TodoData;
import com.igor101.thebesttodoapp.core.TodoRepository;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.List;

public class SqlTodoRepository implements TodoRepository {

    static final Table<?> TODO_TABLE = DSL.table("todo");
    static final Field<Long> ID_FIELD = DSL.field("id", long.class);
    static final Field<String> NAME_FIELD = DSL.field("name", String.class);
    static final Field<String> DESCRIPTION_FIELD = DSL.field("description", String.class);
    private final DSLContext context;

    public SqlTodoRepository(DSLContext context) {
        this.context = context;
    }

    @Override
    public List<Todo> todos(String nameFilter, String descriptionFilter) {
        return context.select(ID_FIELD, NAME_FIELD, DESCRIPTION_FIELD)
                .from(TODO_TABLE)
                .where(todosCondition(nameFilter, descriptionFilter))
                .fetch(r -> new Todo(r.get(ID_FIELD), r.get(NAME_FIELD), r.get(DESCRIPTION_FIELD)));
    }

    private Condition todosCondition(String nameFilter, String descriptionFilter) {
        var condition = DSL.noCondition();

        if (nameFilter != null && !nameFilter.isBlank()) {
            condition = likeCaseInsensitiveCondition(NAME_FIELD, nameFilter);
        }
        if (descriptionFilter != null && !descriptionFilter.isBlank()) {
            condition = condition.and(likeCaseInsensitiveCondition(DESCRIPTION_FIELD, descriptionFilter));
        }

        return condition;
    }

    private Condition likeCaseInsensitiveCondition(Field<String> field, String like) {
        return field.likeIgnoreCase("%" + like.toLowerCase() + '%');
    }

    @Override
    public long create(TodoData todo) {
        return context.insertInto(SqlTodoRepository.TODO_TABLE)
                .columns(NAME_FIELD, DESCRIPTION_FIELD)
                .values(todo.name(), todo.description())
                .returning(ID_FIELD)
                .fetchOptional(ID_FIELD)
                .orElseThrow();
    }

    @Override
    public void update(long id, TodoData todo) {
        context.update(TODO_TABLE)
                .set(NAME_FIELD, todo.name())
                .set(DESCRIPTION_FIELD, todo.description())
                .where(ID_FIELD.eq(id))
                .execute();
    }

    @Override
    public void delete(long id) {
        context.deleteFrom(TODO_TABLE)
                .where(ID_FIELD.eq(id))
                .execute();
    }
}
