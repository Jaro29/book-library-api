import { HttpClient } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { catchError, debounceTime, distinctUntilChanged, of, Subject, switchMap } from 'rxjs';
import { Book } from '../models/book';
import { environment } from '../../environments/environment';
import { BookSuggestion } from '../models/book-suggestion';

interface PageResponse<T> {
  content: T[];
  page: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}

@Service()
export class BookService {
  private http = inject(HttpClient);

  books = signal<Book[]>([]);
  currentPage = signal(0);
  totalPages = signal(0);
  searchQuery = signal('');
  loadError = signal<string | null>(null);
  pageSize = 20;

  private pageRequests = new Subject<number>();
  private searchInput = new Subject<string>();

  constructor() {
    this.pageRequests
      .pipe(
        switchMap((page) => this.fetchPage(page)),
      )
      .subscribe((response) => {
        if (response === null) {
          this.loadError.set('Nie udało się pobrać listy książek. Sprawdź połączenie i spróbuj ponownie.');
          return;
        }
        this.loadError.set(null);
        this.books.set(response.content);
        this.currentPage.set(response.page);
        this.totalPages.set(response.totalPages);
      });

    this.searchInput
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe((query) => {
        this.searchQuery.set(query);
        this.loadBooks(0);
      });
  }

  loadBooks(page: number = 0) {
    this.pageRequests.next(page);
  }

  search(query: string) {
    this.searchInput.next(query);
  }

  clearSearch() {
    this.searchQuery.set('');
    this.loadBooks(0);
  }

  private fetchPage(page: number) {
    const search = this.searchQuery();
    const searchParam = search ? `&search=${encodeURIComponent(search)}` : '';

    return this.http
      .get<PageResponse<Book>>(
        `${environment.apiUrl}/books?page=${page}&pageSize=${this.pageSize}${searchParam}`,
      )
      .pipe(catchError(() => of(null)));
  }

  createBook(book: Partial<Book>, allowDuplicate = false) {
    return this.http.post<Book>(
      `${environment.apiUrl}/books?allowDuplicate=${allowDuplicate}`,
      book,
    );
  }

  deleteBook(id: number) {
    return this.http.delete<void>(`${environment.apiUrl}/books/${id}`);
  }

  updateBook(id: number, changes: Partial<Book>) {
    return this.http.patch<Book>(`${environment.apiUrl}/books/${id}`, changes);
  }

  searchSuggestions(author: string, source: 'bn' | 'google') {
    const langParam = source === 'google' ? '&lang=pl' : '';
    return this.http.get<BookSuggestion[]>(
      `${environment.apiUrl}/books/suggestions?author=${encodeURIComponent(author)}&source=${source}${langParam}`,
    );
  }
}
