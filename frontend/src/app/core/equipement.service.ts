import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from './auth.models';
import {
  AffectationEquipementPayload,
  Equipement,
  EquipementPayload,
  StatutEquipement,
  TypeEquipement
} from './equipement.models';

export interface EquipementFiltres {
  comptoirId?: number;
  disponibles?: boolean;
  type?: TypeEquipement;
}

@Injectable({ providedIn: 'root' })
export class EquipementService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  lister(filtres: EquipementFiltres = {}): Observable<Equipement[]> {
    let params = new HttpParams();
    if (filtres.comptoirId !== undefined) {
      params = params.set('comptoirId', filtres.comptoirId);
    }
    if (filtres.disponibles) {
      params = params.set('disponibles', true);
    }
    return this.http
      .get<ApiResponse<Equipement[]>>(`${this.baseUrl}/equipements`, { params })
      .pipe(map((reponse) => reponse.data));
  }

  creer(payload: EquipementPayload): Observable<Equipement> {
    return this.http
      .post<ApiResponse<Equipement>>(`${this.baseUrl}/equipements`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  modifier(id: number, payload: EquipementPayload): Observable<Equipement> {
    return this.http
      .put<ApiResponse<Equipement>>(`${this.baseUrl}/equipements/${id}`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  affecterEnLot(payload: AffectationEquipementPayload): Observable<Equipement[]> {
    return this.http
      .post<ApiResponse<Equipement[]>>(`${this.baseUrl}/equipements/affectations`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  retirer(id: number, motif: string | null): Observable<Equipement> {
    let params = new HttpParams();
    if (motif) {
      params = params.set('motif', motif);
    }
    return this.http
      .delete<ApiResponse<Equipement>>(`${this.baseUrl}/equipements/${id}/affectation`, {
        params
      })
      .pipe(map((reponse) => reponse.data));
  }

  changerStatut(id: number, statut: StatutEquipement): Observable<Equipement> {
    return this.http
      .patch<ApiResponse<Equipement>>(`${this.baseUrl}/equipements/${id}/statut`, { statut })
      .pipe(map((reponse) => reponse.data));
  }
}
