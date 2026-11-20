package com.sip.clm.controllers;

import java.util.List;

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

import com.sip.clm.entities.DossierMedical;
import com.sip.clm.entities.Facture;
import com.sip.clm.entities.Patient;
import com.sip.clm.entities.RendezVous;
import com.sip.clm.services.PatientService;
import com.sip.clm.util.PageUtil;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

	@Autowired
	private PatientService patientService;

	/** GET /api/patients?search=&page=0&size=10 : recherche par nom, prenom ou CIN. */
	@GetMapping
	public Page<Patient> list(@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return patientService.search(search, PageUtil.of(page, size));
	}

	/** Liste complete (sans pagination), utile pour les listes deroulantes. */
	@GetMapping("/all")
	public List<Patient> all() {
		return patientService.findAll();
	}

	@PostMapping
	public ResponseEntity<Patient> create(@Valid @RequestBody Patient patient) {
		return ResponseEntity.status(HttpStatus.CREATED).body(patientService.create(patient));
	}

	@GetMapping("/{id}")
	public Patient get(@PathVariable Long id) {
		return patientService.get(id);
	}

	@PutMapping("/{id}")
	public Patient update(@PathVariable Long id, @Valid @RequestBody Patient patient) {
		return patientService.update(id, patient);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		patientService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/rendezvous")
	public List<RendezVous> rendezVous(@PathVariable Long id) {
		return patientService.rendezVous(id);
	}

	@GetMapping("/{id}/factures")
	public List<Facture> factures(@PathVariable Long id) {
		return patientService.factures(id);
	}

	@GetMapping("/{id}/dossier")
	public DossierMedical getDossier(@PathVariable Long id) {
		return patientService.getDossier(id);
	}

	@PutMapping("/{id}/dossier")
	public DossierMedical updateDossier(@PathVariable Long id, @RequestBody DossierMedical dossier) {
		return patientService.updateDossier(id, dossier);
	}
}
