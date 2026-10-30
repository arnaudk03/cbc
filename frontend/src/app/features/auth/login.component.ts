import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { finalize } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResult } from '../../core/auth.models';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly etape = signal<'IDENTIFIANTS' | 'OTP'>('IDENTIFIANTS');
  readonly chargement = signal(false);
  readonly erreur = signal<string | null>(null);
  readonly otpDemo = signal<string | null>(null);
  readonly modeDemo = !environment.production;
  readonly annee = new Date().getFullYear();

  readonly form = this.fb.group({
    identifiant: ['', [Validators.required]],
    motDePasse: ['', [Validators.required, Validators.minLength(6)]]
  });

  readonly otpForm = this.fb.group({
    code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
  });

  private challengeToken = '';
  private returnUrl = '/dashboard';

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/dashboard';
  }

  get identifiant() {
    return this.form.controls.identifiant;
  }

  get motDePasse() {
    return this.form.controls.motDePasse;
  }

  get codeOtp() {
    return this.otpForm.controls.code;
  }

  soumettreIdentifiants(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.erreur.set(null);
    this.chargement.set(true);
    const { identifiant, motDePasse } = this.form.getRawValue();

    this.auth
      .login(identifiant, motDePasse)
      .pipe(finalize(() => this.chargement.set(false)))
      .subscribe({
        next: (resultat) => {
          if (resultat.requires2fa) {
            this.challengeToken = resultat.challengeToken ?? '';
            this.otpDemo.set(this.modeDemo ? (resultat.otpDemo ?? null) : null);
            if (this.modeDemo && resultat.otpDemo) {
              this.otpForm.patchValue({ code: resultat.otpDemo });
            }
            this.etape.set('OTP');
            return;
          }
          this.finaliserConnexion(resultat);
        },
        error: (erreur) => this.erreur.set(this.messageErreur(erreur))
      });
  }

  verifierOtp(): void {
    if (this.otpForm.invalid || !this.challengeToken) {
      this.otpForm.markAllAsTouched();
      return;
    }
    this.erreur.set(null);
    this.chargement.set(true);

    this.auth
      .verifierOtp(this.challengeToken, this.otpForm.getRawValue().code)
      .pipe(finalize(() => this.chargement.set(false)))
      .subscribe({
        next: (resultat) => this.finaliserConnexion(resultat),
        error: (erreur) => this.erreur.set(this.messageErreur(erreur))
      });
  }

  revenirAuxIdentifiants(): void {
    this.etape.set('IDENTIFIANTS');
    this.otpForm.reset();
    this.otpDemo.set(null);
    this.erreur.set(null);
  }

  private finaliserConnexion(resultat: AuthResult): void {
    this.auth.appliquerSession(resultat);
    void this.router.navigateByUrl(this.returnUrl);
  }

  private messageErreur(erreur: unknown): string {
    const reponse = erreur as { error?: { message?: string; errors?: Record<string, string> } };
    if (reponse?.error?.errors) {
      const premier = Object.values(reponse.error.errors)[0];
      if (premier) {
        return premier;
      }
    }
    return reponse?.error?.message ?? 'Une erreur est survenue. Veuillez réessayer.';
  }
}
