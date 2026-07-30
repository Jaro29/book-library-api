import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
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

    getBooks() {
        return this.http.get<PageResponse<Book>>('http://localhost:8080/books');
    }
}
