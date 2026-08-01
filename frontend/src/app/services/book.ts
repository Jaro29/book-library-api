import { HttpClient } from '@angular/common/http';
import { inject, Service, signal } from '@angular/core';
import { Book } from '../models/book';

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
  pageSize = 10;

  loadBooks(page: number = 0) {
    this.http
      .get<PageResponse<Book>>(
        `http://localhost:8080/books?page=${page}&pageSize=${this.pageSize}`
      )
      .subscribe((response) => {
        this.books.set(response.content);
        this.currentPage.set(response.page);
        this.totalPages.set(response.totalPages);
      });
  }

  createBook(book: { title: string; author: string }, allowDuplicate = false) {
    return this.http.post<Book>(
      `http://localhost:8080/books?allowDuplicate=${allowDuplicate}`,
      book
    );
  }
}