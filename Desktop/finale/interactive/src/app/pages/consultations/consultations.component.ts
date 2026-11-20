import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ConsultationService } from '../../core/services/consultation.service';
import { RendezVousService } from '../../core/services/rendez-vous.service';
import { RendezVous } from '../../core/models/rendez-vous.model';

@Component({
  selector: 'app-consultations',
  templateUrl: './consultations.component.html',
  styleUrls: ['./consultations.component.scss']
})
export class ConsultationsComponent implements OnInit {
  consultations: any[] = [];
  loading = true;
  searchTerm = '';
  consultationForm!: UntypedFormGroup;
  isEditMode = false;
  selectedConsultationId?: number;
  rendezVousList: RendezVous[] = [];
  errorMessage = '';

  // Statuts de rendez-vous pour lesquels on peut créer une consultation
  readonly statutsAutorises: string[] = ['PLANIFIE', 'EN_COURS', 'TERMINE'];

  constructor(
    private consultationService: ConsultationService,
    private rendezVousService: RendezVousService,
    private fb: UntypedFormBuilder,
    private modalService: NgbModal
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadConsultations();
    this.loadRendezVous();
  }

  loadRendezVous(): void {
    this.rendezVousService.getAll().subscribe({
      next: rendezVous => this.rendezVousList = rendezVous || [],
      error: error => this.errorMessage = error?.error?.message || 'Impossible de charger les rendez-vous.'
    });
  }

  isRendezVousSelectable(rdv: RendezVous): boolean {
    return this.statutsAutorises.includes(rdv.statut as string);
  }

  initForm(): void {
    this.consultationForm = this.fb.group({
      dateConsultation: ['', Validators.required],
      diagnostic: ['', Validators.required],
      observations: [''],
      poids: [null],
      taille: [null],
      temperature: [null],
      tension: [''],
      traitementPrescrit: [''],
      idRendezVous: [null, Validators.required]
    });
  }

  loadConsultations(): void {
    this.loading = true;
    this.consultationService.getAll().subscribe({
      next: (data) => {
        this.consultations = data || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading consultations:', err);
        this.errorMessage = err?.error?.message || 'Impossible de charger les consultations.';
        this.loading = false;
      }
    });
  }

  get filteredConsultations(): any[] {
    if (!this.searchTerm) return this.consultations;
    const term = this.searchTerm.toLowerCase();
    return this.consultations.filter(c =>
      (c.diagnostic?.toLowerCase().includes(term)) ||
      (c.observations?.toLowerCase().includes(term)) ||
      (c.traitementPrescrit?.toLowerCase().includes(term)) ||
      (c.tension?.toLowerCase().includes(term)) ||
      (c.rendezVous?.patient?.nom?.toLowerCase().includes(term)) ||
      (c.rendezVous?.patient?.prenom?.toLowerCase().includes(term)) ||
      (c.rendezVous?.medecin?.nom?.toLowerCase().includes(term))
    );
  }

  openAddModal(content: any): void {
    this.isEditMode = false;
    this.selectedConsultationId = undefined;
    this.errorMessage = '';
    this.initForm();
    this.loadRendezVous(); // liste à jour à chaque ouverture
    this.modalService.open(content, { centered: true, size: 'lg' });
  }

  openEditModal(content: any, c: any): void {
    this.isEditMode = true;
    this.selectedConsultationId = c.id;
    this.errorMessage = '';
    const formattedDate = c.dateConsultation
      ? new Date(c.dateConsultation).toISOString().slice(0, 16)
      : '';

    this.consultationForm.patchValue({
      dateConsultation: formattedDate,
      diagnostic: c.diagnostic || '',
      observations: c.observations || '',
      poids: c.poids ?? null,
      taille: c.taille ?? null,
      temperature: c.temperature ?? null,
      tension: c.tension || '',
      traitementPrescrit: c.traitementPrescrit || '',
      idRendezVous: c.idRendezVous || c.rendezVous?.id || null
    });
    this.modalService.open(content, { centered: true, size: 'lg' });
  }

  saveConsultation(modal: any): void {
    if (this.consultationForm.invalid) return;

    const raw = this.consultationForm.value;
    const payload: any = {
      dateConsultation: raw.dateConsultation,
      diagnostic: raw.diagnostic,
      observations: raw.observations || null,
      poids: raw.poids !== '' ? raw.poids : null,
      taille: raw.taille !== '' ? raw.taille : null,
      temperature: raw.temperature !== '' ? raw.temperature : null,
      tension: raw.tension || null,
      traitementPrescrit: raw.traitementPrescrit || null
    };

    if (raw.idRendezVous) {
      payload.idRendezVous = Number(raw.idRendezVous);
      payload.rendezVous = { id: Number(raw.idRendezVous) };
    }

    if (this.isEditMode && this.selectedConsultationId != null) {
      this.consultationService.update(this.selectedConsultationId, payload).subscribe({
        next: () => {
          modal.close();
          this.loadConsultations();
        },
        error: (err) => this.errorMessage = err?.error?.message || 'Impossible de modifier la consultation.'
      });
    } else {
      this.consultationService.create(payload).subscribe({
        next: () => {
          modal.close();
          this.loadConsultations();
        },
        error: (err) => this.errorMessage = err?.error?.message || 'Impossible de créer la consultation.'
      });
    }
  }

  deleteConsultation(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette consultation ?')) {
      this.consultationService.delete(id).subscribe({
        next: () => this.loadConsultations(),
        error: (err) => this.errorMessage = err?.error?.message || 'Impossible de supprimer la consultation.'
      });
    }
  }
}