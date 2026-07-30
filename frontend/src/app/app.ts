import { Component } from '@angular/core';
import { BookList } from './components/book-list/book-list';
import { RouterOutlet } from "@angular/router";

@Component({
  selector: 'app-root',
  imports: [BookList, RouterOutlet],  // dodaj tutaj
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}