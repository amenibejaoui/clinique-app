export interface Consultation {
  id?: number;
  dateConsultation?: string;
  diagnostic?: string;
  observations?: string;
  poids?: number;
  taille?: number;
  tension?: string;
  temperature?: number;
  traitementPrescrit?: string;
  rendezVous?: any;
}
