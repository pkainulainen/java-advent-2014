package com.javaadvent.bootrest.todo;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Size;

/**
 * This data transfer object contains the information of a single todo
 * entry and specifies validation rules that are used to ensure that only
 * valid information can be saved to the used database.
 * @author Petri Kainulainen
 */
public final class TodoDTO {

    private String id;

    @Size(max = Todo.MAX_LENGTH_DESCRIPTION)
    private String description;

    @NotEmpty
    @Size(max = Todo.MAX_LENGTH_TITLE)
    private String title;

    public TodoDTO() {

    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return String.format(
                "TodoDTO[id=%s, description=%s, title=%s]",
                this.id,
                this.description,
                this.title
        );
    }
}
