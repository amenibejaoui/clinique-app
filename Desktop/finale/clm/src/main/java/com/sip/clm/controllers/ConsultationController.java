package com.sip.clm.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

import com.sip.clm.entities.Consultation;
import com.sip.clm.services.ConsultationService;
import com.sip.clm.util.PageUtil;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

	@Autowired
	private ConsultationService consultationService;

	@GetMapping
	public Page<Consultation> list(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return consultationService.findAll(PageUtil.of(page, size));
	}

	@PostMapping
	public ResponseEntity<Consultation> create(@Valid @RequestBody Consultation consultation) {
		return ResponseEntity.status(HttpStatus.CREATED).body(consultationService.create(consultation));
	}

	@GetMapping("/{id}")
	public Consultation get(@PathVariable Long id) {
		return consultationService.get(id);
	}

	@PutMapping("/{id}")
	public Consultation update(@PathVariable Long id, @RequestBody Consultation consultation) {
		return consultationService.update(id, consultation);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		consultationService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
