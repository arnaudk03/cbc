import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from './auth.models';
import {
  AffectationComptoirPayload,
  AffectationComptoirResult,
  RoleOption,
  Utilisateur,
  UtilisateurPayload
} from './utilisateur.models';

@Injectable({ providedIn: 'root' })
export class UtilisateurService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  lister(): Observable<Utilisateur[]> {
    return this.http
      .get<ApiResponse<Utilisateur[]>>(`${this.baseUrl}/utilisateurs`)
      .pipe(map((reponse) => reponse.data));
  }

  roles(): Observable<RoleOption[]> {
    return this.http
      .get<ApiResponse<RoleOption[]>>(`${this.baseUrl}/utilisateurs/roles`)
      .pipe(map((reponse) => reponse.data));
  }

  creer(payload: UtilisateurPayload): Observable<Utilisateur> {
    return this.http
      .post<ApiResponse<Utilisateur>>(`${this.baseUrl}/utilisateurs`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  modifier(id: number, payload: UtilisateurPayload): Observable<Utilisateur> {
    return this.http
      .put<ApiResponse<Utilisateur>>(`${this.baseUrl}/utilisateurs/${id}`, payload)
      .pipe(map((reponse) => reponse.data));
  }

  changerActivation(id: number, actif: boolean): Observable<Utilisateur> {
    const params = new HttpParams().set('actif', actif);
    return this.http
      .patch<ApiResponse<Utilisateur>>(`${this.baseUrl}/utilisateurs/${id}/activation`, {}, { params })
      .pipe(map((reponse) => reponse.data));
  }

  supprimer(id: number): Observable<unknown> {
    return this.http.delete(`${this.baseUrl}/utilisateurs/${id}`);
  }

  affecterAuComptoir(
    comptoirId: number,
    utilisateurIds: number[]
  ): Observable<AffectationComptoirResult> {
    const payload: AffectationComptoirPayload = { utilisateurIds };
    return this.http
      .post<ApiResponse<AffectationComptoirResult>>(
        `${this.baseUrl}/comptoirs/${comptoirId}/utilisateurs`,
        payload
      )
      .pipe(map((reponse) => reponse.data));
  }
}
