package com.sip.clm.models;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class RegisterRequest {

	@NotBlank(message = "L'email est obligatoire")
	@Email(message = "Format d'email invalide")
	private String email;

	@NotBlank(message = "Le mot de passe est obligatoire")
	@Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
	private String password;

	@Pattern(regexp = "^[+0-9 ]{8,15}$", message = "Téléphone invalide")
	private String tel;

	@NotBlank(message = "Le nom est obligatoire")
	private String nom;

	@NotBlank(message = "Le prénom est obligatoire")
	private String prenom;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
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
}
