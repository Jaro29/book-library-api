import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BookService } from './book';

describe('BookService', () => {
  let service: BookService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(BookService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  const emptyPage = { content: [], page: 0, pageSize: 20, totalElements: 0, totalPages: 0 };

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should request recently added books without status by default', () => {
    service.loadBooks();

    const request = http.expectOne((req) => req.url.includes('/books?'));
    expect(request.request.url).toContain('sort=ADDED');
    expect(request.request.url).not.toContain('status=');
    request.flush(emptyPage);
  });

  it('should send status filter and sort, starting from the first page', () => {
    service.setStatusFilter('FINISHED');
    http.expectOne((req) => req.url.includes('status=FINISHED')).flush(emptyPage);

    service.setSort('READ_DATE');
    const request = http.expectOne((req) => req.url.includes('sort=READ_DATE'));
    expect(request.request.url).toContain('page=0');
    expect(request.request.url).toContain('status=FINISHED');
    request.flush(emptyPage);
  });
});
