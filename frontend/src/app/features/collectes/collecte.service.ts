import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../core/auth.models';
import { PageResponse } from '../../core/api.models';
import {
  Collecte,
  CollecteLotResponse,
  CollecteStatut,
  CreateCollecteLotRequest
} from './collecte.models';

@Injectable({ providedIn: 'root' })
export class CollecteService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/collectes`;

  lister(options: {
    statut?: CollecteStatut | '';
    campagneId?: number | null;
    comptoirId?: number | null;
    page?: number;
    size?: number;
  }): Observable<PageResponse<Collecte>> {
    let params = new HttpParams()
      .set('page', options.page ?? 0)
      .set('size', options.size ?? 20);
    if (options.statut) {
      params = params.set('statut', options.statut);
    }
    if (options.campagneId) {
      params = params.set('campagneId', options.campagneId);
    }
    if (options.comptoirId) {
      params = params.set('comptoirId', options.comptoirId);
    }
    return this.http
      .get<ApiResponse<PageResponse<Collecte>>>(this.baseUrl, { params })
      .pipe(map((reponse) => reponse.data));
  }

  enregistrerLot(request: CreateCollecteLotRequest): Observable<CollecteLotResponse> {
    return this.http
      .post<ApiResponse<CollecteLotResponse>>(`${this.baseUrl}/lot`, request)
      .pipe(map((reponse) => reponse.data));
  }

  listerParBrbc(bordereauId: number): Observable<Collecte[]> {
    return this.http
      .get<ApiResponse<Collecte[]>>(`${this.baseUrl}/brbc/${bordereauId}`)
      .pipe(map((reponse) => reponse.data));
  }

  examiner(id: number, approuve: boolean, motif?: string): Observable<Collecte> {
    return this.http
      .post<ApiResponse<Collecte>>(`${this.baseUrl}/${id}/examen`, { approuve, motif })
      .pipe(map((reponse) => reponse.data));
  }

  valider(id: number, approuve: boolean, motif?: string): Observable<Collecte> {
    return this.http
      .post<ApiResponse<Collecte>>(`${this.baseUrl}/${id}/validation`, { approuve, motif })
      .pipe(map((reponse) => reponse.data));
  }
}
