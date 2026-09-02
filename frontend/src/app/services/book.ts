import { HttpClient } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
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
  pageSize = 10;

  loadBooks(page: number = 0) {
    const search = this.searchQuery();
    const searchParam = search ? `&search=${encodeURIComponent(search)}` : '';

    this.http
      .get<PageResponse<Book>>(
        `${environment.apiUrl}/books?page=${page}&pageSize=${this.pageSize}${searchParam}`,
      )
      .subscribe((response) => {
        this.books.set(response.content);
        this.currentPage.set(response.page);
        this.totalPages.set(response.totalPages);
      });
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
