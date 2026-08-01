import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddBookForm } from './add-book-form';

describe('AddBookForm', () => {
  let component: AddBookForm;
  let fixture: ComponentFixture<AddBookForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddBookForm],
    }).compileComponents();

    fixture = TestBed.createComponent(AddBookForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
