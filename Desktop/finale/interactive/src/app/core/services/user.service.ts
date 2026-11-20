import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { User } from '../models/auth.models';
import { Role } from '../models/role.model';

@Injectable({ providedIn: 'root' })
export class UserProfileService {
    private baseUrl = `${environment.apiUrl}/users`;
    constructor(private http: HttpClient) { }
    /***
     * Get all users (Spring returns a Page object with a `content` array)
     */
    getAll(): Observable<User[]> {
        // request first page with a large size to get all rows
        return this.http.get<any>(`${this.baseUrl}?page=0&size=1000`).pipe(
            map(res => res.content as User[])
        );
    }

    getRoles(): Observable<Role[]> {
        return this.http.get<Role[]>(`${this.baseUrl}/roles`);
    }

    create(user: User): Observable<User> {
        return this.http.post<User>(this.baseUrl, user);
    }

    update(id: number, user: User): Observable<User> {
        return this.http.put<User>(`${this.baseUrl}/${id}`, user);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/${id}`);
    }
}
