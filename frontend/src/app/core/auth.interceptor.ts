import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';

const URLS_SANS_JETON = ['/auth/login', '/auth/verify-otp', '/auth/refresh'];

export const authInterceptor: HttpInterceptorFn = (requete, suivant) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const urlSansJeton = URLS_SANS_JETON.some((url) => requete.url.includes(url));
  const jeton = auth.accessToken;
  const requeteAuthentifiee =
    jeton && !urlSansJeton
      ? requete.clone({ setHeaders: { Authorization: `Bearer ${jeton}` } })
      : requete;

  return suivant(requeteAuthentifiee).pipe(
    catchError((erreur: HttpErrorResponse) => {
      if (erreur.status !== 401 || !jeton || urlSansJeton) {
        return throwError(() => erreur);
      }

      return auth.rafraichirSession().pipe(
        switchMap((resultat) =>
          suivant(
            requete.clone({
              setHeaders: { Authorization: `Bearer ${resultat.accessToken ?? ''}` }
            })
          )
        ),
        catchError((erreurRafraichissement) => {
          auth.effacerSession();
          router.navigate(['/login'], { queryParams: { returnUrl: router.url } });
          return throwError(() => erreurRafraichissement);
        })
      );
    })
  );
};
