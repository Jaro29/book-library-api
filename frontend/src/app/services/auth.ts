import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  credentials = signal<{ username: string; password: string } | null>(null);

  login(username: string, password: string) {
    this.credentials.set({ username, password });
  }

  logout() {
    this.credentials.set(null);
  }
}
