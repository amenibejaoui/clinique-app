import { Injectable } from '@angular/core';
import { getFirebaseBackend } from '../../authUtils';
import { User } from '../models/auth.models';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';

import { GlobalComponent } from "../../global-component";

const AUTH_API = GlobalComponent.AUTH_API;

const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

export interface RegisterPayload {
    email: string;
    password: string;
    nom: string;
    prenom: string;
    tel?: string;
}

@Injectable({ providedIn: 'root' })

/**
 * Auth-service Component
 */
export class AuthenticationService {

    user!: User;
    currentUserValue: any;
    private currentUserSubject: BehaviorSubject<User>;

    constructor(private http: HttpClient) {
        const storedUser = localStorage.getItem('currentUser');
        this.currentUserSubject = new BehaviorSubject<User>(storedUser ? JSON.parse(storedUser) : null!);
    }

    /**
     * Performs the register
     */
    register(payloadOrEmail: RegisterPayload | string, first_name?: string, password?: string): Observable<any> {
        let body: RegisterPayload;

        if (typeof payloadOrEmail === 'object') {
            body = {
                email: payloadOrEmail.email?.trim(),
                password: payloadOrEmail.password,
                nom: payloadOrEmail.nom?.trim(),
                prenom: payloadOrEmail.prenom?.trim(),
                ...(payloadOrEmail.tel && payloadOrEmail.tel.trim() !== ''
                    ? { tel: payloadOrEmail.tel.trim() }
                    : {})
            };
        } else {
            const rawName = (first_name || '').trim();
            const parts = rawName.split(/\s+/);
            body = {
                email: payloadOrEmail.trim(),
                password: password || '',
                nom: parts[0] || rawName || 'Utilisateur',
                prenom: parts.slice(1).join(' ') || parts[0] || 'CLM'
            };
        }

        return this.http.post(AUTH_API + 'register', body, httpOptions);
    }

    /**
     * Performs the auth
     */
    login(email: string, password: string): Observable<any> {
        return this.http.post(AUTH_API + 'login', {
            email,
            password
        }, httpOptions);
    }

    /**
     * Returns the current user
     */
    public currentUser(): any {
        return getFirebaseBackend()!.getAuthenticatedUser();
    }

    /**
     * Logout the user
     */
    logout() {
        localStorage.removeItem('currentUser');
        localStorage.removeItem('token');
        this.currentUserSubject.next(null!);
    }

    /**
     * Reset password
     */
    resetPassword(email: string) {
        return getFirebaseBackend()!.forgetPassword(email).then((response: any) => {
            const message = response.data;
            return message;
        });
    }
}

