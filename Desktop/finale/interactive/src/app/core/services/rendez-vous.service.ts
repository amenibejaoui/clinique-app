import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { RendezVous } from '../models/rendez-vous.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class RendezVousService {
    private apiUrl = `${environment.apiUrl}/rendezvous`;

    constructor(private http: HttpClient) { }

    // Accepte une réponse en tableau OU paginée ({ content: [...] })
    getAll(): Observable<RendezVous[]> {
        return this.http.get<any>(`${this.apiUrl}?page=0&size=1000`).pipe(
            map(r => Array.isArray(r) ? r : (r?.content ?? []))
        );
    }
    create(r: RendezVous): Observable<RendezVous> { return this.http.post<RendezVous>(this.apiUrl, r); }
    update(id: number, r: RendezVous): Observable<RendezVous> { return this.http.put<RendezVous>(`${this.apiUrl}/${id}`, r); }
    changeStatus(id: number, statut: string): Observable<RendezVous> {
        return this.http.patch<RendezVous>(`${this.apiUrl}/${id}/statut`, { statut });
    }
    delete(id: number): Observable<void> { return this.http.delete<void>(`${this.apiUrl}/${id}`); }
}