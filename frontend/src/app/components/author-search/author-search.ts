import { Component, computed, inject, output, signal } from '@angular/core';
import { catchError, forkJoin, of } from 'rxjs';
import { BookService } from '../../services/book';
import { BookSuggestion } from '../../models/book-suggestion';

@Component({
  selector: 'app-author-search',
  imports: [],
  templateUrl: './author-search.html',
  styleUrl: './author-search.css',
})
export class AuthorSearch {
  private bookService = inject(BookService);

  closed = output<void>();

  author = signal('');
  results = signal<BookSuggestion[]>([]);
  selectedIndexes = signal<Set<number>>(new Set());
  searching = signal(false);
  adding = signal(false);
  summary = signal<string | null>(null);
  error = signal<string | null>(null);

  readonly pageSize = 20;
  page = signal(0);

  pageCount = computed(() => Math.ceil(this.results().length / this.pageSize));

  pageResults = computed(() =>
    this.results().slice(this.page() * this.pageSize, (this.page() + 1) * this.pageSize),
  );

  nextPage() {
    if (this.page() + 1 < this.pageCount()) {
      this.page.update((current) => current + 1);
    }
  }

  prevPage() {
    if (this.page() > 0) {
      this.page.update((current) => current - 1);
    }
  }

  onSearch(event: Event) {
    event.preventDefault();
    this.searching.set(true);
    this.error.set(null);
    this.summary.set(null);

    this.bookService.searchSuggestions(this.author()).subscribe({
      next: (suggestions) => {
        this.results.set(suggestions);
        this.selectedIndexes.set(new Set());
        this.page.set(0);
        this.searching.set(false);
      },
      error: () => {
        this.error.set('Nie udało się pobrać wyników. Spróbuj ponownie.');
        this.searching.set(false);
      },
    });
  }

  toggleSelected(index: number) {
    const updated = new Set(this.selectedIndexes());
    if (updated.has(index)) {
      updated.delete(index);
    } else {
      updated.add(index);
    }
    this.selectedIndexes.set(updated);
  }

  selectAll() {
    const allIndexes = this.results().map((_, index) => index);
    this.selectedIndexes.set(new Set(allIndexes));
  }

  deselectAll() {
    this.selectedIndexes.set(new Set());
  }

  addSelected() {
    const selected = this.results().filter((_, index) => this.selectedIndexes().has(index));
    if (selected.length === 0) {
      return;
    }

    this.adding.set(true);

    const requests = selected.map((suggestion) =>
      this.bookService
        .createBook({
          title: suggestion.title,
          author: suggestion.author,
          isbn: suggestion.isbn,
          coverUrl: suggestion.coverUrl,
          status: 'FINISHED',
          timesRead: 1,
        })
        .pipe(catchError(() => of(null))),
    );

    forkJoin(requests).subscribe((responses) => {
      const added = responses.filter((response) => response !== null).length;
      const skipped = responses.length - added;
      this.summary.set(`Dodano ${added}, pominięto ${skipped} (już w bibliotece).`);
      this.adding.set(false);
      this.bookService.loadBooks(this.bookService.currentPage());
    });
  }
}
