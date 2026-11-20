import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { Patient } from '../models/patient.model';

@Injectable({ providedIn: 'root' })
export class PatientService {
  private baseUrl = `${environment.apiUrl}/patients`;

  constructor(private http: HttpClient) {}

  /** Get all patients (Spring returns Page with `content` array) */
  getAll(): Observable<Patient[]> {
    return this.http
      .get<any>(`${this.baseUrl}?page=0&size=1000`)
      .pipe(map(res => res.content as Patient[]));
  }

  get(id: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.baseUrl}/${id}`);
  }

  create(patient: Patient): Observable<any> {
    return this.http.post(this.baseUrl, patient);
  }

  update(id: number, patient: Patient): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}`, patient);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
