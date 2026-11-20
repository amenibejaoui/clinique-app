package com.sip.clm.controllers;

import java.security.Principal;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sip.clm.entities.Role;
import com.sip.clm.entities.User;
import com.sip.clm.services.UserService;
import com.sip.clm.util.PageUtil;

/** Gestion des utilisateurs : reservee a l'ADMIN (voir WebSecurityConfig). */
@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;

	@GetMapping
	public Page<User> list(@RequestParam(required = false) String search, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return userService.search(search, PageUtil.of(page, size));
	}

	@GetMapping("/roles")
	public List<Role> roles() {
		return userService.findRoles();
	}

	@GetMapping("/{id}")
	public User get(@PathVariable Long id) {
		return userService.get(id);
	}

	/** body : {"email","password","nom","prenom","tel","actif","roles":[{"libelle":"SECRETAIRE"}]} */
	@PostMapping
	public ResponseEntity<User> create(@Valid @RequestBody User user) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(user));
	}

	@PutMapping("/{id}")
	public User update(@PathVariable Long id, @Valid @RequestBody User user) {
		return userService.update(id, user);
	}

	/** Validation d'une inscription : PATCH /api/users/{id}/activer */
	@PatchMapping("/{id}/activer")
	public User activer(@PathVariable Long id) {
		return userService.setActif(id, true);
	}

	@PatchMapping("/{id}/desactiver")
	public User desactiver(@PathVariable Long id) {
		return userService.setActif(id, false);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
		userService.delete(id, principal.getName());
		return ResponseEntity.noContent().build();
	}
}
