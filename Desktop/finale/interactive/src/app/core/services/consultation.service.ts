import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Consultation } from '../models/consultation.model';

@Injectable({ providedIn: 'root' })
export class ConsultationService {
  private baseUrl = `${environment.apiUrl}/consultations`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Consultation[]> {
    return this.http
      .get<any>(`${this.baseUrl}?page=0&size=1000`)
      .pipe(map(res => res.content as Consultation[]));
  }

  get(id: number): Observable<Consultation> {
    return this.http.get<Consultation>(`${this.baseUrl}/${id}`);
  }

  create(consultation: Consultation): Observable<any> {
    return this.http.post(this.baseUrl, consultation);
  }

  update(id: number, consultation: Consultation): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}`, consultation);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
