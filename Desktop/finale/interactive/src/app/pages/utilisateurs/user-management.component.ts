import { Component, OnInit } from '@angular/core';
import { User } from '../../core/models/auth.models';
import { Patient } from '../../core/models/patient.model';
import { Medecin } from '../../core/models/medecin.model';
import { Role } from '../../core/models/role.model';
import { PatientService } from '../../core/services/patient.service';
import { MedecinService } from '../../core/services/medecin.service';
import { UserProfileService } from '../../core/services/user.service';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html'
})
export class UserManagementComponent implements OnInit {
  users: User[] = [];
  roles: Role[] = [];
  patients: Patient[] = [];
  medecins: Medecin[] = [];
  loading = true;
  saving = false;
  errorMessage = '';
  showForm = false;
  selectedId?: number;
  role = 'SECRETAIRE';
  patientId: number | null = null;
  medecinId: number | null = null;
  form: User = this.emptyForm();

  constructor(
    private usersService: UserProfileService,
    private patientService: PatientService,
    private medecinService: MedecinService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.usersService.getRoles().subscribe({
      next: roles => this.roles = roles,
      error: error => this.showError(error)
    });
    this.patientService.getAll().subscribe({
      next: patients => this.patients = patients,
      error: error => this.showError(error)
    });
    this.medecinService.getAll().subscribe({
      next: medecins => this.medecins = medecins,
      error: error => this.showError(error)
    });
  }

  private emptyForm(): User {
    return { email: '', nom: '', prenom: '', tel: '', actif: true, password: '' };
  }

  loadUsers(): void {
    this.loading = true;
    this.usersService.getAll().subscribe({
      next: users => {
        this.users = users || [];
        this.loading = false;
      },
      error: error => {
        this.showError(error);
        this.loading = false;
      }
    });
  }

  create(): void {
    this.selectedId = undefined;
    this.form = this.emptyForm();
    this.role = 'SECRETAIRE';
    this.patientId = null;
    this.medecinId = null;
    this.errorMessage = '';
    this.showForm = true;
  }

  edit(user: User): void {
    this.selectedId = user.id;
    this.form = { ...user, password: '' };
    this.role = user.roles?.[0]?.libelle || 'SECRETAIRE';
    this.patientId = user.patient?.id || null;
    this.medecinId = user.medecin?.id || null;
    this.errorMessage = '';
    this.showForm = true;
  }

  save(): void {
    if (!this.form.email || !this.form.nom || !this.form.prenom
      || (!this.selectedId && (!this.form.password || this.form.password.length < 6))) {
      this.errorMessage = 'Nom, prénom, email et un mot de passe de 6 caractères minimum sont requis.';
      return;
    }
    if (this.role === 'PATIENT' && !this.patientId || this.role === 'MEDECIN' && !this.medecinId) {
      this.errorMessage = 'Associez une fiche patient ou médecin au compte.';
      return;
    }

    const payload: User = {
      ...this.form,
      roles: [{ libelle: this.role }],
      patient: this.role === 'PATIENT' ? { id: this.patientId! } : undefined,
      medecin: this.role === 'MEDECIN' ? { id: this.medecinId! } : undefined
    };
    if (!payload.password) {
      delete payload.password;
    }

    this.saving = true;
    const request = this.selectedId
      ? this.usersService.update(this.selectedId, payload)
      : this.usersService.create(payload);
    request.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.loadUsers();
      },
      error: error => {
        this.showError(error);
        this.saving = false;
      }
    });
  }

  delete(user: User): void {
    if (!user.id || !confirm(`Supprimer le compte ${user.email} ?`)) {
      return;
    }
    this.usersService.delete(user.id).subscribe({
      next: () => this.loadUsers(),
      error: error => this.showError(error)
    });
  }

  onRoleChange(): void {
    this.patientId = null;
    this.medecinId = null;
  }

  private showError(error: any): void {
    this.errorMessage = error?.error?.message || 'Une erreur est survenue pendant la communication avec le serveur.';
  }
}
