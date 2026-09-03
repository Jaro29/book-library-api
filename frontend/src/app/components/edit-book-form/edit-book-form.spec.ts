import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditBookForm } from './edit-book-form';
import { Book } from '../../models/book';

describe('EditBookForm', () => {
  let component: EditBookForm;
  let fixture: ComponentFixture<EditBookForm>;

  const sampleBook: Book = {
    id: 1,
    title: 'Test',
    author: 'Test Autor',
    isbn: null,
    status: 'TO_READ',
    startDate: null,
    finishDate: null,
    timesRead: 0,
    notes: null,
    coverUrl: null,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditBookForm],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(EditBookForm);
    fixture.componentRef.setInput('book', sampleBook);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});