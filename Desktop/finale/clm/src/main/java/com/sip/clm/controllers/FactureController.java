package com.sip.clm.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.sip.clm.entities.Facture;
import com.sip.clm.entities.Paiement;
import com.sip.clm.entities.StatutFacture;
import com.sip.clm.services.FacturePdfService;
import com.sip.clm.services.FactureService;
import com.sip.clm.util.PageUtil;

@RestController
@RequestMapping("/api")
public class FactureController {

	@Autowired
	private FactureService factureService;

	@Autowired
	private FacturePdfService facturePdfService;

	/** GET /api/factures?statut=EN_ATTENTE&page=0&size=10 */
	@GetMapping("/factures")
	public Page<Facture> list(@RequestParam(required = false) StatutFacture statut,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return factureService.filter(statut, PageUtil.of(page, size));
	}

	@PostMapping("/factures")
	public ResponseEntity<Facture> create(@Valid @RequestBody Facture facture) {
		return ResponseEntity.status(HttpStatus.CREATED).body(factureService.create(facture));
	}

	@GetMapping("/factures/{id}")
	public Facture get(@PathVariable Long id) {
		return factureService.get(id);
	}

	@PutMapping("/factures/{id}")
	public Facture update(@PathVariable Long id, @Valid @RequestBody Facture facture) {
		return factureService.update(id, facture);
	}

	@PatchMapping("/factures/{id}/annuler")
	public Facture annuler(@PathVariable Long id) {
		return factureService.annuler(id);
	}

	@DeleteMapping("/factures/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		factureService.delete(id);
		return ResponseEntity.noContent().build();
	}

	/** GET /api/factures/{id}/pdf : facture imprimable. */
	@GetMapping("/factures/{id}/pdf")
	public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
		Facture f = factureService.get(id);
		byte[] content = facturePdfService.generer(f);
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + f.getNumero() + ".pdf\"")
				.body(content);
	}

	/** POST /api/factures/{id}/paiements */
	@PostMapping("/factures/{id}/paiements")
	public ResponseEntity<Paiement> addPaiement(@PathVariable Long id, @Valid @RequestBody Paiement paiement) {
		return ResponseEntity.status(HttpStatus.CREATED).body(factureService.addPaiement(id, paiement));
	}

	/** GET /api/paiements */
	@GetMapping("/paiements")
	public Page<Paiement> paiements(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return factureService.findPaiements(PageUtil.of(page, size));
	}
}
