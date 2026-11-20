# Clinic Manager - Backend (Spring Boot)

Backend du cahier des charges **Clinic Manager (CLM) v1.0**, construit dans le meme style que vos workshops :
packages `com.sip.clm.*` (`entities`, `controllers`, `services`, `config`, `models`), securite **JWT** avec
`WebSecurityConfigurerAdapter`, `JwtRequestFilter`, `JwtAuthenticationEntryPoint`, `JwtTokenUtil`,
`JwtUserDetailsService`, `JwtRequest` / `JwtResponse`, jjwt 0.9.1, BCrypt.

## Prerequis
- JDK 8 ou superieur (teste pour Java 8 a 21), Maven (ou l'import Maven de STS / Eclipse / IntelliJ)
- **XAMPP** : demarrer MySQL. La base `clinic_manager` est creee automatiquement.

## Configuration (`src/main/resources/application.properties`)
| Parametre | Valeur par defaut |
|---|---|
| Serveur | `http://localhost:9090` |
| MySQL | `jdbc:mysql://localhost:3307/projet`, user `root`, mot de passe vide |
| JWT | secret `jwt.secret`, duree `jwt.expiration` (5 h) |
| CORS | `http://localhost:4200` |
| Emails | `clm.mail.enabled=false` -> messages journalises, sans envoi SMTP |

> Si MySQL ecoute sur un autre port, adaptez `spring.datasource.url`.

## Lancer
```
mvn spring-boot:run
```
ou, dans l'IDE, executer `ClmApplication`. Pour le deploiement : `mvn package` produit `target/clm.jar`
puis `java -jar clm.jar`.

## Comptes crees au premier demarrage
| Role | Email | Mot de passe |
|---|---|---|
| ADMIN | admin@clinic.tn | admin123 |
| SECRETAIRE | secretaire@clinic.tn | secret123 |

## Authentification (comme le workshop 5, partie JWT)
- `POST /api/auth/login` body `{"username":"admin@clinic.tn","password":"admin123"}`
  (`username` = email ; `email` est aussi accepte) -> `{token, id, email, nom, prenom, roles}`
- La creation des comptes est reservee a l'admin depuis `/utilisateurs`. Les comptes médecin/patient doivent être
  reliés à leur fiche clinique. L'endpoint d'inscription n'est pas ouvert aux visiteurs.
- Ensuite : en-tete `Authorization: Bearer <token>` (c'est ce que fait votre `JwtInterceptor` Angular).
  Les rôles possibles sont `ROLE_ADMIN`, `ROLE_SECRETAIRE`, `ROLE_MEDECIN` et `ROLE_PATIENT`.
- 401 : pas de token / token invalide ; 403 : role insuffisant.

## Droits (section 5 du CDC)
| Ressource | ADMIN | SECRETAIRE |
|---|---|---|
| Patients, médecins, rendez-vous, consultations, factures | CRUD | CRUD |
| Dossiers médicaux | GET, PUT | GET, PUT |
| Paiements | GET, POST | GET, POST |
| Utilisateurs | CRUD | aucun acces |
| `/api/stats` | complet | sans chiffre d'affaires |

Les comptes médecin et patient n'accèdent qu'aux routes personnelles `/api/me/**` : rendez-vous et consultations
associés au compte, plus factures pour un patient. L'association compte-fiche est effectuée par l'admin.

## API (section 7.4)
Liste (page/size/search) : `GET /api/patients`, `/api/medecins`, `/api/users` ; `GET /api/rendezvous?date=&medecinId=&statut=` ;
`GET /api/factures?statut=` ; `GET /api/consultations` ; `GET /api/paiements`. Reponse paginee Spring (`content`, `totalElements`, ...).

Autres : `/api/patients/all`, `/api/medecins/all` (listes deroulantes), `/api/patients/{id}/rendezvous|factures|dossier`,
`/api/medecins/{id}/planning?date=`, `PATCH /api/rendezvous/{id}/statut` `{"statut":"CONFIRME"}`,
`GET /api/factures/{id}/pdf`, `POST /api/factures/{id}/paiements`, `PATCH /api/factures/{id}/annuler`,
`GET /api/users/roles`, `GET /api/stats`.

Importer `postman/ClinicManager.postman_collection.json` dans Postman : la requete *Login* enregistre le token
automatiquement dans la variable `token`.

## Regles de gestion implementees (section 6)
1 chevauchement de rendez-vous refuse (409) · 2 pas de rendez-vous dans le passe · 3 cycle de vie
PLANIFIE -> CONFIRME -> EN_COURS -> TERMINE (+ ANNULE / ABSENT) · 4 consultation seulement si EN_COURS/TERMINE ·
5 numero `FAC-AAAA-NNNN` · 6 calculs automatiques (`tauxTVA` est une fraction, ex. `0.19`) · 7 statut de facture automatique ·
8 paiement <= reste a payer · 9 ligne « Consultation » ajoutee avec le tarif du medecin quand la facture est liee a un rendez-vous ·
10 CIN et email uniques · 11 suppression d'un patient/medecin refusee s'il a des rendez-vous ou factures (409).

Emails : activation de compte -> utilisateur, création/modification de rendez-vous -> patient, création de facture et
confirmation de paiement -> patient, alerte quotidienne (8h) des factures en retard -> admin. L'envoi réel nécessite
un SMTP ; définir `CLM_MAIL_ENABLED=true`, `CLM_SMTP_USERNAME`, `CLM_SMTP_PASSWORD`, et si nécessaire `CLM_SMTP_HOST`,
`CLM_SMTP_PORT` et `CLM_ADMIN_EMAIL`. Sans ces identifiants, aucun envoi réel n'est possible ; par défaut les
notifications sont écrites dans les logs. Les identifiants initiaux peuvent être surchargés par
`CLM_INIT_ADMIN_EMAIL`, `CLM_INIT_ADMIN_PASSWORD`, `CLM_INIT_SECRETAIRE_EMAIL` et
`CLM_INIT_SECRETAIRE_PASSWORD`.

## Notes
- Chiffre d'affaires du mois = somme des factures emises ce mois (hors annulees).
- Les entites optionnelles (Ordonnance, LigneOrdonnance, Specialite) ne sont pas incluses ; `specialite` reste un texte dans `Medecin`.
- Spring Boot **2.7.18** (Java 8, meme style `javax.*` / `WebSecurityConfigurerAdapter` que le workshop, qui utilisait 2.3.4).
