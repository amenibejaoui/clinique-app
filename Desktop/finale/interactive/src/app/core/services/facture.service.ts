import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Facture } from '../models/facture.model';

@Injectable({ providedIn: 'root' })
export class FactureService {
  private baseUrl = `${environment.apiUrl}/factures`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Facture[]> {
    return this.http
      .get<any>(`${this.baseUrl}?page=0&size=1000`)
      .pipe(map(res => res.content as Facture[]));
  }

  get(id: number): Observable<Facture> {
    return this.http.get<Facture>(`${this.baseUrl}/${id}`);
  }

  create(facture: Facture): Observable<any> {
    return this.http.post(this.baseUrl, facture);
  }

  update(id: number, facture: Facture): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}`, facture);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
