import { Component, input, output, signal, OnInit } from '@angular/core';
import { form, FormField, required } from '@angular/forms/signals';
import { inject } from '@angular/core';
import { BookService } from '../../services/book';
import { Book } from '../../models/book';

@Component({
  selector: 'app-edit-book-form',
  imports: [FormField],
  templateUrl: './edit-book-form.html',
  styleUrl: './edit-book-form.css',
})
export class EditBookForm implements OnInit {
  private bookService = inject(BookService);

  book = input.required<Book>();

  saved = output<void>();
  cancelled = output<void>();

  protected model = signal({
    title: '',
    author: '',
    isbn: '',
    status: '',
    startDate: '',
    finishDate: '',
    notes: '',
  });

  ngOnInit(): void {
    const book = this.book();
    this.model.set({
      title: book.title,
      author: book.author,
      isbn: book.isbn ?? '',
      status: book.status,
      startDate: book.startDate ?? '',
      finishDate: book.finishDate ?? '',
      notes: book.notes ?? '',
    });
  }

  protected bookForm = form(this.model, (path) => {
    required(path.title, { message: 'Tytuł jest wymagany' });
    required(path.author, { message: 'Autor jest wymagany' });
  });

  protected onSubmit(event: Event) {
    event.preventDefault();
    if (this.bookForm().invalid()) {
      return;
    }
    this.bookService.updateBook(this.book().id, this.model()).subscribe({
      next: () => {
        this.bookService.loadBooks(this.bookService.currentPage());
        this.saved.emit();
      },
      error: (err) => {
        console.error('Błąd:', err);
      },
    });
  }
}
