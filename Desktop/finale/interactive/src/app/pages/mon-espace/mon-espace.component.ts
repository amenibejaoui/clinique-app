import { Component, OnInit } from '@angular/core';
import { CurrentUserService } from '../../core/services/current-user.service';

@Component({
  selector: 'app-mon-espace',
  templateUrl: './mon-espace.component.html'
})
export class MonEspaceComponent implements OnInit {
  profile: any;
  appointments: any[] = [];
  consultations: any[] = [];
  invoices: any[] = [];
  isPatient = false;
  loading = true;
  errorMessage = '';

  constructor(private currentUserService: CurrentUserService) {}

  ngOnInit(): void {
    try {
      const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}');
      this.isPatient = (currentUser.roles || []).includes('ROLE_PATIENT');
    } catch {
      this.errorMessage = 'Impossible de lire la session. Veuillez vous reconnecter.';
      this.loading = false;
      return;
    }

    this.currentUserService.getProfile().subscribe({
      next: profile => this.profile = profile,
      error: error => this.showError(error)
    });
    this.currentUserService.getAppointments().subscribe({
      next: appointments => {
        this.appointments = appointments || [];
        this.loading = false;
      },
      error: error => this.showError(error)
    });
    this.currentUserService.getConsultations().subscribe({
      next: consultations => this.consultations = consultations || [],
      error: error => this.showError(error)
    });
    if (this.isPatient) {
      this.currentUserService.getInvoices().subscribe({
        next: invoices => this.invoices = invoices || [],
        error: error => this.showError(error)
      });
    }
  }

  private showError(error: any): void {
    this.errorMessage = error?.error?.message || 'Impossible de charger votre espace personnel.';
    this.loading = false;
  }
}
