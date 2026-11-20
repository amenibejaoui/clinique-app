package com.sip.clm.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sip.clm.entities.DossierMedical;
import com.sip.clm.entities.Facture;
import com.sip.clm.entities.Patient;
import com.sip.clm.entities.RendezVous;
import com.sip.clm.exceptions.BusinessException;
import com.sip.clm.exceptions.ResourceNotFoundException;
import com.sip.clm.repositories.DossierMedicalRepository;
import com.sip.clm.repositories.FactureRepository;
import com.sip.clm.repositories.PatientRepository;
import com.sip.clm.repositories.RendezVousRepository;
import com.sip.clm.repositories.UserRepository;

@Service
public class PatientService {

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private DossierMedicalRepository dossierRepository;

	@Autowired
	private RendezVousRepository rendezVousRepository;

	@Autowired
	private FactureRepository factureRepository;

	@Autowired
	private UserRepository userRepository;

	/** Recherche par nom, prénom ou CIN avec pagination. */
	public Page<Patient> search(String q, Pageable pageable) {
		if (q == null || q.trim().isEmpty()) {
			return patientRepository.findAll(pageable);
		}
		return patientRepository.search(q.trim(), pageable);
	}

	public List<Patient> findAll() {
		return patientRepository.findAll(Sort.by("nom", "prenom"));
	}

	public Patient get(Long id) {
		return patientRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Patient introuvable : " + id));
	}

	@Transactional
	public Patient create(Patient p) {
		p.setId(null);
		normalize(p);
		// règle 10 : le CIN est unique
		if (p.getCin() != null && patientRepository.existsByCin(p.getCin())) {
			throw new BusinessException("Un patient avec ce CIN existe déjà", HttpStatus.CONFLICT);
		}
		// un dossier médical vide est créé avec le patient (One-to-One)
		DossierMedical dossier = new DossierMedical();
		dossier.setPatient(p);
		p.setDossierMedical(dossier);
		return patientRepository.save(p);
	}

	@Transactional
	public Patient update(Long id, Patient in) {
		Patient e = get(id);
		normalize(in);
		if (in.getCin() != null && !in.getCin().equals(e.getCin()) && patientRepository.existsByCin(in.getCin())) {
			throw new BusinessException("Un patient avec ce CIN existe déjà", HttpStatus.CONFLICT);
		}
		e.setNom(in.getNom());
		e.setPrenom(in.getPrenom());
		e.setDateNaissance(in.getDateNaissance());
		e.setSexe(in.getSexe());
		e.setCin(in.getCin());
		e.setTel(in.getTel());
		e.setEmail(in.getEmail());
		e.setAdresse(in.getAdresse());
		e.setVille(in.getVille());
		e.setGroupeSanguin(in.getGroupeSanguin());
		e.setNumeroAssurance(in.getNumeroAssurance());
		return patientRepository.save(e);
	}

	@Transactional
	public void delete(Long id) {
		Patient e = get(id);
		// règle 11 : suppression interdite si le patient a des rendez-vous ou des factures
		if (rendezVousRepository.existsByPatientId(id) || factureRepository.existsByPatientId(id)) {
			throw new BusinessException("Impossible de supprimer un patient qui a des rendez-vous ou des factures",
					HttpStatus.CONFLICT);
		}
		userRepository.findByPatientId(id).forEach(user -> {
			user.setPatient(null);
			user.setActif(false);
		});
		patientRepository.delete(e);
	}

	public List<RendezVous> rendezVous(Long patientId) {
		get(patientId);
		return rendezVousRepository.findByPatientIdOrderByDateDescHeureDebutDesc(patientId);
	}

	public List<Facture> factures(Long patientId) {
		get(patientId);
		return factureRepository.findByPatientIdOrderByDateEmissionDesc(patientId);
	}

	@Transactional
	public DossierMedical getDossier(Long patientId) {
		Patient p = get(patientId);
		return dossierRepository.findByPatientId(patientId).orElseGet(() -> {
			DossierMedical d = new DossierMedical();
			d.setPatient(p);
			return dossierRepository.save(d);
		});
	}

	@Transactional
	public DossierMedical updateDossier(Long patientId, DossierMedical in) {
		DossierMedical d = getDossier(patientId);
		d.setAllergies(in.getAllergies());
		d.setAntecedents(in.getAntecedents());
		d.setMaladiesChroniques(in.getMaladiesChroniques());
		d.setNotes(in.getNotes());
		return dossierRepository.save(d);
	}

	private void normalize(Patient p) {
		if (p.getCin() != null) {
			String cin = p.getCin().trim();
			p.setCin(cin.isEmpty() ? null : cin);
		}
	}
}
