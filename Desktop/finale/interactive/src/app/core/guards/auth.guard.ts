import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

// Auth Services
import { AuthenticationService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
    constructor(
        private router: Router,
        private authenticationService: AuthenticationService
    ) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        const storedUser = localStorage.getItem('currentUser');
        let user: any = null;
        if (storedUser) {
            try {
                user = JSON.parse(storedUser);
            } catch {
                localStorage.removeItem('currentUser');
                localStorage.removeItem('token');
            }
        }

        if (environment.defaultauth === 'firebase') {
            const currentUser = this.authenticationService.currentUser();
            if (currentUser) {
                return true;
            }
        }

        if (!user) {
            this.router.navigate(['/auth/login'], { queryParams: { returnUrl: state.url } });
            return false;
        }

        const roles: string[] = user.roles || [];
        const isAdmin = roles.includes('ROLE_ADMIN');
        const hasClinicalAccess = isAdmin || roles.includes('ROLE_SECRETAIRE');
        const isPersonalAccount = roles.includes('ROLE_MEDECIN') || roles.includes('ROLE_PATIENT');

        if (state.url.startsWith('/utilisateurs') && !isAdmin) {
            return this.router.parseUrl('/');
        }
        if (isPersonalAccount && !state.url.startsWith('/mon-espace')) {
            return this.router.parseUrl('/mon-espace');
        }
        if (!hasClinicalAccess && !isPersonalAccount) {
            this.authenticationService.logout();
            return this.router.parseUrl('/auth/login');
        }
        return true;
    }
}
