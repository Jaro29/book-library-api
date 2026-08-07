import { Component, inject, OnInit, signal } from '@angular/core';
import { BookService } from '../../services/book';
import { EditBookForm } from '../edit-book-form/edit-book-form';

@Component({
  selector: 'app-book-list',
  imports: [EditBookForm],
  templateUrl: './book-list.html',
  styleUrl: './book-list.css',
})
export class BookList implements OnInit {
  protected bookService = inject(BookService);
  protected books = this.bookService.books;
  protected currentPage = this.bookService.currentPage;
  protected totalPages = this.bookService.totalPages;

  ngOnInit() {
    this.bookService.loadBooks();
  }

  nextPage() {
    this.bookService.loadBooks(this.currentPage() + 1);
  }

  previousPage() {
    this.bookService.loadBooks(this.currentPage() - 1);
  }

  onDelete(id: number) {
    this.bookService.deleteBook(id).subscribe(() => {
      this.bookService.loadBooks(this.currentPage());
    });
  }

  onSearch(query: string) {
    this.bookService.searchQuery.set(query);
    this.bookService.loadBooks();
  }

  editingBookId = signal<number | null>(null);

  startEdit(id: number) {
    this.editingBookId.set(id);
  }

  cancelEdit() {
    this.editingBookId.set(null);
  }

}
