package com.sip.clm.entities;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "dossiers_medicaux")
public class DossierMedical {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "TEXT")
	private String allergies;

	@Column(columnDefinition = "TEXT")
	private String antecedents;

	@Column(columnDefinition = "TEXT")
	private String maladiesChroniques;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@JsonIgnore
	@OneToOne(optional = false)
	@JoinColumn(name = "id_patient", nullable = false, unique = true)
	private Patient patient;

	public DossierMedical() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAllergies() {
		return allergies;
	}

	public void setAllergies(String allergies) {
		this.allergies = allergies;
	}

	public String getAntecedents() {
		return antecedents;
	}

	public void setAntecedents(String antecedents) {
		this.antecedents = antecedents;
	}

	public String getMaladiesChroniques() {
		return maladiesChroniques;
	}

	public void setMaladiesChroniques(String maladiesChroniques) {
		this.maladiesChroniques = maladiesChroniques;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

}
