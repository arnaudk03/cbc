import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../core/auth.models';
import { Localite, PrixGarantie, PrixGarantiePayload } from './referentiel.models';

@Injectable({ providedIn: 'root' })
export class ReferentielMetierService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  prixGarantis(campagneId: number): Observable<PrixGarantie[]> {
    const params = new HttpParams().set('campagneId', campagneId);
    return this.http
      .get<ApiResponse<PrixGarantie[]>>(`${this.baseUrl}/prix-garantis`, { params })
      .pipe(map((reponse) => reponse.data));
  }

  approuverPrix(id: number): Observable<PrixGarantie> {
    return this.http
      .patch<ApiResponse<PrixGarantie>>(`${this.baseUrl}/prix-garantis/${id}/approbation`, {})
      .pipe(map((reponse) => reponse.data));
  }

  localites(type?: string): Observable<Localite[]> {
    let params = new HttpParams();
    if (type) {
      params = params.set('type', type);
    }
    return this.http
      .get<ApiResponse<Localite[]>>(`${this.baseUrl}/localites`, { params })
      .pipe(map((reponse) => reponse.data));
  }

  activerComptoir(id: number, actif: boolean): Observable<unknown> {
    const params = new HttpParams().set('actif', actif);
    return this.http.patch(`${this.baseUrl}/comptoirs/${id}/activation`, {}, { params });
  }

  creerPrix(payload: PrixGarantiePayload): Observable<PrixGarantie> {
    return this.http
      .post<ApiResponse<PrixGarantie>>(`${this.baseUrl}/prix-garantis`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  modifierPrix(id: number, payload: PrixGarantiePayload): Observable<PrixGarantie> {
    return this.http
      .put<ApiResponse<PrixGarantie>>(`${this.baseUrl}/prix-garantis/${id}`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  rejeterPrix(id: number): Observable<PrixGarantie> {
    return this.http
      .patch<ApiResponse<PrixGarantie>>(`${this.baseUrl}/prix-garantis/${id}/rejet`, {})
      .pipe(map((reponse) => reponse.data));
  }

  supprimerPrix(id: number): Observable<unknown> {
    return this.http.delete(`${this.baseUrl}/prix-garantis/${id}`);
  }
}
