package com.sip.clm.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import javax.validation.constraints.*;

@Entity
@Table(name = "consultations")
public class Consultation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDateTime dateConsultation;

	@Column(columnDefinition = "TEXT")
	private String diagnostic;

	@Column(columnDefinition = "TEXT")
	private String observations;

	private Double poids;

	private Double taille;

	private String tension;

	private Double temperature;

	@Column(columnDefinition = "TEXT")
	private String traitementPrescrit;

	@NotNull(message = "Le rendez-vous est obligatoire")
	@OneToOne(optional = false)
	@JoinColumn(name = "id_rendez_vous", nullable = false, unique = true)
	private RendezVous rendezVous;

	public Consultation() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getDateConsultation() {
		return dateConsultation;
	}

	public void setDateConsultation(LocalDateTime dateConsultation) {
		this.dateConsultation = dateConsultation;
	}

	public String getDiagnostic() {
		return diagnostic;
	}

	public void setDiagnostic(String diagnostic) {
		this.diagnostic = diagnostic;
	}

	public String getObservations() {
		return observations;
	}

	public void setObservations(String observations) {
		this.observations = observations;
	}

	public Double getPoids() {
		return poids;
	}

	public void setPoids(Double poids) {
		this.poids = poids;
	}

	public Double getTaille() {
		return taille;
	}

	public void setTaille(Double taille) {
		this.taille = taille;
	}

	public String getTension() {
		return tension;
	}

	public void setTension(String tension) {
		this.tension = tension;
	}

	public Double getTemperature() {
		return temperature;
	}

	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}

	public String getTraitementPrescrit() {
		return traitementPrescrit;
	}

	public void setTraitementPrescrit(String traitementPrescrit) {
		this.traitementPrescrit = traitementPrescrit;
	}

	public RendezVous getRendezVous() {
		return rendezVous;
	}

	public void setRendezVous(RendezVous rendezVous) {
		this.rendezVous = rendezVous;
	}

}
