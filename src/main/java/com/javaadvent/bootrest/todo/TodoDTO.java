package com.javaadvent.bootrest.todo;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Size;

/**
 * @author Petri Kainulainen
 */
public class TodoDTO {

    private String id;

    @Size(max = 500)
    private String description;

    @NotEmpty
    @Size(max = 100)
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
}
