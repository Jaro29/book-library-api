import { Component, inject, signal } from '@angular/core';
import { BookList } from './components/book-list/book-list';
import { RouterOutlet } from '@angular/router';
import { AddBookForm } from './components/add-book-form/add-book-form';
import { BookService } from './services/book';

@Component({
  selector: 'app-root',
  imports: [BookList, RouterOutlet, AddBookForm], // dodaj tutaj
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  showAddForm = signal(false);

  protected bookService = inject(BookService);

  onSearch(query: string) {
    this.bookService.searchQuery.set(query);
    this.bookService.loadBooks();
  }

  clearSearch() {
    this.bookService.searchQuery.set("");
    this.bookService.loadBooks();
  }

  toggleAddForm() {
    this.showAddForm.update((value) => !value);
  }
}
