import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../core/auth.models';
import { PageResponse } from '../../core/api.models';
import { Notification } from './notification.models';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/notifications`;

  lister(page = 0, size = 20): Observable<PageResponse<Notification>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http
      .get<ApiResponse<PageResponse<Notification>>>(this.baseUrl, { params })
      .pipe(map((reponse) => reponse.data));
  }

  marquerLue(id: number): Observable<Notification> {
    return this.http
      .patch<ApiResponse<Notification>>(`${this.baseUrl}/${id}/lecture`, {})
      .pipe(map((reponse) => reponse.data));
  }
}
