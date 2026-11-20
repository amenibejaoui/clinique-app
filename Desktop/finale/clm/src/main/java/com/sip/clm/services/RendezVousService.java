package com.sip.clm.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sip.clm.entities.Medecin;
import com.sip.clm.entities.Patient;
import com.sip.clm.entities.RendezVous;
import com.sip.clm.entities.StatutRendezVous;
import com.sip.clm.exceptions.BusinessException;
import com.sip.clm.exceptions.ResourceNotFoundException;
import com.sip.clm.repositories.ConsultationRepository;
import com.sip.clm.repositories.FactureRepository;
import com.sip.clm.repositories.MedecinRepository;
import com.sip.clm.repositories.PatientRepository;
import com.sip.clm.repositories.RendezVousRepository;

@Service
public class RendezVousService {

	/** Les rendez-vous annulés ou absents ne bloquent plus le créneau. */
	private static final List<StatutRendezVous> LIBERENT_CRENEAU = Arrays.asList(StatutRendezVous.ANNULE,
			StatutRendezVous.ABSENT);

	@Autowired
	private RendezVousRepository rendezVousRepository;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private MedecinRepository medecinRepository;

	@Autowired
	private ConsultationRepository consultationRepository;

	@Autowired
	private FactureRepository factureRepository;

	@Autowired
	private MailService mailService;

	public Page<RendezVous> filter(LocalDate date, Long medecinId, StatutRendezVous statut, Pageable pageable) {
		return rendezVousRepository.filter(date, medecinId, statut, pageable);
	}

	public RendezVous get(Long id) {
		return rendezVousRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Rendez-vous introuvable : " + id));
	}

	@Transactional
	public RendezVous create(RendezVous in) {
		Patient patient = loadPatient(in);
		Medecin medecin = loadMedecin(in);
		checkDuree(in);
		// règle 2 : pas de rendez-vous dans le passé
		checkNotPast(in.getDate(), in.getHeureDebut());
		// règle 1 : pas de chevauchement pour un même médecin
		checkOverlap(medecin.getId(), in.getDate(), in.getHeureDebut(), in.getDuree(), null);

		in.setId(null);
		in.setPatient(patient);
		in.setMedecin(medecin);
		in.setStatut(StatutRendezVous.PLANIFIE); // règle 3 : début du cycle de vie
		RendezVous saved = rendezVousRepository.save(in);
		mailService.notifyRendezVous(saved);
		return saved;
	}

	@Transactional
	public RendezVous update(Long id, RendezVous in) {
		RendezVous e = get(id);
		if (e.getStatut() == StatutRendezVous.TERMINE || e.getStatut() == StatutRendezVous.ANNULE
				|| e.getStatut() == StatutRendezVous.ABSENT) {
			throw new BusinessException("Un rendez-vous terminé, annulé ou absent ne peut plus être modifié");
		}
		Patient patient = loadPatient(in);
		Medecin medecin = loadMedecin(in);
		checkDuree(in);
		boolean creneauChange = !e.getDate().equals(in.getDate()) || !e.getHeureDebut().equals(in.getHeureDebut());
		if (creneauChange) {
			checkNotPast(in.getDate(), in.getHeureDebut());
		}
		checkOverlap(medecin.getId(), in.getDate(), in.getHeureDebut(), in.getDuree(), e.getId());

		e.setDate(in.getDate());
		e.setHeureDebut(in.getHeureDebut());
		e.setDuree(in.getDuree());
		e.setMotif(in.getMotif());
		e.setNotes(in.getNotes());
		e.setPatient(patient);
		e.setMedecin(medecin);
		// le statut ne change que via PATCH /{id}/statut (cycle de vie contrôlé)
		RendezVous saved = rendezVousRepository.save(e);
		mailService.notifyRendezVous(saved);
		return saved;
	}

	/** Règle 3 : PLANIFIE -> CONFIRME -> EN_COURS -> TERMINE ; ANNULE / ABSENT possibles avant le début. */
	@Transactional
	public RendezVous changerStatut(Long id, StatutRendezVous nouveau) {
		RendezVous e = get(id);
		StatutRendezVous actuel = e.getStatut();
		boolean autorise;
		switch (actuel) {
		case PLANIFIE:
			autorise = nouveau == StatutRendezVous.CONFIRME || nouveau == StatutRendezVous.ANNULE
					|| nouveau == StatutRendezVous.ABSENT;
			break;
		case CONFIRME:
			autorise = nouveau == StatutRendezVous.EN_COURS || nouveau == StatutRendezVous.ANNULE
					|| nouveau == StatutRendezVous.ABSENT;
			break;
		case EN_COURS:
			autorise = nouveau == StatutRendezVous.TERMINE;
			break;
		default:
			autorise = false;
		}
		if (!autorise) {
			throw new BusinessException("Transition de statut interdite : " + actuel + " -> " + nouveau);
		}
		e.setStatut(nouveau);
		return rendezVousRepository.save(e);
	}

	public void delete(Long id) {
		RendezVous e = get(id);
		if (consultationRepository.existsByRendezVousId(id) || factureRepository.existsByRendezVousId(id)) {
			throw new BusinessException("Impossible de supprimer un rendez-vous qui a une consultation ou une facture",
					HttpStatus.CONFLICT);
		}
		rendezVousRepository.delete(e);
	}

	// ------------------------------------------------------------ helpers

	private Patient loadPatient(RendezVous in) {
		if (in.getPatient() == null || in.getPatient().getId() == null) {
			throw new BusinessException("Le patient est obligatoire");
		}
		return patientRepository.findById(in.getPatient().getId())
				.orElseThrow(() -> new ResourceNotFoundException("Patient introuvable : " + in.getPatient().getId()));
	}

	private Medecin loadMedecin(RendezVous in) {
		if (in.getMedecin() == null || in.getMedecin().getId() == null) {
			throw new BusinessException("Le médecin est obligatoire");
		}
		Medecin m = medecinRepository.findById(in.getMedecin().getId())
				.orElseThrow(() -> new ResourceNotFoundException("Médecin introuvable : " + in.getMedecin().getId()));
		if (!m.isActif()) {
			throw new BusinessException("Ce médecin est désactivé");
		}
		return m;
	}

	private void checkDuree(RendezVous in) {
		if (in.getDuree() <= 0) {
			in.setDuree(30);
		}
	}

	private void checkNotPast(LocalDate date, LocalTime heure) {
		if (LocalDateTime.of(date, heure).isBefore(LocalDateTime.now())) {
			throw new BusinessException("Un rendez-vous ne peut pas être créé dans le passé");
		}
	}

	private int minutes(LocalTime t) {
		return t.getHour() * 60 + t.getMinute();
	}

	private void checkOverlap(Long medecinId, LocalDate date, LocalTime heure, int duree, Long excludeId) {
		int debut = minutes(heure);
		int fin = debut + duree;
		List<RendezVous> existants = rendezVousRepository.findByMedecinIdAndDateAndStatutNotIn(medecinId, date,
				LIBERENT_CRENEAU);
		for (RendezVous r : existants) {
			if (excludeId != null && excludeId.equals(r.getId())) {
				continue;
			}
			int d = minutes(r.getHeureDebut());
			int f = d + r.getDuree();
			if (debut < f && fin > d) {
				throw new BusinessException("Le médecin a déjà un rendez-vous qui chevauche ce créneau ("
						+ r.getHeureDebut() + " - " + r.getHeureDebut().plusMinutes(r.getDuree()) + ")",
						HttpStatus.CONFLICT);
			}
		}
	}
}
