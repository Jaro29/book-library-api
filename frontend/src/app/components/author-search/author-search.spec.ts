import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthorSearch } from './author-search';

describe('AuthorSearch', () => {
  let component: AuthorSearch;
  let fixture: ComponentFixture<AuthorSearch>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthorSearch],
    }).compileComponents();

    fixture = TestBed.createComponent(AuthorSearch);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
