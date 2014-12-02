package com.javaadvent.bootrest.todo;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Petri Kainulainen
 */
public class TodoTest {

    private String DESCRIPTION = "description";
    private String TITLE = "title";

    @Test
    public void build_TitleAndDescriptionSet_ShouldCreateNewTodoWithCorrectTitleAndDescription() {
        Todo build = Todo.getBuilder()
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        assertThat(build.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(build.getTitle()).isEqualTo(TITLE);
    }
}
