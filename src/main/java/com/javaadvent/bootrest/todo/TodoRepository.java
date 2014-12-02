package com.javaadvent.bootrest.todo;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Petri Kainulainen
 */
interface TodoRepository extends MongoRepository<Todo, Long> {
}
