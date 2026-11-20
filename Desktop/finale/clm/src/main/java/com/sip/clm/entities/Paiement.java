package com.sip.clm.entities;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import javax.validation.constraints.*;

@Entity
@Table(name = "paiements")
public class Paiement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate datePaiement;

	@Positive(message = "Le montant doit être supérieur à 0")
	private double montant;

	@NotNull(message = "Le mode de paiement est obligatoire")
	@Enumerated(EnumType.STRING)
	private ModePaiement modePaiement;

	private String reference;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "id_facture")
	private Facture facture;

	public Paiement() {
	}

	@JsonProperty("factureId")
	public Long factureId() {
		return facture != null ? facture.getId() : null;
	}

	@JsonProperty("factureNumero")
	public String factureNumero() {
		return facture != null ? facture.getNumero() : null;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getDatePaiement() {
		return datePaiement;
	}

	public void setDatePaiement(LocalDate datePaiement) {
		this.datePaiement = datePaiement;
	}

	public double getMontant() {
		return montant;
	}

	public void setMontant(double montant) {
		this.montant = montant;
	}

	public ModePaiement getModePaiement() {
		return modePaiement;
	}

	public void setModePaiement(ModePaiement modePaiement) {
		this.modePaiement = modePaiement;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public Facture getFacture() {
		return facture;
	}

	public void setFacture(Facture facture) {
		this.facture = facture;
	}

}
