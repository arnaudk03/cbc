import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../core/auth.models';
import {
  FicheStockMateriel,
  FicheStockMaterielPayload,
  MaterielEmballage,
  MaterielEmballagePayload,
  StockMateriel
} from './emballage.models';

@Injectable({ providedIn: 'root' })
export class EmballageService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/stocks/emballages`;

  materiels(actifsSeulement = true): Observable<MaterielEmballage[]> {
    const params = new HttpParams().set('actifsSeulement', actifsSeulement);
    return this.http
      .get<ApiResponse<MaterielEmballage[]>>(`${this.baseUrl}/materiels`, { params })
      .pipe(map((reponse) => reponse.data));
  }

  creerMateriel(payload: MaterielEmballagePayload): Observable<MaterielEmballage> {
    return this.http
      .post<ApiResponse<MaterielEmballage>>(`${this.baseUrl}/materiels`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  stockCourant(comptoirId: number): Observable<StockMateriel[]> {
    const params = new HttpParams().set('comptoirId', comptoirId);
    return this.http
      .get<ApiResponse<StockMateriel[]>>(`${this.baseUrl}/stock`, { params })
      .pipe(map((reponse) => reponse.data));
  }

  fiches(comptoirId: number, date?: string | null): Observable<FicheStockMateriel[]> {
    let params = new HttpParams().set('comptoirId', comptoirId);
    if (date) {
      params = params.set('date', date);
    }
    return this.http
      .get<ApiResponse<FicheStockMateriel[]>>(`${this.baseUrl}/fiches`, { params })
      .pipe(map((reponse) => reponse.data));
  }

  detailFiche(id: number): Observable<FicheStockMateriel> {
    return this.http
      .get<ApiResponse<FicheStockMateriel>>(`${this.baseUrl}/fiches/${id}`)
      .pipe(map((reponse) => reponse.data));
  }

  creerFiche(payload: FicheStockMaterielPayload): Observable<FicheStockMateriel> {
    return this.http
      .post<ApiResponse<FicheStockMateriel>>(`${this.baseUrl}/fiches`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  validerFiche(id: number): Observable<FicheStockMateriel> {
    return this.http
      .post<ApiResponse<FicheStockMateriel>>(`${this.baseUrl}/fiches/${id}/validation`, {})
      .pipe(map((reponse) => reponse.data));
  }
}
