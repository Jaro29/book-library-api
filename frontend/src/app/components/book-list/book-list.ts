import { Component, inject, OnInit, signal } from '@angular/core';
import { BookService, BookSort, BookStatusFilter, BookView } from '../../services/book';
import { EditBookForm } from '../edit-book-form/edit-book-form';
import { BookDetails } from '../book-details/book-details';
import { Book } from '../../models/book';

@Component({
  selector: 'app-book-list',
  imports: [EditBookForm, BookDetails],
  templateUrl: './book-list.html',
  styleUrl: './book-list.css',
})
export class BookList implements OnInit {
  protected bookService = inject(BookService);
  protected books = this.bookService.books;
  protected currentPage = this.bookService.currentPage;
  protected totalPages = this.bookService.totalPages;
  protected viewMode = this.bookService.viewMode;
  protected statusFilter = this.bookService.statusFilter;
  protected sort = this.bookService.sort;

  protected readonly statusTabs: { value: BookStatusFilter; label: string }[] = [
    { value: null, label: 'Wszystkie' },
    { value: 'TO_READ', label: 'Do przeczytania' },
    { value: 'READING', label: 'W trakcie' },
    { value: 'FINISHED', label: 'Przeczytane' },
  ];

  protected readonly sortOptions: { value: BookSort; label: string }[] = [
    { value: 'ADDED', label: 'Ostatnio dodane' },
    { value: 'TITLE', label: 'Tytuł' },
    { value: 'AUTHOR', label: 'Autor' },
    { value: 'READ_DATE', label: 'Data przeczytania' },
  ];

  ngOnInit() {
    this.bookService.loadBooks();
  }

  setViewMode(mode: BookView) {
    this.editingBookId.set(null);
    this.confirmingDeleteId.set(null);
    this.bookService.setViewMode(mode);
  }

  setStatusFilter(status: BookStatusFilter) {
    this.editingBookId.set(null);
    this.confirmingDeleteId.set(null);
    this.bookService.setStatusFilter(status);
  }

  setSort(sort: string) {
    this.bookService.setSort(sort as BookSort);
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

  detailsBook = signal<Book | null>(null);

  openDetails(book: Book) {
    this.detailsBook.set(book);
  }

  closeDetails() {
    this.detailsBook.set(null);
  }
}
