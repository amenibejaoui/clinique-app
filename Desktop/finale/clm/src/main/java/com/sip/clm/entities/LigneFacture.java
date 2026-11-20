package com.sip.clm.entities;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.validation.constraints.*;

@Entity
@Table(name = "lignes_facture")
public class LigneFacture {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "La désignation est obligatoire")
	private String designation;

	@Min(value = 1, message = "La quantite minimale est 1")
	private int quantite = 1;

	@PositiveOrZero(message = "Le prix unitaire doit être positif")
	private double prixUnitaire;

	private double totalLigne;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "id_facture")
	private Facture facture;

	public LigneFacture() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public int getQuantite() {
		return quantite;
	}

	public void setQuantite(int quantite) {
		this.quantite = quantite;
	}

	public double getPrixUnitaire() {
		return prixUnitaire;
	}

	public void setPrixUnitaire(double prixUnitaire) {
		this.prixUnitaire = prixUnitaire;
	}

	public double getTotalLigne() {
		return totalLigne;
	}

	public void setTotalLigne(double totalLigne) {
		this.totalLigne = totalLigne;
	}

	public Facture getFacture() {
		return facture;
	}

	public void setFacture(Facture facture) {
		this.facture = facture;
	}

}
