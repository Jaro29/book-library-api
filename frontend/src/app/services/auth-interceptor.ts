import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const credentials = authService.token();

  if (credentials) {
    const authReq = req.clone({
      setHeaders: { Authorization: `Bearer ${credentials}` },
    });
    return next(authReq);
  }

  return next(req);
};