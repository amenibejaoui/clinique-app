package com.sip.clm.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sip.clm.entities.Role;
import com.sip.clm.entities.User;
import com.sip.clm.repositories.UserRepository;

/** Charge l'utilisateur depuis la base MySQL (a la place de l'utilisateur en dur du workshop). */
@Service
public class JwtUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

		List<GrantedAuthority> authorities = new ArrayList<>();
		for (Role role : user.getRoles()) {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getLibelle()));
		}
		// enabled = actif : un compte non valide par l'admin ne peut pas se connecter
		return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
				user.isActif(), true, true, true, authorities);
	}
}
