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
    this.deleteError.set(null);
    this.deletingId.set(id);
    this.bookService.deleteBook(id).subscribe({
      next: () => {
        this.deletingId.set(null);
        this.confirmingDeleteId.set(null);
        this.bookService.loadBooks(this.currentPage());
      },
      error: (err) => {
        this.deletingId.set(null);
        this.deleteError.set(
          typeof err.error === 'string' ? err.error : 'Nie udało się usunąć książki. Spróbuj ponownie.',
        );
      },
    });
  }

  deletingId = signal<number | null>(null);
  deleteError = signal<string | null>(null);

  confirmingDeleteId = signal<number | null>(null);

  askDeleteConfirmation(id: number){
    this.deleteError.set(null);
    this.confirmingDeleteId.set(id);
  }

  cancelDeleteConfirmation(){
    this.deleteError.set(null);
    this.confirmingDeleteId.set(null);
  }

  editingBookId = signal<number | null>(null);

  startEdit(id: number) {
    this.editingBookId.set(id);
  }

  cancelEdit() {
    this.editingBookId.set(null);
  }
}
