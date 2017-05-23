package com.javaadvent.bootrest.todo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.javaadvent.bootrest.todo.TodoAssert.assertThatTodo;
import static com.javaadvent.bootrest.todo.TodoDTOAssert.assertThatTodoDTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Petri Kainulainen
 */
@RunWith(MockitoJUnitRunner.class)
public class MongoDbTodoServiceTest {

    private static final String DESCRIPTION = "description";
    private static final String ID = "id";
    private static final String TITLE = "title";

    @Mock
    private TodoRepository repository;

    private MongoDBTodoService service;

    @Before
    public void setUp() {
        this.service = new MongoDBTodoService(repository);
    }

    @Test
    public void create_ShouldSaveNewTodoEntry() {
        TodoDTO newTodo = new TodoDTOBuilder()
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        when(repository.save(isA(Todo.class))).thenAnswer(invocation -> (Todo) invocation.getArguments()[0]);

        service.create(newTodo);

        ArgumentCaptor<Todo> savedTodoArgument = ArgumentCaptor.forClass(Todo.class);

        verify(repository, times(1)).save(savedTodoArgument.capture());
        verifyNoMoreInteractions(repository);

        Todo savedTodo = savedTodoArgument.getValue();
        assertThatTodo(savedTodo)
                .hasTitle(TITLE)
                .hasDescription(DESCRIPTION);
    }

    @Test
    public void create_ShouldReturnTheInformationOfCreatedTodoEntry() {
        TodoDTO newTodo = new TodoDTOBuilder()
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        when(repository.save(isA(Todo.class))).thenAnswer(invocation -> {
            Todo persisted = (Todo) invocation.getArguments()[0];
            ReflectionTestUtils.setField(persisted, "id", ID);
            return persisted;
        });

        TodoDTO returned = service.create(newTodo);

        assertThatTodoDTO(returned)
                .hasId(ID)
                .hasTitle(TITLE)
                .hasDescription(DESCRIPTION);
    }

    @Test(expected = TodoNotFoundException.class)
    public void delete_TodoEntryNotFound_ShouldThrowException() {
        when(repository.findOne(ID)).thenReturn(Optional.empty());

        service.findById(ID);
    }

    @Test
    public void delete_TodoEntryFound_ShouldDeleteTheFoundTodoEntry() {
        Todo deleted = new TodoBuilder()
                .id(ID)
                .build();

        when(repository.findOne(ID)).thenReturn(Optional.of(deleted));

        service.delete(ID);

        verify(repository, times(1)).delete(deleted);
    }

    @Test
    public void delete_TodoEntryFound_ShouldReturnTheDeletedTodoEntry() {
        Todo deleted = new TodoBuilder()
                .id(ID)
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        when(repository.findOne(ID)).thenReturn(Optional.of(deleted));

        TodoDTO returned = service.delete(ID);

        assertThatTodoDTO(returned)
                .hasId(ID)
                .hasTitle(TITLE)
                .hasDescription(DESCRIPTION);
    }

    @Test
    public void findAll_OneTodoEntryFound_ShouldReturnTheInformationOfFoundTodoEntry() {
        Todo expected = new TodoBuilder()
                .id(ID)
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        when(repository.findAll()).thenReturn(Arrays.asList(expected));

        List<TodoDTO> todoEntries = service.findAll();
        assertThat(todoEntries).hasSize(1);

        TodoDTO actual = todoEntries.iterator().next();
        assertThatTodoDTO(actual)
                .hasId(ID)
                .hasTitle(TITLE)
                .hasDescription(DESCRIPTION);
    }

    @Test(expected = TodoNotFoundException.class)
    public void findById_TodoEntryNotFound_ShouldThrowException() {
        when(repository.findOne(ID)).thenReturn(Optional.empty());

        service.findById(ID);
    }

    @Test
    public void findById_TodoEntryFound_ShouldReturnTheInformationOfFoundTodoEntry() {
        Todo found = new TodoBuilder()
                .id(ID)
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        when(repository.findOne(ID)).thenReturn(Optional.of(found));

        TodoDTO returned = service.findById(ID);

        assertThatTodoDTO(returned)
                .hasId(ID)
                .hasTitle(TITLE)
                .hasDescription(DESCRIPTION);
    }

    @Test(expected = TodoNotFoundException.class)
    public void update_UpdatedTodoEntryNotFound_ShouldThrowException() {
        when(repository.findOne(ID)).thenReturn(Optional.empty());

        TodoDTO updated = new TodoDTOBuilder()
                .id(ID)
                .build();

        service.update(updated);
    }

    @Test
    public void update_UpdatedTodoEntryFound_ShouldSaveUpdatedTodoEntry() {
        Todo existing = new TodoBuilder()
                .id(ID)
                .build();

        when(repository.findOne(ID)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        TodoDTO updated = new TodoDTOBuilder()
                .id(ID)
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        service.update(updated);

        verify(repository, times(1)).save(existing);
        assertThatTodo(existing)
                .hasId(ID)
                .hasTitle(TITLE)
                .hasDescription(DESCRIPTION);
    }

    @Test
    public void update_UpdatedTodoEntryFound_ShouldReturnTheInformationOfUpdatedTodoEntry() {
        Todo existing = new TodoBuilder()
                .id(ID)
                .build();

        when(repository.findOne(ID)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        TodoDTO updated = new TodoDTOBuilder()
                .id(ID)
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        TodoDTO returned = service.update(updated);
        assertThatTodoDTO(returned)
                .hasId(ID)
                .hasTitle(TITLE)
                .hasDescription(DESCRIPTION);
    }
}
