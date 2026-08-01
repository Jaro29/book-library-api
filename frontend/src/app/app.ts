import { Component } from '@angular/core';
import { BookList } from './components/book-list/book-list';
import { RouterOutlet } from "@angular/router";
import { AddBookForm } from './components/add-book-form/add-book-form';

@Component({
  selector: 'app-root',
  imports: [BookList, RouterOutlet, AddBookForm],  // dodaj tutaj
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}