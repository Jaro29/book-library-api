import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../environments/environment';

interface LoginResponse {
  token: string;
  displayName: string;
}

interface RegisterResponse {
  id: number;
  displayName: string;
  email: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  token = signal<string | null>(sessionStorage.getItem('token'));
  displayName = signal<string | null>(sessionStorage.getItem('displayName'));

  login(email: string, password: string) {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/login`, { email, password });
  }

  register(displayName: string, email: string, password: string) {
    return this.http.post<RegisterResponse>(`${environment.apiUrl}/register`, {
      displayName,
      email,
      password,
    });
  }

  setSession(token: string, displayName: string) {
    this.token.set(token);
    this.displayName.set(displayName);
    sessionStorage.setItem('token', token);
    sessionStorage.setItem('displayName', displayName);
  }

  logout() {
    this.token.set(null);
    this.displayName.set(null);
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('displayName');
  }
}