export interface Facture {
  id?: number;
  numero: string;
  dateEmission: string;
  dateEcheance: string;
  montantHT: number;
  tauxTVA: number;
  montantTVA: number;
  montantTTC: number;
  montantPaye: number;
  resteAPayer: number;
  statut: string;
  lignes?: Array<{ designation: string; quantite: number; prixUnitaire: number; totalLigne?: number }>;
  patient?: { id: number; nom?: string; prenom?: string };
  rendezVous?: { id: number; medecin?: { nom?: string; prenom?: string } };
}
