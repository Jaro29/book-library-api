package pl.jaro.restapiworkshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jaro.restapiworkshop.dto.BookCreateRequest;
import pl.jaro.restapiworkshop.dto.BookResponse;
import pl.jaro.restapiworkshop.mapper.BookMapper;
import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.service.BookService;


@RestController
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @PostMapping("/books")
    public ResponseEntity<BookResponse> createBook(@RequestBody @Valid BookCreateRequest createRequest,
                                                   @RequestParam(defaultValue = "false") boolean allowDuplicate) {

        Book book = bookService.createBook(createRequest, allowDuplicate);
        BookResponse response = BookMapper.fromBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<BookResponse> findBookById(@PathVariable Long id){
        Book book = bookService.findBookById(id);
        BookResponse response = BookMapper.fromBook(book);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}