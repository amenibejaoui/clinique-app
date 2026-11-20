package com.sip.clm.controllers;

import java.time.LocalDate;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.sip.clm.entities.RendezVous;
import com.sip.clm.entities.StatutRendezVous;
import com.sip.clm.models.StatutRequest;
import com.sip.clm.services.RendezVousService;

@RestController
@RequestMapping("/api/rendezvous")
public class RendezVousController {

	@Autowired
	private RendezVousService rendezVousService;

	/** GET /api/rendezvous?date=2026-11-20&medecinId=1&statut=PLANIFIE&page=0&size=10 (tous les filtres optionnels). */
	@GetMapping
	public Page<RendezVous> list(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) Long medecinId, @RequestParam(required = false) StatutRendezVous statut,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		// le tri (date desc, heure desc) est defini dans la requete JPQL
		return rendezVousService.filter(date, medecinId, statut,
				PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
	}

	@PostMapping
	public ResponseEntity<RendezVous> create(@Valid @RequestBody RendezVous rdv) {
		return ResponseEntity.status(HttpStatus.CREATED).body(rendezVousService.create(rdv));
	}

	@GetMapping("/{id}")
	public RendezVous get(@PathVariable Long id) {
		return rendezVousService.get(id);
	}

	@PutMapping("/{id}")
	public RendezVous update(@PathVariable Long id, @Valid @RequestBody RendezVous rdv) {
		return rendezVousService.update(id, rdv);
	}

	/** PATCH /api/rendezvous/{id}/statut  body : {"statut":"CONFIRME"} */
	@PatchMapping("/{id}/statut")
	public RendezVous changerStatut(@PathVariable Long id, @Valid @RequestBody StatutRequest request) {
		return rendezVousService.changerStatut(id, request.getStatut());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		rendezVousService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
