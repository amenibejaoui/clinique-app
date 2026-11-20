package com.sip.clm.models;

import java.io.Serializable;
import java.util.List;

public class JwtResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String token;
	private final Long id;
	private final String email;
	private final String nom;
	private final String prenom;
	private final List<String> roles;

	public JwtResponse(String token, Long id, String email, String nom, String prenom, List<String> roles) {
		this.token = token;
		this.id = id;
		this.email = email;
		this.nom = nom;
		this.prenom = prenom;
		this.roles = roles;
	}

	public String getToken() {
		return token;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getNom() {
		return nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public List<String> getRoles() {
		return roles;
	}
}
