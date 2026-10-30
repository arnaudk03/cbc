import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../core/auth.models';
import { DashboardResponse } from './dashboard.models';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);

  charger(campagneId?: number): Observable<DashboardResponse> {
    const url = campagneId
      ? `${environment.apiUrl}/dashboard?campagneId=${campagneId}`
      : `${environment.apiUrl}/dashboard`;
    return this.http
      .get<ApiResponse<DashboardResponse>>(url)
      .pipe(map((reponse) => reponse.data));
  }
}
