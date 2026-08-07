import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const credentials = authService.credentials();

  if (credentials) {
    const encoded = btoa(`${credentials.username}:${credentials.password}`);
    const authReq = req.clone({
      setHeaders: { Authorization: `Basic ${encoded}` },
    });
    return next(authReq);
  }

  return next(req);
};