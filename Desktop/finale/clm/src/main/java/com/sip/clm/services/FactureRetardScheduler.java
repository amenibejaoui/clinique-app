package com.sip.clm.services;

import java.time.LocalDate;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sip.clm.entities.Facture;
import com.sip.clm.entities.StatutFacture;
import com.sip.clm.repositories.FactureRepository;

/** Alerte (optionnelle) : chaque jour a 8h, previent l'admin des factures en retard de paiement. */
@Component
public class FactureRetardScheduler {

	@Autowired
	private FactureRepository factureRepository;

	@Autowired
	private MailService mailService;

	@Scheduled(cron = "0 0 8 * * *")
	public void alerterFacturesEnRetard() {
		for (Facture f : factureRepository.findByDateEcheanceBeforeAndStatutIn(LocalDate.now(),
				Arrays.asList(StatutFacture.EN_ATTENTE, StatutFacture.PARTIELLEMENT_PAYEE))) {
			mailService.alertFactureEnRetard(f);
		}
	}
}
