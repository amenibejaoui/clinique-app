import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CurrentUserService {
  private readonly baseUrl = `${environment.apiUrl}/me`;

  constructor(private http: HttpClient) {}

  getProfile(): Observable<any> {
    return this.http.get(this.baseUrl);
  }

  getAppointments(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/rendezvous`);
  }

  getConsultations(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/consultations`);
  }

  getInvoices(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/factures`);
  }
}
