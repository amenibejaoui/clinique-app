import { Component, OnInit } from '@angular/core';
import { FactureService } from '../../core/services/facture.service';
import { Facture } from '../../core/models/facture.model';
import { RendezVousService } from '../../core/services/rendez-vous.service';
import { RendezVous } from '../../core/models/rendez-vous.model';
import { Patient } from '../../core/models/patient.model';
import { PatientService } from '../../core/services/patient.service';

type Mode = 'create' | 'edit' | 'view';

@Component({
  selector: 'app-factures',
  templateUrl: './factures.component.html',
  styleUrls: ['./factures.component.scss']
})
export class FacturesComponent implements OnInit {
  factures: Facture[] = [];
  loading = true;
  saving = false;
  searchTerm = '';

  // Modal
  showModal = false;
  mode: Mode = 'create';
  form: Facture = this.emptyForm();
  patientId: number | null = null;
  rendezVousId: number | null = null;
  errorMsg = '';

  rendezVousList: RendezVous[] = [];
  patients: Patient[] = [];

  statuts = ['EN_ATTENTE', 'PARTIELLEMENT_PAYEE', 'PAYEE', 'ANNULEE'];

  constructor(
    private factureService: FactureService,
    private rdvService: RendezVousService,
    private patientService: PatientService
  ) { }

  ngOnInit(): void {
    this.loadFactures();
    this.loadRendezVous();
    this.patientService.getAll().subscribe({
      next: patients => this.patients = patients,
      error: error => this.errorMsg = error?.error?.message || 'Impossible de charger les patients.'
    });
  }

  loadRendezVous(): void {
    this.rdvService.getAll().subscribe({
      next: (d: any) => {
        this.rendezVousList = Array.isArray(d) ? d : (d?.content ?? []);
      },
      error: (e) => this.errorMsg = e?.error?.message || 'Impossible de charger les rendez-vous.'
    });
  }

  // Rendez-vous du patient choisi (tous si aucun patient)
  get rdvDuPatient(): RendezVous[] {
    if (!this.patientId) return this.rendezVousList;
    return this.rendezVousList.filter((r: any) =>
      Number(r.patient?.id ?? r.idPatient ?? r.id_patient ?? r.patientId) === Number(this.patientId)
    );
  }

  onPatientChange(): void {
    this.rendezVousId = null;
  }

  loadFactures(): void {
    this.loading = true;
    this.factureService.getAll().subscribe({
      next: (data) => {
        this.factures = data || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading factures:', err);
        this.errorMsg = err?.error?.message || 'Impossible de charger les factures.';
        this.loading = false;
      }
    });
  }

  get filteredFactures(): Facture[] {
    if (!this.searchTerm) return this.factures;
    const term = this.searchTerm.toLowerCase();
    return this.factures.filter(f => f.numero?.toLowerCase().includes(term));
  }

  get modalTitle(): string {
    return this.mode === 'create' ? 'Ajouter une facture'
      : this.mode === 'edit' ? 'Modifier la facture' : 'Détails de la facture';
  }

  private emptyForm(): Facture {
    const today = new Date().toLocaleDateString('en-CA');
    return {
      numero: '',
      dateEmission: today,
      dateEcheance: today,
      montantHT: 0,
      tauxTVA: 0.19,
      montantTVA: 0,
      montantTTC: 0,
      montantPaye: 0,
      resteAPayer: 0,
      statut: 'EN_ATTENTE'
    };
  }

  // ---------- Ouverture du modal ----------
  openCreate(): void {
    this.mode = 'create';
    this.form = this.emptyForm();
    this.patientId = null;
    this.rendezVousId = null;
    this.errorMsg = '';
    this.showModal = true;
  }

  openEdit(f: Facture): void { this.openWith(f, 'edit'); }
  openView(f: Facture): void { this.openWith(f, 'view'); }

  private openWith(f: Facture, mode: Mode): void {
    this.mode = mode;
    this.form = {
      ...f,
      dateEmission: (f.dateEmission || '').slice(0, 10),
      dateEcheance: (f.dateEcheance || '').slice(0, 10)
    };
    this.patientId = f.patient?.id ?? null;
    this.rendezVousId = f.rendezVous?.id ?? null;
    this.errorMsg = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  // ---------- Enregistrer (création / modification) ----------
  save(): void {
    if (!this.patientId || !this.rendezVousId) {
      this.errorMsg = 'Patient et rendez-vous sont obligatoires.';
      return;
    }
    const payload: any = {
      dateEmission: this.form.dateEmission,
      dateEcheance: this.form.dateEcheance,
      tauxTVA: this.form.tauxTVA
    };
    if (this.mode === 'create') {
      payload.patient = { id: this.patientId };
      payload.rendezVous = { id: this.rendezVousId };
    } else {
      payload.lignes = this.form.lignes || [];
    }

    this.saving = true;
    const request$ = this.mode === 'edit' && this.form.id
      ? this.factureService.update(this.form.id, payload)
      : this.factureService.create(payload);

    request$.subscribe({
      next: () => {
        this.saving = false;
        this.showModal = false;
        this.loadFactures();
      },
      error: (err: any) => {
        console.error('Error saving facture:', err);
        this.errorMsg = err?.error?.message || err?.error?.error
          || (typeof err?.error === 'string' ? err.error : '')
          || `Erreur ${err.status} lors de l'enregistrement.`;
        this.saving = false;
      }
    });
  }

  deleteFacture(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette facture ?')) {
      this.factureService.delete(id).subscribe({
        next: () => this.loadFactures(),
        error: (err) => this.errorMsg = err?.error?.message || 'Impossible de supprimer la facture.'
      });
    }
  }

  badgeClass(statut: string): string {
    switch (statut) {
      case 'PAYEE': return 'bg-success';
      case 'PARTIELLEMENT_PAYEE': return 'bg-warning';
      case 'ANNULEE': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }
}