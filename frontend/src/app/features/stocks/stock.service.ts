import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../core/auth.models';
import {
  FicheStock,
  FsjRequest,
  ReceptionRequest,
  StockDisponible,
  Transfert,
  TransfertRequest
} from './stock.models';

@Injectable({ providedIn: 'root' })
export class StockService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  stockDisponible(comptoirId: number): Observable<StockDisponible> {
    return this.http
      .get<ApiResponse<StockDisponible>>(`${this.baseUrl}/stocks/comptoirs/${comptoirId}/disponible`)
      .pipe(map((reponse) => reponse.data));
  }

  enregistrerFsj(request: FsjRequest): Observable<FicheStock> {
    return this.http
      .post<ApiResponse<FicheStock>>(`${this.baseUrl}/stocks/fsj`, request)
      .pipe(map((reponse) => reponse.data));
  }

  listerFsj(comptoirId: number, date?: string): Observable<FicheStock[]> {
    let params = new HttpParams().set('comptoirId', comptoirId);
    if (date) {
      params = params.set('date', date);
    }
    return this.http
      .get<ApiResponse<FicheStock[]>>(`${this.baseUrl}/stocks/fsj`, { params })
      .pipe(map((reponse) => reponse.data));
  }

  listerTransferts(): Observable<Transfert[]> {
    return this.http
      .get<ApiResponse<Transfert[]>>(`${this.baseUrl}/transferts`)
      .pipe(map((reponse) => reponse.data));
  }

  creerTransfert(request: TransfertRequest): Observable<Transfert> {
    return this.http
      .post<ApiResponse<Transfert>>(`${this.baseUrl}/transferts`, request)
      .pipe(map((reponse) => reponse.data));
  }

  receptionner(id: number, request: ReceptionRequest): Observable<Transfert> {
    return this.http
      .post<ApiResponse<Transfert>>(`${this.baseUrl}/transferts/${id}/reception`, request)
      .pipe(map((reponse) => reponse.data));
  }
}
