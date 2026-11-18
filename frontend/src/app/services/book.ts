import { HttpClient } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { catchError, debounceTime, distinctUntilChanged, of, Subject, switchMap } from 'rxjs';
import { Book } from '../models/book';
import { environment } from '../../environments/environment';
import { BookSuggestion } from '../models/book-suggestion';

export type BookView = 'list' | 'grid';
export type BookStatusFilter = 'TO_READ' | 'READING' | 'FINISHED' | null;
export type BookSort = 'ADDED' | 'TITLE' | 'AUTHOR' | 'READ_DATE';

const VIEW_STORAGE_KEY = 'bookView';
const PAGE_SIZES: Record<BookView, number> = { list: 20, grid: 48 };

function readStoredView(): BookView {
  try {
    return localStorage.getItem(VIEW_STORAGE_KEY) === 'grid' ? 'grid' : 'list';
  } catch {
    return 'list';
  }
}

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
  viewMode = signal<BookView>(readStoredView());
  statusFilter = signal<BookStatusFilter>(null);
  sort = signal<BookSort>('ADDED');

  get pageSize() {
    return PAGE_SIZES[this.viewMode()];
  }

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

  setViewMode(mode: BookView) {
    if (mode === this.viewMode()) {
      return;
    }
    const firstBookIndex = this.currentPage() * this.pageSize;
    this.viewMode.set(mode);
    try {
      localStorage.setItem(VIEW_STORAGE_KEY, mode);
    } catch {
      // brak dostępu do localStorage: widok działa, tylko nie zostanie zapamiętany
    }
    this.loadBooks(Math.floor(firstBookIndex / this.pageSize));
  }

  search(query: string) {
    this.searchInput.next(query);
  }

  clearSearch() {
    this.searchQuery.set('');
    this.loadBooks(0);
  }

  setStatusFilter(status: BookStatusFilter) {
    if (status === this.statusFilter()) {
      return;
    }
    this.statusFilter.set(status);
    this.loadBooks(0);
  }

  setSort(sort: BookSort) {
    if (sort === this.sort()) {
      return;
    }
    this.sort.set(sort);
    this.loadBooks(0);
  }

  private fetchPage(page: number) {
    const search = this.searchQuery();
    const status = this.statusFilter();
    const searchParam = search ? `&search=${encodeURIComponent(search)}` : '';
    const statusParam = status ? `&status=${status}` : '';

    return this.http
      .get<PageResponse<Book>>(
        `${environment.apiUrl}/books?page=${page}&pageSize=${this.pageSize}${searchParam}${statusParam}&sort=${this.sort()}`,
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

  searchSuggestions(author: string) {
    return this.http.get<BookSuggestion[]>(
      `${environment.apiUrl}/books/suggestions?author=${encodeURIComponent(author)}`,
    );
  }
}
