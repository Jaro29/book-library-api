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
    isbn: '',
    status: 'TO_READ',
    startDate: '',
    finishDate: '',
    notes: '',
  });

  protected bookForm = form(this.model, (path) => {
    required(path.title, { message: 'Tytuł jest wymagany' });
    required(path.author, { message: 'Autor jest wymagany' });
  });

  protected duplicateError = signal(false);

  protected onSubmit(event: Event) {
    event.preventDefault();
    if (this.bookForm().invalid()) {
      return;
    }
    this.submitBook(false);
  }

  protected confirmDuplicate() {
    this.submitBook(true);
  }

  protected cancelDuplicate() {
    this.duplicateError.set(false);
  }

  private submitBook(allowDuplicate: boolean) {
    this.bookService.createBook(this.model(), allowDuplicate).subscribe({
      next: () => {
        this.bookService.loadBooks(this.bookService.currentPage());
        this.model.set({
          title: '',
          author: '',
          isbn: '',
          status: 'TO_READ',
          startDate: '',
          finishDate: '',
          notes: '',
        });
        this.duplicateError.set(false);
      },
      error: (err) => {
        if (err.status === 409) {
          this.duplicateError.set(true);
        } else {
          console.error('Błąd:', err);
        }
      },
    });
  }
}
