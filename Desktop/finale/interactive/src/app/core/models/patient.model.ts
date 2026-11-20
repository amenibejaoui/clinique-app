export interface Patient {
  id?: number;
  nom: string;
  prenom: string;
  cin?: string;
  dateNaissance?: string;
  dateInscription?: string;
  sexe?: 'HOMME' | 'FEMME' | string; // <-- Remplacez "MASCULIN" | "FEMININ" ici
  groupeSanguin?: string;
  numeroAssurance?: string;
  tel?: string;
  email?: string;
  adresse?: string;
  ville?: string;
}