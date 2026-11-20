package com.sip.clm.entities;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.*;

@Entity
@Table(name = "factures")
public class Facture {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String numero;

	private LocalDate dateEmission;

	private LocalDate dateEcheance;

	private double montantHT;

	@PositiveOrZero(message = "Le taux de TVA doit être positif")
	private double tauxTVA;

	private double montantTVA;

	private double montantTTC;

	private double montantPaye;

	private double resteAPayer;

	@Enumerated(EnumType.STRING)
	private StatutFacture statut;

	@ManyToOne
	@JoinColumn(name = "id_patient")
	private Patient patient;

	@OneToOne
	@JoinColumn(name = "id_rendez_vous", unique = true)
	private RendezVous rendezVous;

	@Valid
	@OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<LigneFacture> lignes = new ArrayList<>();

	@OneToMany(mappedBy = "facture", cascade = CascadeType.REMOVE)
	private List<Paiement> paiements = new ArrayList<>();

	public Facture() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public LocalDate getDateEmission() {
		return dateEmission;
	}

	public void setDateEmission(LocalDate dateEmission) {
		this.dateEmission = dateEmission;
	}

	public LocalDate getDateEcheance() {
		return dateEcheance;
	}

	public void setDateEcheance(LocalDate dateEcheance) {
		this.dateEcheance = dateEcheance;
	}

	public double getMontantHT() {
		return montantHT;
	}

	public void setMontantHT(double montantHT) {
		this.montantHT = montantHT;
	}

	public double getTauxTVA() {
		return tauxTVA;
	}

	public void setTauxTVA(double tauxTVA) {
		this.tauxTVA = tauxTVA;
	}

	public double getMontantTVA() {
		return montantTVA;
	}

	public void setMontantTVA(double montantTVA) {
		this.montantTVA = montantTVA;
	}

	public double getMontantTTC() {
		return montantTTC;
	}

	public void setMontantTTC(double montantTTC) {
		this.montantTTC = montantTTC;
	}

	public double getMontantPaye() {
		return montantPaye;
	}

	public void setMontantPaye(double montantPaye) {
		this.montantPaye = montantPaye;
	}

	public double getResteAPayer() {
		return resteAPayer;
	}

	public void setResteAPayer(double resteAPayer) {
		this.resteAPayer = resteAPayer;
	}

	public StatutFacture getStatut() {
		return statut;
	}

	public void setStatut(StatutFacture statut) {
		this.statut = statut;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public RendezVous getRendezVous() {
		return rendezVous;
	}

	public void setRendezVous(RendezVous rendezVous) {
		this.rendezVous = rendezVous;
	}

	public List<LigneFacture> getLignes() {
		return lignes;
	}

	public void setLignes(List<LigneFacture> lignes) {
		this.lignes = lignes;
	}

	public List<Paiement> getPaiements() {
		return paiements;
	}

	public void setPaiements(List<Paiement> paiements) {
		this.paiements = paiements;
	}

}
