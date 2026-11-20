package com.sip.clm.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sip.clm.entities.Consultation;
import com.sip.clm.entities.RendezVous;
import com.sip.clm.entities.StatutRendezVous;
import com.sip.clm.exceptions.BusinessException;
import com.sip.clm.exceptions.ResourceNotFoundException;
import com.sip.clm.repositories.ConsultationRepository;
import com.sip.clm.repositories.RendezVousRepository;

@Service
public class ConsultationService {

	@Autowired
	private ConsultationRepository consultationRepository;

	@Autowired
	private RendezVousRepository rendezVousRepository;

	public Page<Consultation> findAll(Pageable pageable) {
		return consultationRepository.findAll(pageable);
	}

	public Consultation get(Long id) {
		return consultationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Consultation introuvable : " + id));
	}

	@Transactional
	public Consultation create(Consultation in) {
		if (in.getRendezVous() == null || in.getRendezVous().getId() == null) {
			throw new BusinessException("Le rendez-vous est obligatoire");
		}
		RendezVous rdv = rendezVousRepository.findById(in.getRendezVous().getId()).orElseThrow(
				() -> new ResourceNotFoundException("Rendez-vous introuvable : " + in.getRendezVous().getId()));
		// règle 4 : uniquement pour un rendez-vous EN_COURS ou TERMINE
		if (rdv.getStatut() != StatutRendezVous.EN_COURS && rdv.getStatut() != StatutRendezVous.TERMINE) {
			throw new BusinessException("Une consultation ne peut être créée que pour un rendez-vous EN_COURS ou TERMINE");
		}
		if (consultationRepository.existsByRendezVousId(rdv.getId())) {
			throw new BusinessException("Ce rendez-vous a déjà une consultation", HttpStatus.CONFLICT);
		}
		in.setId(null);
		in.setRendezVous(rdv);
		if (in.getDateConsultation() == null) {
			in.setDateConsultation(LocalDateTime.now());
		}
		return consultationRepository.save(in);
	}

	@Transactional
	public Consultation update(Long id, Consultation in) {
		Consultation e = get(id);
		if (in.getDateConsultation() != null) {
			e.setDateConsultation(in.getDateConsultation());
		}
		e.setDiagnostic(in.getDiagnostic());
		e.setObservations(in.getObservations());
		e.setPoids(in.getPoids());
		e.setTaille(in.getTaille());
		e.setTension(in.getTension());
		e.setTemperature(in.getTemperature());
		e.setTraitementPrescrit(in.getTraitementPrescrit());
		return consultationRepository.save(e);
	}

	public void delete(Long id) {
		consultationRepository.delete(get(id));
	}
}
