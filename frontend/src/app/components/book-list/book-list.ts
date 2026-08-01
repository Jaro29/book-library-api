import { Component, inject, OnInit, signal } from '@angular/core';
import { BookService } from '../../services/book';
import { Book } from '../../models/book';

@Component({
  selector: 'app-book-list',
  imports: [],
  templateUrl: './book-list.html',
  styleUrl: './book-list.css',
})
export class BookList implements OnInit {
  protected bookService = inject(BookService);
  protected books = this.bookService.books;

  ngOnInit() {
    this.bookService.loadBooks();
  }
}
