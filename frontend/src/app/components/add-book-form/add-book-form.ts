import { Component, inject, signal } from '@angular/core';
import { form, FormField, required } from '@angular/forms/signals';
import { BookService } from '../../services/book';

@Component({
  selector: 'app-add-book-form',
  imports: [FormField],
  templateUrl: './add-book-form.html',
  styleUrl: './add-book-form.css',
})
export class AddBookForm {
  private bookService = inject(BookService);

  protected model = signal({
    title: '',
    author: '',
  });

  protected bookForm = form(this.model, (path) => {
    required(path.title, { message: 'Tytuł jest wymagany' });
    required(path.author, { message: 'Autor jest wymagany' });
  });

  protected onSubmit(event: Event) {
  event.preventDefault();
  if (this.bookForm().invalid()) {
    return;
  }
  this.bookService.createBook(this.model()).subscribe({
    next: () => {
      this.bookService.loadBooks();
      this.model.set({ title: '', author: '' });
    },
    error: (err) => console.error('Błąd:', err),
  });
}
}