import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login-form',
  imports: [],
  templateUrl: './login-form.html',
  styleUrl: './login-form.css',
})
export class LoginForm {
  private authService = inject(AuthService);

  email = signal('');
  password = signal('');
  error = signal<string | null>(null);

  protected onSubmit(event: Event) {
    event.preventDefault();
    this.error.set(null);

    this.authService.login(this.email(), this.password()).subscribe({
      next: (response) => {
        this.authService.setSession(response.token, response.displayName);
      },
      error: (err) => {
        this.error.set(
          typeof err.error === 'string' && err.error.trim()
            ? err.error
            : 'Nieprawidłowy email lub hasło.',
        );
      },
    });
  }
}