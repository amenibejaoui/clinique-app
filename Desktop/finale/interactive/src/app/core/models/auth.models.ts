export class User {
  id?: number;
  email?: string;
  password?: string;
  nom?: string;
  prenom?: string;
  tel?: string;
  actif?: boolean;
  roles?: Array<{ id?: number; libelle: string }>;
  patient?: { id?: number };
  medecin?: { id?: number };
  token?: string;
}
