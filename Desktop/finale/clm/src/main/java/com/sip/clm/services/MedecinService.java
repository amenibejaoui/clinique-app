package com.sip.clm.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sip.clm.entities.Medecin;
import com.sip.clm.entities.RendezVous;
import com.sip.clm.exceptions.BusinessException;
import com.sip.clm.exceptions.ResourceNotFoundException;
import com.sip.clm.repositories.MedecinRepository;
import com.sip.clm.repositories.RendezVousRepository;
import com.sip.clm.repositories.UserRepository;

@Service
public class MedecinService {

	@Autowired
	private MedecinRepository medecinRepository;

	@Autowired
	private RendezVousRepository rendezVousRepository;

	@Autowired
	private UserRepository userRepository;

	public Page<Medecin> search(String q, Pageable pageable) {
		if (q == null || q.trim().isEmpty()) {
			return medecinRepository.findAll(pageable);
		}
		return medecinRepository.search(q.trim(), pageable);
	}

	public List<Medecin> findAll() {
		return medecinRepository.findAll(Sort.by("nom", "prenom"));
	}

	public Medecin get(Long id) {
		return medecinRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Médecin introuvable : " + id));
	}

	public Medecin create(Medecin m) {
		m.setId(null);
		return medecinRepository.save(m);
	}

	public Medecin update(Long id, Medecin in) {
		Medecin e = get(id);
		e.setNom(in.getNom());
		e.setPrenom(in.getPrenom());
		e.setSpecialite(in.getSpecialite());
		e.setTel(in.getTel());
		e.setEmail(in.getEmail());
		e.setNumeroOrdre(in.getNumeroOrdre());
		e.setTarifConsultation(in.getTarifConsultation());
		e.setActif(in.isActif());
		return medecinRepository.save(e);
	}

	@Transactional
	public void delete(Long id) {
		Medecin e = get(id);
		// règle 11 : suppression interdite s'il a des rendez-vous (désactivation recommandée)
		if (rendezVousRepository.existsByMedecinId(id)) {
			throw new BusinessException(
					"Impossible de supprimer un médecin qui a des rendez-vous : désactivez-le à la place",
					HttpStatus.CONFLICT);
		}
		userRepository.findByMedecinId(id).forEach(user -> {
			user.setMedecin(null);
			user.setActif(false);
		});
		medecinRepository.delete(e);
	}

	public List<RendezVous> planning(Long id, LocalDate date) {
		get(id);
		return rendezVousRepository.findByMedecinIdAndDateOrderByHeureDebutAsc(id, date);
	}
}
