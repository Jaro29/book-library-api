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

  loadBooks() {
    this.http.get<PageResponse<Book>>('http://localhost:8080/books').subscribe((response) => {
      this.books.set(response.content);
    });
  }

  createBook(book: { title: string; author: string }, allowDuplicate = false) {
    return this.http.post<Book>(
      `http://localhost:8080/books?allowDuplicate=${allowDuplicate}`,
      book
    );
  }
}