package com.sip.clm.entities;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.validation.constraints.*;

@Entity
@Table(name = "rendez_vous")
public class RendezVous {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "La date est obligatoire")
	@Column(name = "date_rdv", nullable = false)
	private LocalDate date;

	@NotNull(message = "L'heure de début est obligatoire")
	private LocalTime heureDebut;

	@Min(value = 5, message = "La durée minimale est de 5 minutes")
	private int duree = 30;

	private String motif;

	@Enumerated(EnumType.STRING)
	private StatutRendezVous statut;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@NotNull(message = "Le patient est obligatoire")
	@ManyToOne(optional = false)
	@JoinColumn(name = "id_patient")
	private Patient patient;

	@NotNull(message = "Le médecin est obligatoire")
	@ManyToOne(optional = false)
	@JoinColumn(name = "id_medecin")
	private Medecin medecin;

	public RendezVous() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getHeureDebut() {
		return heureDebut;
	}

	public void setHeureDebut(LocalTime heureDebut) {
		this.heureDebut = heureDebut;
	}

	public int getDuree() {
		return duree;
	}

	public void setDuree(int duree) {
		this.duree = duree;
	}

	public String getMotif() {
		return motif;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}

	public StatutRendezVous getStatut() {
		return statut;
	}

	public void setStatut(StatutRendezVous statut) {
		this.statut = statut;
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

	public Medecin getMedecin() {
		return medecin;
	}

	public void setMedecin(Medecin medecin) {
		this.medecin = medecin;
	}

}
