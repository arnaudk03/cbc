import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from './auth.models';
import {
  Campagne,
  CampagnePayload,
  Comptoir,
  ComptoirPayload,
  Magasin,
  MagasinPayload,
  Producteur,
  ProducteurPayload,
  Speculation,
  SpeculationPayload
} from './api.models';

@Injectable({ providedIn: 'root' })
export class ReferentielService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  campagnes(): Observable<Campagne[]> {
    return this.http
      .get<ApiResponse<Campagne[]>>(`${this.baseUrl}/campagnes`)
      .pipe(map((reponse) => reponse.data));
  }

  campagneActive(): Observable<Campagne> {
    return this.http
      .get<ApiResponse<Campagne>>(`${this.baseUrl}/campagnes/active`)
      .pipe(map((reponse) => reponse.data));
  }

  comptoirs(): Observable<Comptoir[]> {
    return this.http
      .get<ApiResponse<Comptoir[]>>(`${this.baseUrl}/comptoirs`)
      .pipe(map((reponse) => reponse.data));
  }

  speculations(activesSeulement = true): Observable<Speculation[]> {
    const params = new HttpParams().set('activesSeulement', activesSeulement);
    return this.http
      .get<ApiResponse<Speculation[]>>(`${this.baseUrl}/speculations`, { params })
      .pipe(map((reponse) => reponse.data));
  }

  producteurs(): Observable<Producteur[]> {
    return this.http
      .get<ApiResponse<Producteur[]>>(`${this.baseUrl}/producteurs`)
      .pipe(map((reponse) => reponse.data));
  }

  magasins(): Observable<Magasin[]> {
    return this.http
      .get<ApiResponse<Magasin[]>>(`${this.baseUrl}/magasins`)
      .pipe(map((reponse) => reponse.data));
  }

  creerCampagne(payload: CampagnePayload): Observable<Campagne> {
    return this.http
      .post<ApiResponse<Campagne>>(`${this.baseUrl}/campagnes`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  modifierCampagne(id: number, payload: CampagnePayload): Observable<Campagne> {
    return this.http
      .put<ApiResponse<Campagne>>(`${this.baseUrl}/campagnes/${id}`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  supprimerCampagne(id: number): Observable<unknown> {
    return this.http.delete(`${this.baseUrl}/campagnes/${id}`);
  }

  creerSpeculation(payload: SpeculationPayload): Observable<Speculation> {
    return this.http
      .post<ApiResponse<Speculation>>(`${this.baseUrl}/speculations`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  modifierSpeculation(id: number, payload: SpeculationPayload): Observable<Speculation> {
    return this.http
      .put<ApiResponse<Speculation>>(`${this.baseUrl}/speculations/${id}`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  supprimerSpeculation(id: number): Observable<unknown> {
    return this.http.delete(`${this.baseUrl}/speculations/${id}`);
  }

  creerComptoir(payload: ComptoirPayload): Observable<Comptoir> {
    return this.http
      .post<ApiResponse<Comptoir>>(`${this.baseUrl}/comptoirs`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  modifierComptoir(id: number, payload: ComptoirPayload): Observable<Comptoir> {
    return this.http
      .put<ApiResponse<Comptoir>>(`${this.baseUrl}/comptoirs/${id}`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  supprimerComptoir(id: number): Observable<unknown> {
    return this.http.delete(`${this.baseUrl}/comptoirs/${id}`);
  }

  creerProducteur(payload: ProducteurPayload): Observable<Producteur> {
    return this.http
      .post<ApiResponse<Producteur>>(`${this.baseUrl}/producteurs`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  modifierProducteur(id: number, payload: ProducteurPayload): Observable<Producteur> {
    return this.http
      .put<ApiResponse<Producteur>>(`${this.baseUrl}/producteurs/${id}`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  supprimerProducteur(id: number): Observable<unknown> {
    return this.http.delete(`${this.baseUrl}/producteurs/${id}`);
  }

  creerMagasin(payload: MagasinPayload): Observable<Magasin> {
    return this.http
      .post<ApiResponse<Magasin>>(`${this.baseUrl}/magasins`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  modifierMagasin(id: number, payload: MagasinPayload): Observable<Magasin> {
    return this.http
      .put<ApiResponse<Magasin>>(`${this.baseUrl}/magasins/${id}`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  supprimerMagasin(id: number): Observable<unknown> {
    return this.http.delete(`${this.baseUrl}/magasins/${id}`);
  }
}
