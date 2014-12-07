package com.javaadvent.bootrest.todo;

/**
 * This exception is thrown when the requested todo entry is not found.
 * @author Petri Kainulainen
 */
public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(String id) {
        super(String.format("No todo entry found with id: <%s>", id));
    }
}
