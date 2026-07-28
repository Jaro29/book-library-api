package pl.jaro.restapiworkshop.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.jaro.restapiworkshop.dto.BookCreateRequest;
import pl.jaro.restapiworkshop.dto.BookPatchRequest;
import pl.jaro.restapiworkshop.dto.BookResponse;
import pl.jaro.restapiworkshop.dto.PageResponse;
import pl.jaro.restapiworkshop.mapper.BookMapper;
import pl.jaro.restapiworkshop.model.Book;
import pl.jaro.restapiworkshop.service.BookService;

import java.util.List;

@RestController
@Validated
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
    public ResponseEntity<BookResponse> findBookById(@PathVariable Long id) {
        Book book = bookService.findBookById(id);
        BookResponse response = BookMapper.fromBook(book);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/books")
    public ResponseEntity<PageResponse<BookResponse>> findAllBooks(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                                   @RequestParam(defaultValue = "20") @Min(1) int pageSize) {
        PageResponse<Book> pageResponse = bookService.findAllBooks(page, pageSize);
        List<BookResponse> content = pageResponse.content().stream()
                .map(BookMapper::fromBook)
                .toList();
        PageResponse<BookResponse> response = new PageResponse<>(
                content,
                pageResponse.page(),
                pageResponse.pageSize(),
                pageResponse.totalElements(),
                pageResponse.totalPages()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/books/{id}")
    public ResponseEntity<BookResponse> patchBook(@PathVariable Long id, @RequestBody @Valid BookPatchRequest bookPatchRequest) {
        Book book = bookService.updateBook(id, bookPatchRequest);
        BookResponse response = BookMapper.fromBook(book);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id){
        bookService.deleteBook(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}