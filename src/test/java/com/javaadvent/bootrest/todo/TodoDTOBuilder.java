package com.javaadvent.bootrest.todo;

/**
 * @author Petri Kainulainen
 */
class TodoDTOBuilder {

    private String description;
    private String id;
    private String title;

    TodoDTOBuilder() {

    }

    TodoDTOBuilder description(String description) {
        this.description = description;
        return this;
    }

    TodoDTOBuilder id(String id) {
        this.id = id;
        return this;
    }

    TodoDTOBuilder title(String title) {
        this.title = title;
        return this;
    }

    TodoDTO build() {
        TodoDTO dto = new TodoDTO();

        dto.setDescription(description);
        dto.setId(id);
        dto.setTitle(title);

        return dto;
    }
}
