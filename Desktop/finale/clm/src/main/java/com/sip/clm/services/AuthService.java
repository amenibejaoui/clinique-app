package com.sip.clm.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sip.clm.config.JwtTokenUtil;
import com.sip.clm.entities.Role;
import com.sip.clm.entities.User;
import com.sip.clm.exceptions.BusinessException;
import com.sip.clm.models.JwtRequest;
import com.sip.clm.models.JwtResponse;
import com.sip.clm.models.RegisterRequest;
import com.sip.clm.repositories.RoleRepository;
import com.sip.clm.repositories.UserRepository;

@Service
public class AuthService {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MailService mailService;

	public JwtResponse login(JwtRequest request) {
		if (request.getUsername() == null || request.getPassword() == null) {
			throw new BusinessException("Email et mot de passe obligatoires", HttpStatus.BAD_REQUEST);
		}
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		} catch (DisabledException e) {
			throw new BusinessException("Votre compte est en attente de validation par l'administrateur",
					HttpStatus.FORBIDDEN);
		} catch (BadCredentialsException e) {
			throw new BusinessException("Email ou mot de passe incorrect", HttpStatus.UNAUTHORIZED);
		}

		final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
		final String token = jwtTokenUtil.generateToken(userDetails);
		User user = userRepository.findByEmail(request.getUsername()).get();
		List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());
		return new JwtResponse(token, user.getId(), user.getEmail(), user.getNom(), user.getPrenom(), roles);
	}

	/** Cree un compte inactif (role SECRETAIRE) et previent l'admin par email. */
	@Transactional
	public Map<String, String> register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new BusinessException("Cet email est déjà utilisé", HttpStatus.CONFLICT);
		}
		User user = new User();
		user.setEmail(request.getEmail().trim());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setNom(request.getNom());
		user.setPrenom(request.getPrenom());
		user.setTel(request.getTel());
		user.setActif(false);
		Role secretaire = roleRepository.findByLibelle("SECRETAIRE")
				.orElseThrow(() -> new BusinessException("Rôle SECRETAIRE introuvable"));
		user.getRoles().add(secretaire);
		userRepository.save(user);

		mailService.notifyAdminNewUser(user);

		Map<String, String> body = new HashMap<>();
		body.put("message", "Inscription enregistrée. Votre compte sera actif après validation par l'administrateur.");
		return body;
	}
}
