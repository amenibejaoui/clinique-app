import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { map } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { Medecin } from '../models/medecin.model';

@Injectable({ providedIn: 'root' })
export class MedecinService {
  private baseUrl = `${environment.apiUrl}/medecins`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Medecin[]> {
    return this.http
      .get<any>(`${this.baseUrl}?page=0&size=1000`)
      .pipe(map(res => res.content as Medecin[]));
  }

  get(id: number): Observable<Medecin> {
    return this.http.get<Medecin>(`${this.baseUrl}/${id}`);
  }

  create(medecin: Medecin): Observable<any> {
    return this.http.post(this.baseUrl, medecin);
  }

  update(id: number, medecin: Medecin): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}`, medecin);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
