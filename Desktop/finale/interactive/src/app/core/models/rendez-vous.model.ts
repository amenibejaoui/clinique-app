export interface RendezVous {
  id?: number;
  date: string;         // yyyy-MM-dd, as returned by the Spring API
  duree: number;        // minutes
  heureDebut: string;   // HH:mm ou HH:mm:ss
  motif: string;
  notes?: string;
  statut: string;
  medecin?: { id: number; nom?: string; prenom?: string };
  patient?: { id: number; nom?: string; prenom?: string };
}