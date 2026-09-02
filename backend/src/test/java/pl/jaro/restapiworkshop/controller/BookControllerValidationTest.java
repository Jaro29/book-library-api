package pl.jaro.restapiworkshop.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.jaro.restapiworkshop.exception.GlobalExceptionHandler;
import pl.jaro.restapiworkshop.service.BnDataService;
import pl.jaro.restapiworkshop.service.BookService;
import pl.jaro.restapiworkshop.service.GoogleBooksService;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

class BookControllerValidationTest {

    private MockMvc mockMvc;
    private BookService bookService;
    private GoogleBooksService googleBooksService;
    private BnDataService bnDataService;


    @BeforeEach
    void setUp() {
        bookService = mock(BookService.class);
        googleBooksService = mock(GoogleBooksService.class);
        bnDataService = mock(BnDataService.class);


        StringHttpMessageConverter utf8StringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new BookController(bookService, googleBooksService, bnDataService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(utf8StringConverter, new JacksonJsonHttpMessageConverter())
                .build();
    }

    @Test
    void shouldReturn400WithMessageWhenTitleIsBlank() throws Exception {
        mockMvc.perform(patch("/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Tytuł nie może być pusty")));
    }

    @Test
    void shouldReturn400WithJoinedMessagesWhenMultipleFieldsInvalid() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"\", \"author\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Tytuł nie może być pusty")))
                .andExpect(content().string(containsString("Autor nie może być pusty")));
    }

    @Test
    void shouldReturn500WithGenericMessageOnUnexpectedException() throws Exception {
        when(bookService.findBookById(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("coś nieoczekiwanego, np. NPE w mapperze"));

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Wystąpił nieoczekiwany błąd."));
    }
}