import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { MedecinService } from '../../core/services/medecin.service';
import { Medecin } from '../../core/models/medecin.model';

@Component({
  selector: 'app-medecins',
  templateUrl: './medecins.component.html',
  styleUrls: ['./medecins.component.scss']
})
export class MedecinsComponent implements OnInit {
  medecins: Medecin[] = [];
  loading = true;
  searchTerm = '';
  medecinForm!: UntypedFormGroup;
  isEditMode = false;
  selectedMedecinId?: number;
  errorMessage = '';

  constructor(
    private medecinService: MedecinService,
    private fb: UntypedFormBuilder,
    private modalService: NgbModal
  ) { }

  ngOnInit(): void {
    this.initForm();
    this.loadMedecins();
  }

  initForm(): void {
    this.medecinForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      specialite: [''],
      tel: ['', [Validators.pattern('^[+0-9 ]{8,15}$')]],
      email: ['', [Validators.email]],
      numeroOrdre: [''],
      tarifConsultation: [0, [Validators.min(0)]],
      actif: [true]
    });
  }

  loadMedecins(): void {
    this.loading = true;
    this.medecinService.getAll().subscribe({
      next: (data) => {
        this.medecins = data || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading medecins:', err);
        this.errorMessage = err?.error?.message || 'Impossible de charger les médecins.';
        this.loading = false;
      }
    });
  }

  get filteredMedecins(): Medecin[] {
    if (!this.searchTerm) return this.medecins;
    const term = this.searchTerm.toLowerCase();
    return this.medecins.filter(m =>
      (m.prenom?.toLowerCase().includes(term)) ||
      (m.nom?.toLowerCase().includes(term)) ||
      (m.specialite?.toLowerCase().includes(term)) ||
      (m.tel?.toLowerCase().includes(term))
    );
  }

  openAddModal(content: any): void {
    this.errorMessage = '';
    this.isEditMode = false;
    this.selectedMedecinId = undefined;
    this.initForm();
    this.modalService.open(content, { centered: true, size: 'lg' });
  }

  openEditModal(content: any, medecin: Medecin): void {
    this.errorMessage = '';
    this.isEditMode = true;
    this.selectedMedecinId = medecin.id;
    this.medecinForm.patchValue({
      nom: medecin.nom,
      prenom: medecin.prenom,
      specialite: medecin.specialite || '',
      tel: medecin.tel || '',
      email: medecin.email || '',
      numeroOrdre: medecin.numeroOrdre || '',
      tarifConsultation: medecin.tarifConsultation ?? 0,
      actif: medecin.actif ?? true
    });
    this.modalService.open(content, { centered: true, size: 'lg' });
  }

  saveMedecin(modal: any): void {
    if (this.medecinForm.invalid) return;

    if (this.isEditMode && this.selectedMedecinId != null) {
      this.medecinService.update(this.selectedMedecinId, this.medecinForm.value).subscribe({
        next: () => {
          modal.close();
          this.loadMedecins();
        },
        error: (err) => this.errorMessage = err?.error?.message || 'Impossible de modifier le médecin.'
      });
    } else {
      this.medecinService.create(this.medecinForm.value).subscribe({
        next: () => {
          modal.close();
          this.loadMedecins();
        },
        error: (err) => this.errorMessage = err?.error?.message || 'Impossible de créer le médecin.'
      });
    }
  }

  deleteMedecin(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce médecin ?')) {
      this.medecinService.delete(id).subscribe({
        next: () => this.loadMedecins(),
        error: (err) => this.errorMessage = err?.error?.message || 'Impossible de supprimer le médecin.'
      });
    }
  }
}