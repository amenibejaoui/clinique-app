package com.sip.clm.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sip.clm.entities.Role;
import com.sip.clm.entities.User;
import com.sip.clm.exceptions.BusinessException;
import com.sip.clm.exceptions.ResourceNotFoundException;
import com.sip.clm.repositories.RoleRepository;
import com.sip.clm.repositories.PatientRepository;
import com.sip.clm.repositories.MedecinRepository;
import com.sip.clm.repositories.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private MedecinRepository medecinRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MailService mailService;

	public Page<User> search(String q, Pageable pageable) {
		if (q == null || q.trim().isEmpty()) {
			return userRepository.findAll(pageable);
		}
		return userRepository.search(q.trim(), pageable);
	}

	public List<Role> findRoles() {
		return roleRepository.findAll();
	}

	public User get(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
	}

	@Transactional
	public User create(User in) {
		if (in.getPassword() == null || in.getPassword().length() < 6) {
			throw new BusinessException("Le mot de passe doit contenir au moins 6 caractères");
		}
		if (userRepository.existsByEmail(in.getEmail())) {
			throw new BusinessException("Cet email est déjà utilisé", HttpStatus.CONFLICT);
		}
		in.setId(null);
		in.setEmail(in.getEmail().trim());
		in.setPassword(passwordEncoder.encode(in.getPassword()));
		Set<Role> roles = resolveRoles(in.getRoles());
		if (roles.isEmpty()) {
			roles.add(roleRepository.findByLibelle("SECRETAIRE")
					.orElseThrow(() -> new BusinessException("Rôle SECRETAIRE introuvable")));
		}
		in.setRoles(roles);
		applyLinkedProfile(in, roles, null);
		User saved = userRepository.save(in);
		if (saved.isActif()) {
			mailService.notifyUserActivated(saved);
		}
		return saved;
	}

	@Transactional
	public User update(Long id, User in) {
		User e = get(id);
		boolean etaitInactif = !e.isActif();
		if (!e.getEmail().equalsIgnoreCase(in.getEmail()) && userRepository.existsByEmail(in.getEmail())) {
			throw new BusinessException("Cet email est déjà utilisé", HttpStatus.CONFLICT);
		}
		e.setEmail(in.getEmail().trim());
		e.setNom(in.getNom());
		e.setPrenom(in.getPrenom());
		e.setTel(in.getTel());
		e.setActif(in.isActif());
		if (in.getPassword() != null && !in.getPassword().trim().isEmpty()) {
			if (in.getPassword().length() < 6) {
				throw new BusinessException("Le mot de passe doit contenir au moins 6 caractères");
			}
			e.setPassword(passwordEncoder.encode(in.getPassword()));
		}
		if (in.getRoles() != null && !in.getRoles().isEmpty()) {
			Set<Role> roles = resolveRoles(in.getRoles());
			e.setRoles(roles);
			applyLinkedProfile(in, roles, e.getId());
			e.setPatient(in.getPatient());
			e.setMedecin(in.getMedecin());
		}
		User saved = userRepository.save(e);
		if (etaitInactif && saved.isActif()) {
			mailService.notifyUserActivated(saved);
		}
		return saved;
	}

	/** Active ou desactive un compte (validation d'une inscription par l'admin). */
	@Transactional
	public User setActif(Long id, boolean actif) {
		User e = get(id);
		boolean etaitInactif = !e.isActif();
		e.setActif(actif);
		User saved = userRepository.save(e);
		if (actif && etaitInactif) {
			mailService.notifyUserActivated(saved);
		}
		return saved;
	}

	public void delete(Long id, String emailCourant) {
		User e = get(id);
		if (e.getEmail().equalsIgnoreCase(emailCourant)) {
			throw new BusinessException("Vous ne pouvez pas supprimer votre propre compte");
		}
		userRepository.delete(e);
	}

	private Set<Role> resolveRoles(Set<Role> in) {
		Set<Role> result = new HashSet<>();
		if (in == null) {
			return result;
		}
		for (Role r : in) {
			Role found = null;
			if (r.getLibelle() != null) {
				found = roleRepository.findByLibelle(r.getLibelle().trim().toUpperCase()).orElse(null);
			}
			if (found == null && r.getId() != null) {
				found = roleRepository.findById(r.getId()).orElse(null);
			}
			if (found == null) {
				throw new BusinessException("Rôle inconnu");
			}
			result.add(found);
		}
		return result;
	}

	private void applyLinkedProfile(User user, Set<Role> roles, Long currentUserId) {
		boolean patientRole = roles.stream().anyMatch(role -> "PATIENT".equals(role.getLibelle()));
		boolean medecinRole = roles.stream().anyMatch(role -> "MEDECIN".equals(role.getLibelle()));
		if (patientRole && medecinRole) {
			throw new BusinessException("Un compte ne peut pas être à la fois médecin et patient");
		}
		if (patientRole) {
			if (user.getPatient() == null || user.getPatient().getId() == null) {
				throw new BusinessException("Une fiche patient doit être associée à ce compte");
			}
			Long patientId = user.getPatient().getId();
			if (userRepository.findByPatientId(patientId).stream()
					.anyMatch(existing -> !existing.getId().equals(currentUserId))) {
				throw new BusinessException("Cette fiche patient est déjà associée à un compte", HttpStatus.CONFLICT);
			}
			user.setPatient(patientRepository.findById(patientId).orElseThrow(
					() -> new ResourceNotFoundException("Patient introuvable : " + user.getPatient().getId())));
			user.setMedecin(null);
		} else if (medecinRole) {
			if (user.getMedecin() == null || user.getMedecin().getId() == null) {
				throw new BusinessException("Une fiche médecin doit être associée à ce compte");
			}
			Long medecinId = user.getMedecin().getId();
			if (userRepository.findByMedecinId(medecinId).stream()
					.anyMatch(existing -> !existing.getId().equals(currentUserId))) {
				throw new BusinessException("Cette fiche médecin est déjà associée à un compte", HttpStatus.CONFLICT);
			}
			user.setMedecin(medecinRepository.findById(medecinId).orElseThrow(
					() -> new ResourceNotFoundException("Médecin introuvable : " + medecinId)));
			user.setPatient(null);
		} else {
			user.setPatient(null);
			user.setMedecin(null);
		}
	}
}
