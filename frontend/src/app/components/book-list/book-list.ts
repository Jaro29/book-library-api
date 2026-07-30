import { Component, inject, OnInit, signal } from '@angular/core';
import { BookService } from '../../services/book';
import { Book } from '../../models/book';

@Component({
  selector: 'app-book-list',
  imports: [],
  templateUrl: './book-list.html',
  styleUrl: './book-list.css',
})
export class BookList implements OnInit{
  private bookService = inject(BookService);
  books= signal<Book[]>([]);

    ngOnInit() {
    this.loadBooks();
  }

  loadBooks() {
    this.bookService.getBooks().subscribe((response) => {
      this.books.set(response.content);
    });
  }
}
