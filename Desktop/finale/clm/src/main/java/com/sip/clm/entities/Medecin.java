package com.sip.clm.entities;

import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
@Table(name = "medecins")
public class Medecin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Le nom est obligatoire")
	private String nom;

	@NotBlank(message = "Le prénom est obligatoire")
	private String prenom;

	private String specialite;

	@Pattern(regexp = "^[+0-9 ]{8,15}$", message = "Téléphone invalide")
	private String tel;

	@Email(message = "Format d'email invalide")
	private String email;

	private String numeroOrdre;

	@PositiveOrZero(message = "Le tarif doit être positif")
	private double tarifConsultation;

	private boolean actif = true;

	public Medecin() {
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

	public String getSpecialite() {
		return specialite;
	}

	public void setSpecialite(String specialite) {
		this.specialite = specialite;
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

	public String getNumeroOrdre() {
		return numeroOrdre;
	}

	public void setNumeroOrdre(String numeroOrdre) {
		this.numeroOrdre = numeroOrdre;
	}

	public double getTarifConsultation() {
		return tarifConsultation;
	}

	public void setTarifConsultation(double tarifConsultation) {
		this.tarifConsultation = tarifConsultation;
	}

	public boolean isActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

}
