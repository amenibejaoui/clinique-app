package com.sip.clm.controllers;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sip.clm.entities.Consultation;
import com.sip.clm.entities.Facture;
import com.sip.clm.entities.RendezVous;
import com.sip.clm.entities.User;
import com.sip.clm.exceptions.BusinessException;
import com.sip.clm.repositories.ConsultationRepository;
import com.sip.clm.repositories.FactureRepository;
import com.sip.clm.repositories.RendezVousRepository;
import com.sip.clm.repositories.UserRepository;

@RestController
@RequestMapping("/api/me")
public class CurrentUserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RendezVousRepository rendezVousRepository;

	@Autowired
	private ConsultationRepository consultationRepository;

	@Autowired
	private FactureRepository factureRepository;

	@GetMapping
	public User profile(Principal principal) {
		return currentUser(principal);
	}

	@GetMapping("/rendezvous")
	public List<RendezVous> rendezVous(Principal principal) {
		User user = currentUser(principal);
		if (hasRole(user, "PATIENT") && user.getPatient() != null) {
			return rendezVousRepository.findByPatientIdOrderByDateDescHeureDebutDesc(user.getPatient().getId());
		}
		if (hasRole(user, "MEDECIN") && user.getMedecin() != null) {
			return rendezVousRepository.findByMedecinIdOrderByDateDescHeureDebutDesc(user.getMedecin().getId());
		}
		throw new BusinessException("Ce compte n'est pas associé à une fiche médecin ou patient", HttpStatus.FORBIDDEN);
	}

	@GetMapping("/consultations")
	public List<Consultation> consultations(Principal principal) {
		User user = currentUser(principal);
		if (hasRole(user, "PATIENT") && user.getPatient() != null) {
			return consultationRepository
					.findByRendezVousPatientIdOrderByDateConsultationDesc(user.getPatient().getId());
		}
		if (hasRole(user, "MEDECIN") && user.getMedecin() != null) {
			return consultationRepository
					.findByRendezVousMedecinIdOrderByDateConsultationDesc(user.getMedecin().getId());
		}
		throw new BusinessException("Ce compte n'est pas associé à une fiche médecin ou patient", HttpStatus.FORBIDDEN);
	}

	@GetMapping("/factures")
	public List<Facture> factures(Principal principal) {
		User user = currentUser(principal);
		if (!hasRole(user, "PATIENT") || user.getPatient() == null) {
			throw new BusinessException("Cette fonctionnalité est réservée aux patients", HttpStatus.FORBIDDEN);
		}
		return factureRepository.findByPatientIdOrderByDateEmissionDesc(user.getPatient().getId());
	}

	private User currentUser(Principal principal) {
		return userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new BusinessException("Utilisateur introuvable", HttpStatus.UNAUTHORIZED));
	}

	private boolean hasRole(User user, String role) {
		return user.getRoles().stream().map(r -> r.getLibelle()).collect(Collectors.toSet()).contains(role);
	}
}
