import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditBookForm } from './edit-book-form';

describe('EditBookForm', () => {
  let component: EditBookForm;
  let fixture: ComponentFixture<EditBookForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditBookForm],
    }).compileComponents();

    fixture = TestBed.createComponent(EditBookForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
