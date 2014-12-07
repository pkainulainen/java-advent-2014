package com.javaadvent.bootrest.todo;

import com.javaadvent.bootrest.error.RestErrorHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;

import static com.javaadvent.bootrest.todo.TodoDTOAssert.assertThatTodoDTO;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Petri Kainulainen
 */
@RunWith(MockitoJUnitRunner.class)
public class TodoControllerTest {

    private static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8")
    );

    private static final String DESCRIPTION = "description";
    private static final String ID = "id";
    private static final String TITLE = "title";

    private static final int MAX_LENGTH_DESCRIPTION = 500;
    private static final int MAX_LENGTH_TITLE = 100;

    @Mock
    private TodoService service;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TodoController(service))
                .setHandlerExceptionResolvers(withExceptionControllerAdvice())
                .build();
    }

    /**
     * For some reason this does not work. The correct error handler method is invoked but when it tries
     * to return the validation errors as json, the following error appears to log:
     *
     * Failed to invoke @ExceptionHandler method:
     * public com.javaadvent.bootrest.error.ValidationErrorDTO com.javaadvent.bootrest.error.RestErrorHandler.processValidationError(org.springframework.web.bind.MethodArgumentNotValidException)
     * org.springframework.web.HttpMediaTypeNotAcceptableException: Could not find acceptable representation
     *
     * I have to figure out how to fix this before I can write the unit tests that ensure that validation is working.
     */
    private ExceptionHandlerExceptionResolver withExceptionControllerAdvice() {
        final ExceptionHandlerExceptionResolver exceptionResolver = new ExceptionHandlerExceptionResolver() {
            @Override
            protected ServletInvocableHandlerMethod getExceptionHandlerMethod(final HandlerMethod handlerMethod,
                                                                              final Exception exception) {
                Method method = new ExceptionHandlerMethodResolver(RestErrorHandler.class).resolveMethod(exception);
                if (method != null) {
                    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
                    messageSource.setBasename("messages");
                    return new ServletInvocableHandlerMethod(new RestErrorHandler(messageSource), method);
                }
                return super.getExceptionHandlerMethod(handlerMethod, exception);
            }
        };
        exceptionResolver.afterPropertiesSet();
        return exceptionResolver;
    }

    @Test
    public void create_TodoEntryWithOnlyTitle_ShouldCreateNewTodoEntryWithoutDescription() throws Exception {
        TodoDTO newTodoEntry = new TodoDTOBuilder()
                .title(TITLE)
                .build();

        mockMvc.perform(post("/api/todo")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(WebTestUtil.convertObjectToJsonBytes(newTodoEntry))
        );

        ArgumentCaptor<TodoDTO> createdArgument = ArgumentCaptor.forClass(TodoDTO.class);
        verify(service, times(1)).create(createdArgument.capture());
        verifyNoMoreInteractions(service);

        TodoDTO created = createdArgument.getValue();
        assertThatTodoDTO(created)
                .hasNoId()
                .hasTitle(TITLE)
                .hasNoDescription();
    }

    @Test
    public void create_TodoEntryWithOnlyTitle_ShouldReturnResponseStatusCreated() throws Exception {
        TodoDTO newTodoEntry = new TodoDTOBuilder()
                .title(TITLE)
                .build();

        when(service.create(isA(TodoDTO.class))).then(invocationOnMock -> {
            TodoDTO saved = (TodoDTO) invocationOnMock.getArguments()[0];
            saved.setId(ID);
            return saved;
        });

        mockMvc.perform(post("/api/todo")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(WebTestUtil.convertObjectToJsonBytes(newTodoEntry))
        )
                .andExpect(status().isCreated());
    }

    @Test
    public void create_TodoEntryWithOnlyTitle_ShouldReturnTheInformationOfCreatedTodoEntryAsJSon() throws Exception {
        TodoDTO newTodoEntry = new TodoDTOBuilder()
                .title(TITLE)
                .build();

        when(service.create(isA(TodoDTO.class))).then(invocationOnMock -> {
            TodoDTO saved = (TodoDTO) invocationOnMock.getArguments()[0];
            saved.setId(ID);
            return saved;
        });

        mockMvc.perform(post("/api/todo")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(WebTestUtil.convertObjectToJsonBytes(newTodoEntry))
        )
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(ID)))
                .andExpect(jsonPath("$.title", is(TITLE)))
                .andExpect(jsonPath("$.description", isEmptyOrNullString()));
    }

    @Test
    public void create_TodoEntryWithMaxLengthTitleAndDescription_ShouldCreateNewTodoEntryWithCorrectInformation() throws Exception {
        String maxLengthTitle = StringTestUtil.createStringWithLength(MAX_LENGTH_TITLE);
        String maxLengthDescription = StringTestUtil.createStringWithLength(MAX_LENGTH_DESCRIPTION);

        TodoDTO newTodoEntry = new TodoDTOBuilder()
                .title(maxLengthTitle)
                .description(maxLengthDescription)
                .build();

        mockMvc.perform(post("/api/todo")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(WebTestUtil.convertObjectToJsonBytes(newTodoEntry))
        );

        ArgumentCaptor<TodoDTO> createdArgument = ArgumentCaptor.forClass(TodoDTO.class);
        verify(service, times(1)).create(createdArgument.capture());
        verifyNoMoreInteractions(service);

        TodoDTO created = createdArgument.getValue();
        assertThatTodoDTO(created)
                .hasNoId()
                .hasTitle(maxLengthTitle)
                .hasDescription(maxLengthDescription);
    }

    @Test
    public void create_TodoEntryWithMaxLengthTitleAndDescription_ShouldReturnResponseStatusCreated() throws Exception {
        String maxLengthTitle = StringTestUtil.createStringWithLength(MAX_LENGTH_TITLE);
        String maxLengthDescription = StringTestUtil.createStringWithLength(MAX_LENGTH_DESCRIPTION);

        TodoDTO newTodoEntry = new TodoDTOBuilder()
                .title(maxLengthTitle)
                .description(maxLengthDescription)
                .build();

        when(service.create(isA(TodoDTO.class))).then(invocationOnMock -> {
            TodoDTO saved = (TodoDTO) invocationOnMock.getArguments()[0];
            saved.setId(ID);
            return saved;
        });

        mockMvc.perform(post("/api/todo")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(WebTestUtil.convertObjectToJsonBytes(newTodoEntry))
        )
                .andExpect(status().isCreated());
    }

    @Test
    public void create_TodoEntryWithMaxLengthTitleAndDescription_ShouldReturnTheInformationOfCreatedTodoEntryAsJson() throws Exception {
        String maxLengthTitle = StringTestUtil.createStringWithLength(MAX_LENGTH_TITLE);
        String maxLengthDescription = StringTestUtil.createStringWithLength(MAX_LENGTH_DESCRIPTION);

        TodoDTO newTodoEntry = new TodoDTOBuilder()
                .title(maxLengthTitle)
                .description(maxLengthDescription)
                .build();

        when(service.create(isA(TodoDTO.class))).then(invocationOnMock -> {
            TodoDTO saved = (TodoDTO) invocationOnMock.getArguments()[0];
            saved.setId(ID);
            return saved;
        });

        mockMvc.perform(post("/api/todo")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(WebTestUtil.convertObjectToJsonBytes(newTodoEntry))
        )
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(ID)))
                .andExpect(jsonPath("$.title", is(maxLengthTitle)))
                .andExpect(jsonPath("$.description", is(maxLengthDescription)));
    }

    @Test
    public void delete_TodoEntryNotFound_ShouldReturnResponseStatusNotFound() throws Exception {
        when(service.delete(ID)).thenThrow(new TodoNotFoundException(ID));

        mockMvc.perform(delete("/api/todo/{id}", ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void delete_TodoEntryFound_ShouldReturnResponseStatusOk() throws Exception {
        TodoDTO deleted = new TodoDTOBuilder()
                .id(ID)
                .build();

        when(service.delete(ID)).thenReturn(deleted);

        mockMvc.perform(delete("/api/todo/{id}", ID))
                .andExpect(status().isOk());
    }

    @Test
    public void delete_TodoEntryFound_ShouldTheInformationOfDeletedTodoEntryAsJson() throws Exception {
        TodoDTO deleted = new TodoDTOBuilder()
                .id(ID)
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        when(service.delete(ID)).thenReturn(deleted);

        mockMvc.perform(delete("/api/todo/{id}", ID))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(ID)))
                .andExpect(jsonPath("$.title", is(TITLE)))
                .andExpect(jsonPath("$.description", is(DESCRIPTION)));
    }

    @Test
    public void findAll_ShouldReturnResponseStatusOk() throws Exception {
        mockMvc.perform(get("/api/todo"))
                .andExpect(status().isOk());
    }

    @Test
    public void findAll_OneTodoEntryFound_ShouldReturnListThatContainsOneTodoEntryAsJson() throws Exception {
        TodoDTO found = new TodoDTOBuilder()
                .id(ID)
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        when(service.findAll()).thenReturn(Arrays.asList(found));

        mockMvc.perform(get("/api/todo"))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(ID)))
                .andExpect(jsonPath("$[0].title", is(TITLE)))
                .andExpect(jsonPath("$[0].description", is(DESCRIPTION)));
    }

    @Test
    public void findById_TodoEntryFound_ShouldReturnResponseStatusOk() throws Exception {
        TodoDTO found = new TodoDTOBuilder().build();

        when(service.findById(ID)).thenReturn(found);

        mockMvc.perform(get("/api/todo/{id}", ID))
                .andExpect(status().isOk());
    }

    @Test
    public void findById_TodoEntryFound_ShouldTheInformationOfFoundTodoEntryAsJson() throws Exception {
        TodoDTO found = new TodoDTOBuilder()
                .id(ID)
                .title(TITLE)
                .description(DESCRIPTION)
                .build();

        when(service.findById(ID)).thenReturn(found);

        mockMvc.perform(get("/api/todo/{id}", ID))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(ID)))
                .andExpect(jsonPath("$.title", is(TITLE)))
                .andExpect(jsonPath("$.description", is(DESCRIPTION)));
    }

    @Test
    public void findById_TodoEntryNotFound_ShouldReturnResponseStatusNotFound() throws Exception {
        when(service.findById(ID)).thenThrow(new TodoNotFoundException(ID));

        mockMvc.perform(get("/api/todo/{id}", ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void update_TodoEntryWithOnlyTitle_ShouldUpdateTheInformationOfTodoEntry() throws Exception {
        TodoDTO updatedTodoEntry = new TodoDTOBuilder()
                .id(ID)
                .title(TITLE)
                .build();

        mockMvc.perform(put("/api/todo/{id}", ID)
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(WebTestUtil.convertObjectToJsonBytes(updatedTodoEntry))
        );

        ArgumentCaptor<TodoDTO> updatedArgument = ArgumentCaptor.forClass(TodoDTO.class);
        verify(service, times(1)).update(updatedArgument.capture());
        verifyNoMoreInteractions(service);

        TodoDTO updated = updatedArgument.getValue();
        assertThatTodoDTO(updated)
                .hasId(ID)
                .hasTitle(TITLE)
                .hasNoDescription();
    }

    @Test
    public void update_TodoEntryWithOnlyTitle_ShouldReturnResponseStatusOk() throws Exception {
        TodoDTO updatedTodoEntry = new TodoDTOBuilder()
                .id(ID)
                .title(TITLE)
                .build();

        when(service.update(isA(TodoDTO.class))).then(invocationOnMock -> (TodoDTO) invocationOnMock.getArguments()[0]);

        mockMvc.perform(put("/api/todo/{id}", ID)
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(WebTestUtil.convertObjectToJsonBytes(updatedTodoEntry))
        )
                .andExpect(status().isOk());
    }

    @Test
    public void update_TodoEntryWithOnlyTitle_ShouldReturnTheInformationOfUpdatedTodoEntryAsJSon() throws Exception {
        TodoDTO updatedTodoEntry = new TodoDTOBuilder()
                .id(ID)
                .title(TITLE)
                .build();

        when(service.update(isA(TodoDTO.class))).then(invocationOnMock ->  (TodoDTO) invocationOnMock.getArguments()[0]);

        mockMvc.perform(put("/api/todo/{id}", ID)
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(WebTestUtil.convertObjectToJsonBytes(updatedTodoEntry))
        )
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(ID)))
                .andExpect(jsonPath("$.title", is(TITLE)))
                .andExpect(jsonPath("$.description", isEmptyOrNullString()));
    }

    @Test
    public void update_TodoEntryWithMaxLengthTitleAndDescription_ShouldUpdateTheInformationOfTodoEntry() throws Exception {
        String maxLengthTitle = StringTestUtil.createStringWithLength(MAX_LENGTH_TITLE);
        String maxLengthDescription = StringTestUtil.createStringWithLength(MAX_LENGTH_DESCRIPTION);

        TodoDTO updatedTodoEntry = new TodoDTOBuilder()
                .id(ID)
                .title(maxLengthTitle)
                .description(maxLengthDescription)
                .build();

        mockMvc.perform(put("/api/todo/{id}", ID)
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(WebTestUtil.convertObjectToJsonBytes(updatedTodoEntry))
        );

        ArgumentCaptor<TodoDTO> updatedArgument = ArgumentCaptor.forClass(TodoDTO.class);
        verify(service, times(1)).update(updatedArgument.capture());
        verifyNoMoreInteractions(service);

        TodoDTO updated = updatedArgument.getValue();
        assertThatTodoDTO(updated)
                .hasId(ID)
                .hasTitle(maxLengthTitle)
                .hasDescription(maxLengthDescription);
    }

    @Test
    public void update_TodoEntryWithMaxLengthTitleAndDescription_ShouldReturnResponseStatusOk() throws Exception {
        String maxLengthTitle = StringTestUtil.createStringWithLength(MAX_LENGTH_TITLE);
        String maxLengthDescription = StringTestUtil.createStringWithLength(MAX_LENGTH_DESCRIPTION);

        TodoDTO updatedTodoEntry = new TodoDTOBuilder()
                .id(ID)
                .title(maxLengthTitle)
                .description(maxLengthDescription)
                .build();

        when(service.create(isA(TodoDTO.class))).then(invocationOnMock -> (TodoDTO) invocationOnMock.getArguments()[0]);

        mockMvc.perform(put("/api/todo/{id}", ID)
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(WebTestUtil.convertObjectToJsonBytes(updatedTodoEntry))
        )
                .andExpect(status().isOk());
    }

    @Test
    public void update_TodoEntryWithMaxLengthTitleAndDescription_ShouldReturnTheInformationOfCreatedUpdatedTodoEntryAsJson() throws Exception {
        String maxLengthTitle = StringTestUtil.createStringWithLength(MAX_LENGTH_TITLE);
        String maxLengthDescription = StringTestUtil.createStringWithLength(MAX_LENGTH_DESCRIPTION);

        TodoDTO updatedTodoEntry = new TodoDTOBuilder()
                .id(ID)
                .title(maxLengthTitle)
                .description(maxLengthDescription)
                .build();

        when(service.update(isA(TodoDTO.class))).then(invocationOnMock -> (TodoDTO) invocationOnMock.getArguments()[0]);

        mockMvc.perform(put("/api/todo/{id}", ID)
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(WebTestUtil.convertObjectToJsonBytes(updatedTodoEntry))
        )
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(ID)))
                .andExpect(jsonPath("$.title", is(maxLengthTitle)))
                .andExpect(jsonPath("$.description", is(maxLengthDescription)));
    }
}
