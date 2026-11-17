import { AfterViewInit, Component, ElementRef, input, output, viewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Book } from '../../models/book';

@Component({
  selector: 'app-book-details',
  imports: [DatePipe],
  templateUrl: './book-details.html',
  styleUrl: './book-details.css',
})
export class BookDetails implements AfterViewInit {
  book = input.required<Book>();
  closed = output<void>();
  editRequested = output<number>();

  private dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  ngAfterViewInit() {
    // showModal() daje tło, zamykanie klawiszem Esc i fokus wewnątrz okna
    this.dialog().nativeElement.showModal?.();
  }

  close() {
    this.dialog().nativeElement.close?.();
    this.closed.emit();
  }

  edit() {
    this.close();
    this.editRequested.emit(this.book().id);
  }

  onDialogClick(event: MouseEvent) {
    // Kliknięcie w tło poza oknem trafia w sam element <dialog>
    if (event.target === this.dialog().nativeElement) {
      this.close();
    }
  }
}
