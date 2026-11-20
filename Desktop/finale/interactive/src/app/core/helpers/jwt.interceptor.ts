import { Injectable } from '@angular/core';
import {
    HttpRequest,
    HttpHandler,
    HttpEvent,
    HttpInterceptor,
} from '@angular/common/http';
import { Observable } from 'rxjs';

import { AuthenticationService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
    constructor(private authenticationService: AuthenticationService) { }

    intercept(
        request: HttpRequest<any>,
        next: HttpHandler
    ): Observable<HttpEvent<any>> {
        if (environment.defaultauth === 'firebase') {
            const currentUser = this.authenticationService.currentUser();
            if (currentUser && currentUser.token) {
                request = request.clone({
                    setHeaders: {
                        Authorization: `Bearer ${currentUser.token}`,
                    },
                });
            }
        } else {
            let token = localStorage.getItem('token');

            if (!token) {
                const storedUser = localStorage.getItem('currentUser');
                if (storedUser) {
                    try {
                        const parsed = JSON.parse(storedUser);
                        token = parsed?.token || parsed?.accessToken || null;
                    } catch {
                        token = null;
                    }
                }
            }

            if (token) {
                request = request.clone({
                    setHeaders: {
                        Authorization: `Bearer ${token}`,
                    },
                });
            }
        }

        return next.handle(request);
    }
}