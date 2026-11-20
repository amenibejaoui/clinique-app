export interface Medecin {
  id?: number;
  nom: string;
  prenom: string;
  specialite?: string;
  tel?: string;
  email?: string;
  numeroOrdre?: string;
  tarifConsultation?: number;
  actif?: boolean;
}
