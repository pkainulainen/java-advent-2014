package com.javaadvent.bootrest.todo;

import org.junit.Test;

import static com.javaadvent.bootrest.todo.TodoAssert.assertThatTodo;

/**
 * @author Petri Kainulainen
 */
public class TodoTest {

    private static final String DESCRIPTION = "description";
    private static final String TITLE = "title";

    private static final int MAX_LENGTH_DESCRIPTION = 500;
    private static final int MAX_LENGTH_TITLE = 100;

    private static final String UPDATED_DESCRIPTION = "updatedDescription";
    private static final String UPDATED_TITLE = "updatedTitle";

    @Test(expected = NullPointerException.class)
    public void build_TitleIsNull_ShouldThrowException() {
        Todo.getBuilder()
                .title(null)
                .description(DESCRIPTION)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void build_TitleIsEmpty_ShouldThrowException() {
        Todo.getBuilder()
                .title("")
                .description(DESCRIPTION)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void build_TitleIsTooLong_ShouldThrowException() {
        String tooLongTitle = StringTestUtil.createStringWithLength(MAX_LENGTH_TITLE + 1);
        Todo.getBuilder()
                .title(tooLongTitle)
                .description(DESCRIPTION)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void build_DescriptionIsTooLong_ShouldThrowException() {
        String tooLongDescription = StringTestUtil.createStringWithLength(MAX_LENGTH_DESCRIPTION + 1);
        Todo.getBuilder()
                .title(TITLE)
                .description(tooLongDescription)
                .build();
    }

    @Test
    public void build_WithoutDescription_ShouldCreateNewTodoEntryWithCorrectTitle() {
        Todo build = Todo.getBuilder()
                .title(TITLE)
                .build();

        assertThatTodo(build)
                .hasNoId()
                .hasTitle(TITLE)
                .hasNoDescription();
    }

    @Test
    public void build_WithTitleAndDescription_ShouldCreateNewTodoEntryWithCorrectTitleAndDescription() {
        Todo build = Todo.getBuilder()
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        assertThatTodo(build)
                .hasNoId()
                .hasTitle(TITLE)
                .hasDescription(DESCRIPTION);
    }

    @Test
    public void build_WithMaxLengthTitleAndDescription_ShouldCreateNewTodoEntryWithCorrectTitleAndDescription() {
        String maxLengthTitle = StringTestUtil.createStringWithLength(MAX_LENGTH_TITLE);
        String maxLengthDescription = StringTestUtil.createStringWithLength(MAX_LENGTH_DESCRIPTION);

        Todo build = Todo.getBuilder()
                .title(maxLengthTitle)
                .description(maxLengthDescription)
                .build();

        assertThatTodo(build)
                .hasNoId()
                .hasTitle(maxLengthTitle)
                .hasDescription(maxLengthDescription);
    }

    @Test(expected = NullPointerException.class)
    public void update_TitleIsNull_ShouldThrowException() {
        Todo updated = Todo.getBuilder()
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        updated.update(null, UPDATED_DESCRIPTION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_TitleIsEmpty_ShouldThrowException() {
        Todo updated = Todo.getBuilder()
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        updated.update("", UPDATED_DESCRIPTION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_TitleIsTooLong_ShouldThrowException() {
        Todo updated = Todo.getBuilder()
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        String tooLongTitle = StringTestUtil.createStringWithLength(MAX_LENGTH_TITLE + 1);
        updated.update(tooLongTitle, UPDATED_DESCRIPTION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_DescriptionIsTooLong_ShouldThrowException() {
        Todo updated = Todo.getBuilder()
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        String tooLongDescription = StringTestUtil.createStringWithLength(MAX_LENGTH_DESCRIPTION + 1);
        updated.update(UPDATED_TITLE, tooLongDescription);
    }

    @Test
    public void update_DescriptionIsNull_ShouldUpdateTitleAndDescription() {
        Todo updated = Todo.getBuilder()
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        updated.update(UPDATED_TITLE, null);

        assertThatTodo(updated)
                .hasTitle(UPDATED_TITLE)
                .hasNoDescription();
    }

    @Test
    public void update_MaxLengthTitleAndDescription_ShouldUpdateTitleAndDescription() {
        Todo updated = Todo.getBuilder()
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        String maxLengthTitle = StringTestUtil.createStringWithLength(MAX_LENGTH_TITLE);
        String maxLengthDescription = StringTestUtil.createStringWithLength(MAX_LENGTH_DESCRIPTION);

        updated.update(maxLengthTitle, maxLengthDescription);

        assertThatTodo(updated)
                .hasTitle(maxLengthTitle)
                .hasDescription(maxLengthDescription);
    }
}
