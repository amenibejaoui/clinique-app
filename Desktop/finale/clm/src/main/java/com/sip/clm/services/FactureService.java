package com.sip.clm.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sip.clm.entities.Facture;
import com.sip.clm.entities.LigneFacture;
import com.sip.clm.entities.Paiement;
import com.sip.clm.entities.Patient;
import com.sip.clm.entities.RendezVous;
import com.sip.clm.entities.StatutFacture;
import com.sip.clm.exceptions.BusinessException;
import com.sip.clm.exceptions.ResourceNotFoundException;
import com.sip.clm.repositories.FactureRepository;
import com.sip.clm.repositories.PaiementRepository;
import com.sip.clm.repositories.PatientRepository;
import com.sip.clm.repositories.RendezVousRepository;
import com.sip.clm.util.MoneyUtil;

@Service
public class FactureService {

	@Autowired
	private FactureRepository factureRepository;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private RendezVousRepository rendezVousRepository;

	@Autowired
	private PaiementRepository paiementRepository;

	@Autowired
	private MailService mailService;

	public Page<Facture> filter(StatutFacture statut, Pageable pageable) {
		return factureRepository.filter(statut, pageable);
	}

	public Page<Paiement> findPaiements(Pageable pageable) {
		return paiementRepository.findAll(pageable);
	}

	public Facture get(Long id) {
		return factureRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Facture introuvable : " + id));
	}

	@Transactional
	public Facture create(Facture in) {
		Facture f = new Facture();

		RendezVous rdv = null;
		if (in.getRendezVous() != null && in.getRendezVous().getId() != null) {
			Long rdvId = in.getRendezVous().getId();
			rdv = rendezVousRepository.findById(rdvId)
					.orElseThrow(() -> new ResourceNotFoundException("Rendez-vous introuvable : " + rdvId));
			if (factureRepository.existsByRendezVousId(rdvId)) {
				throw new BusinessException("Ce rendez-vous est déjà facturé", HttpStatus.CONFLICT);
			}
			f.setRendezVous(rdv);
		}

		Patient patient;
		if (in.getPatient() != null && in.getPatient().getId() != null) {
			Long pid = in.getPatient().getId();
			patient = patientRepository.findById(pid)
					.orElseThrow(() -> new ResourceNotFoundException("Patient introuvable : " + pid));
		} else if (rdv != null) {
			patient = rdv.getPatient();
		} else {
			throw new BusinessException("Le patient est obligatoire");
		}
		if (rdv != null && !rdv.getPatient().getId().equals(patient.getId())) {
			throw new BusinessException("Le rendez-vous n'appartient pas à ce patient");
		}
		f.setPatient(patient);

		f.setDateEmission(in.getDateEmission() != null ? in.getDateEmission() : LocalDate.now());
		f.setDateEcheance(in.getDateEcheance() != null ? in.getDateEcheance() : f.getDateEmission().plusDays(30));
		if (f.getDateEcheance().isBefore(f.getDateEmission())) {
			throw new BusinessException("La date d'échéance doit être postérieure à la date d'émission");
		}
		f.setTauxTVA(in.getTauxTVA());
		f.setMontantPaye(0);
		f.setStatut(StatutFacture.EN_ATTENTE);
		f.setNumero(genererNumero(f.getDateEmission().getYear())); // règle 5

		if (in.getLignes() != null) {
			for (LigneFacture l : in.getLignes()) {
				addLigne(f, l.getDesignation(), l.getQuantite(), l.getPrixUnitaire());
			}
		}

		// règle 9 : ligne « Consultation » par défaut avec le tarif du médecin
		if (rdv != null && !hasLigneConsultation(f)) {
			LigneFacture l = new LigneFacture();
			l.setDesignation("Consultation");
			l.setQuantite(1);
			l.setPrixUnitaire(rdv.getMedecin().getTarifConsultation());
			l.setFacture(f);
			f.getLignes().add(0, l);
		}

		calculer(f);
		Facture saved = factureRepository.save(f);
		mailService.notifyFactureCreated(saved);
		return saved;
	}

	@Transactional
	public Facture update(Long id, Facture in) {
		Facture e = get(id);
		if (e.getStatut() == StatutFacture.ANNULEE) {
			throw new BusinessException("Une facture annulée ne peut pas être modifiée");
		}
		if (in.getDateEmission() != null) {
			e.setDateEmission(in.getDateEmission());
		}
		if (in.getDateEcheance() != null) {
			e.setDateEcheance(in.getDateEcheance());
		}
		if (e.getDateEcheance() != null && e.getDateEcheance().isBefore(e.getDateEmission())) {
			throw new BusinessException("La date d'échéance doit être postérieure à la date d'émission");
		}
		e.setTauxTVA(in.getTauxTVA());
		if (in.getLignes() != null) {
			e.getLignes().clear();
			for (LigneFacture l : in.getLignes()) {
				addLigne(e, l.getDesignation(), l.getQuantite(), l.getPrixUnitaire());
			}
		}
		calculer(e);
		return factureRepository.save(e);
	}

	@Transactional
	public Facture annuler(Long id) {
		Facture e = get(id);
		if (e.getMontantPaye() > 0) {
			throw new BusinessException("Impossible d'annuler une facture qui a déjà des paiements");
		}
		e.setStatut(StatutFacture.ANNULEE);
		return factureRepository.save(e);
	}

	@Transactional
	public void delete(Long id) {
		factureRepository.delete(get(id)); // supprime aussi les lignes et les paiements
	}

	public List<Facture> findByPatient(Long patientId) {
		return factureRepository.findByPatientIdOrderByDateEmissionDesc(patientId);
	}

	/** Règle 8 : un paiement ne peut pas dépasser le reste à payer. */
	@Transactional
	public Paiement addPaiement(Long factureId, Paiement in) {
		Facture f = get(factureId);
		if (f.getStatut() == StatutFacture.ANNULEE) {
			throw new BusinessException("Impossible de payer une facture annulée");
		}
		if (in.getMontant() <= 0) {
			throw new BusinessException("Le montant du paiement doit être supérieur à 0");
		}
		if (in.getMontant() > f.getResteAPayer() + MoneyUtil.EPS) {
			throw new BusinessException(
					"Le montant du paiement dépasse le reste à payer (" + f.getResteAPayer() + ")");
		}
		Paiement p = new Paiement();
		p.setDatePaiement(in.getDatePaiement() != null ? in.getDatePaiement() : LocalDate.now());
		p.setMontant(MoneyUtil.round(in.getMontant()));
		p.setModePaiement(in.getModePaiement());
		p.setReference(in.getReference());
		p.setFacture(f);
		p = paiementRepository.save(p);

		f.setMontantPaye(MoneyUtil.round(f.getMontantPaye() + p.getMontant()));
		calculer(f);
		factureRepository.save(f);
		mailService.notifyPaiement(p);
		return p;
	}

	// ------------------------------------------------------------ calculs

	private void addLigne(Facture f, String designation, int quantite, double prixUnitaire) {
		LigneFacture l = new LigneFacture();
		l.setDesignation(designation);
		l.setQuantite(quantite);
		l.setPrixUnitaire(prixUnitaire);
		l.setFacture(f);
		f.getLignes().add(l);
	}

	private boolean hasLigneConsultation(Facture f) {
		for (LigneFacture l : f.getLignes()) {
			if (l.getDesignation() != null && l.getDesignation().trim().equalsIgnoreCase("Consultation")) {
				return true;
			}
		}
		return false;
	}

	/** Règle 5 : FAC-AAAA-NNNN. */
	private String genererNumero(int annee) {
		final String prefix = "FAC-" + annee + "-";
		int suivant = 1;
		Facture derniere = factureRepository.findTopByNumeroStartingWithOrderByNumeroDesc(prefix).orElse(null);
		if (derniere != null) {
			suivant = Integer.parseInt(derniere.getNumero().substring(prefix.length())) + 1;
		}
		return String.format("FAC-%d-%04d", annee, suivant);
	}

	/** Règles 6 et 7 : totaux, reste à payer et statut. */
	private void calculer(Facture f) {
		double ht = 0;
		for (LigneFacture l : f.getLignes()) {
			l.setTotalLigne(MoneyUtil.round(l.getQuantite() * l.getPrixUnitaire()));
			ht += l.getTotalLigne();
		}
		f.setMontantHT(MoneyUtil.round(ht));
		f.setMontantTVA(MoneyUtil.round(f.getMontantHT() * f.getTauxTVA()));
		f.setMontantTTC(MoneyUtil.round(f.getMontantHT() + f.getMontantTVA()));
		if (f.getMontantPaye() > f.getMontantTTC() + MoneyUtil.EPS) {
			throw new BusinessException("Le montant déjà payé dépasse le total de la facture");
		}
		f.setResteAPayer(MoneyUtil.round(f.getMontantTTC() - f.getMontantPaye()));
		majStatut(f);
	}

	private void majStatut(Facture f) {
		if (f.getStatut() == StatutFacture.ANNULEE) {
			return;
		}
		if (f.getMontantPaye() <= MoneyUtil.EPS) {
			f.setStatut(StatutFacture.EN_ATTENTE);
		} else if (f.getResteAPayer() > MoneyUtil.EPS) {
			f.setStatut(StatutFacture.PARTIELLEMENT_PAYEE);
		} else {
			f.setStatut(StatutFacture.PAYEE);
		}
	}
}
