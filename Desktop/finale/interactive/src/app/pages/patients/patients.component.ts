import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { PatientService } from '../../core/services/patient.service';
import { Patient } from '../../core/models/patient.model';

@Component({
  selector: 'app-patients',
  templateUrl: './patients.component.html',
  styleUrls: ['./patients.component.scss']
})
export class PatientsComponent implements OnInit {
  patients: Patient[] = [];
  loading = true;
  searchTerm = '';
  patientForm!: UntypedFormGroup;
  isEditMode = false;
  selectedPatientId?: number;
  errorMessage = '';

  constructor(
    private patientService: PatientService,
    private fb: UntypedFormBuilder,
    private modalService: NgbModal
  ) { }

  ngOnInit(): void {
    this.initForm();
    this.loadPatients();
  }

  initForm(): void {
    const today = new Date().toISOString().split('T')[0];
    this.patientForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      cin: [''],
      dateNaissance: [''],
      dateInscription: [today],
      sexe: [''],
      groupeSanguin: [''],
      numeroAssurance: [''],
      tel: [''],
      email: ['', [Validators.email]],
      adresse: [''],
      ville: ['']
    });
  }

  loadPatients(): void {
    this.loading = true;
    this.patientService.getAll().subscribe({
      next: (data) => {
        this.patients = data || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading patients:', err);
        this.errorMessage = err?.error?.message || 'Impossible de charger les patients.';
        this.loading = false;
      }
    });
  }

  get filteredPatients(): Patient[] {
    if (!this.searchTerm) return this.patients;
    const term = this.searchTerm.toLowerCase();
    return this.patients.filter(p =>
      (p.prenom?.toLowerCase().includes(term)) ||
      (p.nom?.toLowerCase().includes(term)) ||
      (p.cin?.toLowerCase().includes(term)) ||
      (p.email?.toLowerCase().includes(term)) ||
      (p.tel?.toLowerCase().includes(term)) ||
      (p.ville?.toLowerCase().includes(term))
    );
  }

  openAddModal(content: any): void {
    this.errorMessage = '';
    this.isEditMode = false;
    this.selectedPatientId = undefined;
    this.initForm();
    this.modalService.open(content, { centered: true, size: 'lg' });
  }

  openEditModal(content: any, patient: Patient): void {
    this.errorMessage = '';
    this.isEditMode = true;
    this.selectedPatientId = patient.id;
    this.patientForm.patchValue({
      nom: patient.nom || '',
      prenom: patient.prenom || '',
      cin: patient.cin || '',
      dateNaissance: patient.dateNaissance || '',
      dateInscription: patient.dateInscription || '',
      sexe: patient.sexe || '',
      groupeSanguin: patient.groupeSanguin || '',
      numeroAssurance: patient.numeroAssurance || '',
      tel: patient.tel || '',
      email: patient.email || '',
      adresse: patient.adresse || '',
      ville: patient.ville || ''
    });
    this.modalService.open(content, { centered: true, size: 'lg' });
  }

  savePatient(modal: any): void {
    if (this.patientForm.invalid) return;

    const payload = { ...this.patientForm.value };
    if (!payload.dateNaissance) delete payload.dateNaissance;
    if (!payload.dateInscription) delete payload.dateInscription;
    if (!payload.sexe) delete payload.sexe;
    if (!payload.groupeSanguin) delete payload.groupeSanguin;

    if (this.isEditMode && this.selectedPatientId != null) {
      this.patientService.update(this.selectedPatientId, payload).subscribe({
        next: () => {
          modal.close();
          this.loadPatients();
        },
        error: (err) => this.errorMessage = err?.error?.message || 'Impossible de modifier le patient.'
      });
    } else {
      this.patientService.create(payload).subscribe({
        next: () => {
          modal.close();
          this.loadPatients();
        },
        error: (err) => this.errorMessage = err?.error?.message || 'Impossible de créer le patient.'
      });
    }
  }

  deletePatient(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce patient ?')) {
      this.patientService.delete(id).subscribe({
        next: () => this.loadPatients(),
        error: (err) => this.errorMessage = err?.error?.message || 'Impossible de supprimer le patient.'
      });
    }
  }
}