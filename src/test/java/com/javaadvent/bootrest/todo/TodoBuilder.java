package com.javaadvent.bootrest.todo;

import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author Petri Kainulainen
 */
class TodoBuilder {

    private String description;
    private String id;
    private String title = "NOT_IMPORTANT";

    TodoBuilder() {

    }

    TodoBuilder description(String description) {
        this.description = description;
        return this;
    }

    TodoBuilder id(String id) {
        this.id = id;
        return this;
    }

    TodoBuilder title(String title) {
        this.title = title;
        return this;
    }

    Todo build() {
        Todo todo = Todo.getBuilder()
                .title(title)
                .description(description)
                .build();

        ReflectionTestUtils.setField(todo, "id", id);

        return todo;
    }
}
