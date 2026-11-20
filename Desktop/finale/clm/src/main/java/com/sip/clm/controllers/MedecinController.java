package com.sip.clm.controllers;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sip.clm.entities.Medecin;
import com.sip.clm.entities.RendezVous;
import com.sip.clm.services.MedecinService;
import com.sip.clm.util.PageUtil;

@RestController
@RequestMapping("/api/medecins")
public class MedecinController {

	@Autowired
	private MedecinService medecinService;

	@GetMapping
	public Page<Medecin> list(@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return medecinService.search(search, PageUtil.of(page, size));
	}

	/** Liste complete (sans pagination), utile pour les listes deroulantes. */
	@GetMapping("/all")
	public List<Medecin> all() {
		return medecinService.findAll();
	}

	@PostMapping
	public ResponseEntity<Medecin> create(@Valid @RequestBody Medecin medecin) {
		return ResponseEntity.status(HttpStatus.CREATED).body(medecinService.create(medecin));
	}

	@GetMapping("/{id}")
	public Medecin get(@PathVariable Long id) {
		return medecinService.get(id);
	}

	@PutMapping("/{id}")
	public Medecin update(@PathVariable Long id, @Valid @RequestBody Medecin medecin) {
		return medecinService.update(id, medecin);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		medecinService.delete(id);
		return ResponseEntity.noContent().build();
	}

	/** GET /api/medecins/{id}/planning?date=2026-11-20 (date du jour par defaut). */
	@GetMapping("/{id}/planning")
	public List<RendezVous> planning(@PathVariable Long id,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return medecinService.planning(id, date != null ? date : LocalDate.now());
	}
}
