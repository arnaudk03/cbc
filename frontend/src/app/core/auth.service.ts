import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import {
  Observable,
  catchError,
  finalize,
  map,
  shareReplay,
  switchMap,
  tap,
  throwError,
  timer
} from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, AuthResult, ROLE_LABELS, UserSummary } from './auth.models';

const ACCESS_TOKEN_KEY = 'cbc.accessToken';
const REFRESH_TOKEN_KEY = 'cbc.refreshToken';
const USER_KEY = 'cbc.user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly _user = signal<UserSummary | null>(this.lireUtilisateurStocke());
  private refreshRequest$: Observable<AuthResult> | null = null;
  private refreshTimer: number | null = null;

  readonly user = this._user.asReadonly();
  readonly isAuthenticated = computed(() => this._user() !== null && this.accessToken !== null);
  readonly roles = computed(() => this._user()?.roles ?? []);
  readonly roleLabel = computed(() => {
    const roles = this.roles();
    if (roles.length === 0) {
      return 'Utilisateur';
    }
    return ROLE_LABELS[roles[0]] ?? roles[0];
  });
  readonly initiales = computed(() => {
    const utilisateur = this._user();
    if (!utilisateur) {
      return '?';
    }
    return `${utilisateur.prenom.charAt(0)}${utilisateur.nom.charAt(0)}`.toUpperCase();
  });

  constructor() {
    if (!this.accessToken) {
      this.effacerSession();
      return;
    }
    this.planifierRafraichissement();
  }

  get accessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  get refreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  login(identifiant: string, motDePasse: string): Observable<AuthResult> {
    return this.http
      .post<ApiResponse<AuthResult>>(`${environment.apiUrl}/auth/login`, { identifiant, motDePasse })
      .pipe(map((reponse) => reponse.data));
  }

  verifierOtp(challengeToken: string, code: string): Observable<AuthResult> {
    return this.http
      .post<ApiResponse<AuthResult>>(`${environment.apiUrl}/auth/verify-otp`, { challengeToken, code })
      .pipe(map((reponse) => reponse.data));
  }

  appliquerSession(resultat: AuthResult): void {
    if (!resultat.accessToken || !resultat.refreshToken) {
      return;
    }
    localStorage.setItem(ACCESS_TOKEN_KEY, resultat.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, resultat.refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify(resultat.utilisateur));
    this._user.set(resultat.utilisateur);
    this.planifierRafraichissement();
  }

  rafraichirSession(): Observable<AuthResult> {
    const token = this.refreshToken;
    if (!token) {
      return throwError(() => new Error('Aucun jeton de rafraîchissement disponible.'));
    }
    if (!this.refreshRequest$) {
      this.refreshRequest$ = this.executerRafraichissement(token).pipe(
        finalize(() => {
          this.refreshRequest$ = null;
        }),
        shareReplay(1)
      );
    }
    return this.refreshRequest$;
  }

  private executerRafraichissement(token: string, reessayer = true): Observable<AuthResult> {
    return this.http
      .post<ApiResponse<AuthResult>>(`${environment.apiUrl}/auth/refresh`, { refreshToken: token })
      .pipe(
        map((reponse) => reponse.data),
        tap((resultat) => this.appliquerSession(resultat)),
        catchError((erreur) => {
          if (!reessayer) {
            this.effacerSession();
            return throwError(() => erreur);
          }
          return timer(600).pipe(
            switchMap(() => {
              const tokenActuel = this.refreshToken;
              if (tokenActuel && tokenActuel !== token) {
                return this.executerRafraichissement(tokenActuel, false);
              }
              this.effacerSession();
              return throwError(() => erreur);
            })
          );
        })
      );
  }

  private planifierRafraichissement(): void {
    this.annulerRafraichissement();
    const token = this.accessToken;
    if (!token || !this.refreshToken) {
      return;
    }
    const expiration = this.lireExpiration(token);
    if (expiration === null) {
      return;
    }
    const marge = 60_000 + Math.floor(Math.random() * 30_000);
    const delai = expiration - Date.now() - marge;
    if (delai <= 0) {
      return;
    }
    this.refreshTimer = window.setTimeout(() => {
      this.refreshTimer = null;
      this.rafraichirSession().subscribe({ error: () => undefined });
    }, delai);
  }

  private annulerRafraichissement(): void {
    if (this.refreshTimer !== null) {
      window.clearTimeout(this.refreshTimer);
      this.refreshTimer = null;
    }
  }

  private lireExpiration(token: string): number | null {
    try {
      const charge = token.split('.')[1];
      if (!charge) {
        return null;
      }
      const base64 = charge.replace(/-/g, '+').replace(/_/g, '/');
      const complete = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
      const chargeUtile = JSON.parse(atob(complete)) as { exp?: unknown };
      return typeof chargeUtile.exp === 'number' ? chargeUtile.exp * 1000 : null;
    } catch {
      return null;
    }
  }

  deconnexion(): void {
    const token = this.refreshToken;
    if (token) {
      this.http
        .post(`${environment.apiUrl}/auth/logout`, { refreshToken: token })
        .subscribe({ error: () => undefined });
    }
    this.effacerSession();
    this.router.navigate(['/login']);
  }

  effacerSession(): void {
    this.annulerRafraichissement();
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this._user.set(null);
  }

  possedeUnRole(...roles: string[]): boolean {
    return this.roles().some((role) => roles.includes(role));
  }

  private lireUtilisateurStocke(): UserSummary | null {
    const brut = localStorage.getItem(USER_KEY);
    if (!brut) {
      return null;
    }
    try {
      return JSON.parse(brut) as UserSummary;
    } catch {
      return null;
    }
  }
}
