package com.sip.clm.entities;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import javax.validation.constraints.*;

@Entity
@Table(name = "patients")
public class Patient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Le nom est obligatoire")
	private String nom;

	@NotBlank(message = "Le prénom est obligatoire")
	private String prenom;

	@Past(message = "La date de naissance doit être dans le passé")
	private LocalDate dateNaissance;

	@Enumerated(EnumType.STRING)
	private Sexe sexe;

	@Pattern(regexp = "^[0-9]{8}$", message = "Le CIN doit contenir 8 chiffres")
	@Column(unique = true)
	private String cin;

	@Pattern(regexp = "^[+0-9 ]{8,15}$", message = "Téléphone invalide")
	private String tel;

	@Email(message = "Format d'email invalide")
	private String email;

	private String adresse;

	private String ville;

	private String groupeSanguin;

	private String numeroAssurance;

	private LocalDate dateInscription;

	@JsonIgnore
	@OneToOne(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
	private DossierMedical dossierMedical;

	public Patient() {
	}

	@PrePersist
	protected void onCreate() {
		if (dateInscription == null) {
			dateInscription = LocalDate.now();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public LocalDate getDateNaissance() {
		return dateNaissance;
	}

	public void setDateNaissance(LocalDate dateNaissance) {
		this.dateNaissance = dateNaissance;
	}

	public Sexe getSexe() {
		return sexe;
	}

	public void setSexe(Sexe sexe) {
		this.sexe = sexe;
	}

	public String getCin() {
		return cin;
	}

	public void setCin(String cin) {
		this.cin = cin;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public String getVille() {
		return ville;
	}

	public void setVille(String ville) {
		this.ville = ville;
	}

	public String getGroupeSanguin() {
		return groupeSanguin;
	}

	public void setGroupeSanguin(String groupeSanguin) {
		this.groupeSanguin = groupeSanguin;
	}

	public String getNumeroAssurance() {
		return numeroAssurance;
	}

	public void setNumeroAssurance(String numeroAssurance) {
		this.numeroAssurance = numeroAssurance;
	}

	public LocalDate getDateInscription() {
		return dateInscription;
	}

	public void setDateInscription(LocalDate dateInscription) {
		this.dateInscription = dateInscription;
	}

	public DossierMedical getDossierMedical() {
		return dossierMedical;
	}

	public void setDossierMedical(DossierMedical dossierMedical) {
		this.dossierMedical = dossierMedical;
	}

}
