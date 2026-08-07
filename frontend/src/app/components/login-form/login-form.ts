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

  username = signal('');
  password = signal('');

  protected onSubmit(event: Event) {
    event.preventDefault();
    this.authService.login(this.username(), this.password());
  }
}
