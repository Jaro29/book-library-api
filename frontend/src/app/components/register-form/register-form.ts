import { Component, inject, output, signal } from '@angular/core';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-register-form',
  imports: [],
  templateUrl: './register-form.html',
  styleUrl: './register-form.css',
})
export class RegisterForm {
  private authService = inject(AuthService);

  displayName = signal('');
  email = signal('');
  password = signal('');
  error = signal<string | null>(null);

  registered = output<void>();

  protected onSubmit(event: Event) {
    event.preventDefault();
    this.error.set(null);

    this.authService.register(this.displayName(), this.email(), this.password()).subscribe({
      next: () => {
        this.registered.emit();
      },
      error: (err) => {
        this.error.set(
          typeof err.error === 'string' ? err.error : 'Wystąpił błąd. Spróbuj ponownie.'
        );
      },
    });
  }
}