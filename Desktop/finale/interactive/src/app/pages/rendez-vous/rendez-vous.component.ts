import { Component, OnInit } from '@angular/core';
import { RendezVousService } from '../../core/services/rendez-vous.service';
import { RendezVous } from '../../core/models/rendez-vous.model';
import { PatientService } from '../../core/services/patient.service';
import { MedecinService } from '../../core/services/medecin.service';
import { Patient } from '../../core/models/patient.model';
import { Medecin } from '../../core/models/medecin.model';
import { concatMap } from 'rxjs/operators';
import { of } from 'rxjs';

type Mode = 'create' | 'edit' | 'view';

@Component({
  selector: 'app-rendez-vous',
  templateUrl: './rendez-vous.component.html',
  styleUrls: ['./rendez-vous.component.scss']
})
export class RendezVousComponent implements OnInit {
  rendezVous: RendezVous[] = [];
  loading = true;
  saving = false;
  searchTerm = '';

  showModal = false;
  mode: Mode = 'create';
  form: RendezVous = this.emptyForm();
  medecinId: number | null = null;
  patientId: number | null = null;
  errorMsg = '';
  originalStatus?: string;
  patients: Patient[] = [];
  medecins: Medecin[] = [];

  // Valeurs de l'enum Java (gardez seulement celles qui existent chez vous)
  statuts = ['PLANIFIE', 'CONFIRME', 'EN_COURS', 'TERMINE', 'ANNULE', 'ABSENT'];

  constructor(
    private rdvService: RendezVousService,
    private patientService: PatientService,
    private medecinService: MedecinService
  ) { }

  ngOnInit(): void {
    this.load();
    this.patientService.getAll().subscribe({
      next: patients => this.patients = patients,
      error: err => this.errorMsg = err?.error?.message || 'Impossible de charger les patients.'
    });
    this.medecinService.getAll().subscribe({
      next: medecins => this.medecins = medecins,
      error: err => this.errorMsg = err?.error?.message || 'Impossible de charger les médecins.'
    });
  }

  load(): void {
    this.loading = true;
    this.rdvService.getAll().subscribe({
      next: (data) => { this.rendezVous = data || []; this.loading = false; },
      error: (err) => {
        console.error('Error loading rendez-vous:', err);
        this.rendezVous = [];
        this.loading = false;
      }
    });
  }

  get filtered(): RendezVous[] {
    if (!this.searchTerm) return this.rendezVous;
    const t = this.searchTerm.toLowerCase();
    return this.rendezVous.filter(r =>
      (r.motif || '').toLowerCase().includes(t) ||
      (r.statut || '').toLowerCase().includes(t) ||
      (r.patient?.nom || '').toLowerCase().includes(t) ||
      (r.medecin?.nom || '').toLowerCase().includes(t)
    );
  }

  get modalTitle(): string {
    return this.mode === 'create' ? 'Ajouter un rendez-vous'
      : this.mode === 'edit' ? 'Modifier le rendez-vous' : 'Détails du rendez-vous';
  }

  private emptyForm(): RendezVous {
    return {
      date: new Date().toLocaleDateString('en-CA'),
      duree: 30,
      heureDebut: '09:00',
      motif: '',
      notes: '',
      statut: 'PLANIFIE'
    };
  }

  openCreate(): void {
    this.mode = 'create';
    this.form = this.emptyForm();
    this.medecinId = null;
    this.patientId = null;
    this.errorMsg = '';
    this.showModal = true;
  }

  openEdit(r: RendezVous): void { this.openWith(r, 'edit'); }
  openView(r: RendezVous): void { this.openWith(r, 'view'); }

  private openWith(r: RendezVous, mode: Mode): void {
    this.mode = mode;
    this.form = {
      ...r,
      date: (r.date || '').slice(0, 10),
      heureDebut: (r.heureDebut || '').slice(0, 5)
    };
    const any: any = r;
    this.medecinId = r.medecin?.id ?? any.idMedecin ?? null;
    this.patientId = r.patient?.id ?? any.idPatient ?? null;
    this.originalStatus = r.statut;
    this.errorMsg = '';
    this.showModal = true;
  }

  closeModal(): void { this.showModal = false; }

  save(): void {
    if (!this.form.date || !this.form.heureDebut || !this.form.motif?.trim()
      || !this.medecinId || !this.patientId) {
      this.errorMsg = 'Date, heure, motif, médecin et patient sont obligatoires.';
      return;
    }
    const payload: any = {
      ...this.form,
      notes: this.form.notes || null,
      medecin: { id: this.medecinId },
      patient: { id: this.patientId }
    };

    this.saving = true;
    const request$ = this.mode === 'edit' && this.form.id
      ? this.rdvService.update(this.form.id, payload).pipe(
        concatMap(updated => this.originalStatus !== this.form.statut
          ? this.rdvService.changeStatus(this.form.id!, this.form.statut)
          : of(updated))
      )
      : this.rdvService.create(payload);

    request$.subscribe({
      next: () => { this.saving = false; this.showModal = false; this.load(); },
      error: (err: any) => {
        console.error('Error saving rendez-vous:', err);
        this.errorMsg = typeof err === 'string' ? err
          : err?.error?.errors ? JSON.stringify(err.error.errors)
            : err?.error?.message || err?.error?.error
            || (typeof err?.error === 'string' ? err.error : '')
            || err?.message
            || 'Erreur lors de l\'enregistrement.';
        this.saving = false;
      }
    });
  }

  deleteRdv(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce rendez-vous ?')) {
      this.rdvService.delete(id).subscribe({
        next: () => this.load(),
        error: (err) => {
          console.error('Error deleting rendez-vous:', err);
          alert('Suppression impossible (ce rendez-vous est peut-être lié à une facture).');
        }
      });
    }
  }

  badgeClass(statut: string): string {
    switch (statut) {
      case 'CONFIRME': return 'bg-success';
      case 'EN_COURS': return 'bg-warning';
      case 'TERMINE': return 'bg-info';
      case 'ABSENT': return 'bg-dark';
      case 'ANNULE': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }
}