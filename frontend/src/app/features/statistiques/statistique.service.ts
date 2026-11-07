import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../core/auth.models';
import { FiltreStatistique, StatistiquesResponse } from './statistique.models';

@Injectable({ providedIn: 'root' })
export class StatistiqueService {
  private readonly http = inject(HttpClient);

  charger(filtre?: FiltreStatistique): Observable<StatistiquesResponse> {
    const params = this.construireParams(filtre);
    return this.http
      .get<ApiResponse<StatistiquesResponse>>(`${environment.apiUrl}/statistiques`, { params })
      .pipe(map((reponse) => reponse.data));
  }

  telechargerCsv(filtre?: FiltreStatistique): Observable<Blob> {
    const params = this.construireParams(filtre);
    return this.http.get(`${environment.apiUrl}/statistiques/export-csv`, {
      params,
      responseType: 'blob'
    });
  }

  private construireParams(filtre?: FiltreStatistique): HttpParams {
    let params = new HttpParams();
    if (!filtre) return params;

    if (filtre.campagneId) {
      params = params.set('campagneId', filtre.campagneId.toString());
    }
    if (filtre.comptoirId) {
      params = params.set('comptoirId', filtre.comptoirId.toString());
    }
    if (filtre.regionId) {
      params = params.set('regionId', filtre.regionId.toString());
    }
    if (filtre.speculationId) {
      params = params.set('speculationId', filtre.speculationId.toString());
    }
    if (filtre.dateDebut) {
      params = params.set('dateDebut', filtre.dateDebut);
    }
    if (filtre.dateFin) {
      params = params.set('dateFin', filtre.dateFin);
    }

    return params;
  }
}

