import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BookDetails } from './book-details';
import { Book } from '../../models/book';

describe('BookDetails', () => {
  let fixture: ComponentFixture<BookDetails>;

  const sampleBook: Book = {
    id: 7,
    title: 'Solaris',
    author: 'Stanisław Lem',
    isbn: '9788308049713',
    status: 'FINISHED',
    startDate: '2026-01-03',
    finishDate: '2026-01-20',
    timesRead: 2,
    notes: 'Ocean myśli.',
    coverUrl: null,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookDetails],
    }).compileComponents();

    fixture = TestBed.createComponent(BookDetails);
    fixture.componentRef.setInput('book', sampleBook);
    await fixture.whenStable();
  });

  it('should show book fields', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Solaris');
    expect(text).toContain('Stanisław Lem');
    expect(text).toContain('9788308049713');
    expect(text).toContain('20.01.2026');
    expect(text).toContain('Ocean myśli.');
  });

  it('should emit book id when edit is clicked', () => {
    let editedId: number | undefined;
    fixture.componentInstance.editRequested.subscribe((id) => (editedId = id));
    const editButton = (fixture.nativeElement as HTMLElement).querySelector('.btn-primary') as HTMLButtonElement;
    editButton.click();
    expect(editedId).toBe(7);
  });
});
