package com.sip.clm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sip.clm.entities.Role;
import com.sip.clm.entities.User;
import com.sip.clm.repositories.RoleRepository;
import com.sip.clm.repositories.UserRepository;

/** Au premier demarrage : cree les roles et deux comptes de test. */
@Component
public class DataInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${clm.init.admin.email}")
	private String adminEmail;

	@Value("${clm.init.admin.password}")
	private String adminPassword;

	@Value("${clm.init.secretaire.email}")
	private String secretaireEmail;

	@Value("${clm.init.secretaire.password}")
	private String secretairePassword;

	@Override
	public void run(String... args) {
		Role admin = roleRepository.findByLibelle("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));
		Role secretaire = roleRepository.findByLibelle("SECRETAIRE")
				.orElseGet(() -> roleRepository.save(new Role("SECRETAIRE")));
		roleRepository.findByLibelle("MEDECIN")
				.orElseGet(() -> roleRepository.save(new Role("MEDECIN")));
		roleRepository.findByLibelle("PATIENT")
				.orElseGet(() -> roleRepository.save(new Role("PATIENT")));

		if (!userRepository.existsByEmail(adminEmail)) {
			userRepository.save(buildUser(adminEmail, adminPassword, "Admin", "Clinic", admin));
			log.info("Compte ADMIN cree : {}", adminEmail);
		}
		if (!userRepository.existsByEmail(secretaireEmail)) {
			userRepository.save(buildUser(secretaireEmail, secretairePassword, "Secretaire", "Clinic", secretaire));
			log.info("Compte SECRETAIRE cree : {}", secretaireEmail);
		}
	}

	private User buildUser(String email, String password, String nom, String prenom, Role role) {
		User u = new User();
		u.setEmail(email);
		u.setPassword(passwordEncoder.encode(password));
		u.setNom(nom);
		u.setPrenom(prenom);
		u.setActif(true);
		u.getRoles().add(role);
		return u;
	}
}
