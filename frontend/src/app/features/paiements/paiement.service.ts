import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../core/auth.models';
import { OtpChallenge, Paiement, PaiementStatut } from './paiement.models';

@Injectable({ providedIn: 'root' })
export class PaiementService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/paiements`;

  lister(statut?: PaiementStatut | ''): Observable<Paiement[]> {
    let params = new HttpParams();
    if (statut) {
      params = params.set('statut', statut);
    }
    return this.http
      .get<ApiResponse<Paiement[]>>(this.baseUrl, { params })
      .pipe(map((reponse) => reponse.data));
  }

  initier(bordereauId: number): Observable<Paiement> {
    return this.http
      .post<ApiResponse<Paiement>>(this.baseUrl, { bordereauId })
      .pipe(map((reponse) => reponse.data));
  }

  demanderOtp(id: number): Observable<OtpChallenge> {
    return this.http
      .post<ApiResponse<OtpChallenge>>(`${this.baseUrl}/${id}/otp`, {})
      .pipe(map((reponse) => reponse.data));
  }

  valider(id: number, codeOtp: string): Observable<Paiement> {
    return this.http
      .post<ApiResponse<Paiement>>(`${this.baseUrl}/${id}/validation`, { codeOtp })
      .pipe(map((reponse) => reponse.data));
  }

  rejeter(id: number, motif: string): Observable<Paiement> {
    return this.http
      .post<ApiResponse<Paiement>>(`${this.baseUrl}/${id}/rejet`, { motif })
      .pipe(map((reponse) => reponse.data));
  }
}
