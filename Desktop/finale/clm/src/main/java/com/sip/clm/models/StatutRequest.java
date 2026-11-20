package com.sip.clm.models;

import javax.validation.constraints.NotNull;

import com.sip.clm.entities.StatutRendezVous;

public class StatutRequest {

	@NotNull(message = "Le statut est obligatoire")
	private StatutRendezVous statut;

	public StatutRendezVous getStatut() {
		return statut;
	}

	public void setStatut(StatutRendezVous statut) {
		this.statut = statut;
	}
}
