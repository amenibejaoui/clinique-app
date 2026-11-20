package com.sip.clm.services;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.sip.clm.entities.Facture;
import com.sip.clm.entities.LigneFacture;
import com.sip.clm.entities.Paiement;
import com.sip.clm.exceptions.BusinessException;

@Service
public class FacturePdfService {

	public byte[] generer(Facture f) {
		try {
			Font titre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
			Font gras = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
			Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
			PdfWriter.getInstance(doc, out);
			doc.open();

			doc.add(new Paragraph("Clinic Manager", titre));
			doc.add(new Paragraph("FACTURE " + f.getNumero(), gras));
			doc.add(new Paragraph("Date d'émission : " + f.getDateEmission() + "    Échéance : " + f.getDateEcheance(),
					normal));
			doc.add(new Paragraph("Statut : " + f.getStatut(), normal));
			doc.add(new Paragraph(" "));

			if (f.getPatient() != null) {
				doc.add(new Paragraph("Patient : " + f.getPatient().getNom() + " " + f.getPatient().getPrenom(), gras));
				if (f.getPatient().getCin() != null) {
					doc.add(new Paragraph("CIN : " + f.getPatient().getCin(), normal));
				}
			}
			if (f.getRendezVous() != null) {
				doc.add(new Paragraph("Rendez-vous du " + f.getRendezVous().getDate() + " avec Dr "
						+ f.getRendezVous().getMedecin().getNom(), normal));
			}
			doc.add(new Paragraph(" "));

			PdfPTable table = new PdfPTable(4);
			table.setWidthPercentage(100);
			table.setWidths(new float[] { 5f, 1.5f, 2.5f, 2.5f });
			header(table, "Désignation", gras, Element.ALIGN_LEFT);
			header(table, "Qté", gras, Element.ALIGN_RIGHT);
			header(table, "Prix unitaire", gras, Element.ALIGN_RIGHT);
			header(table, "Total", gras, Element.ALIGN_RIGHT);
			for (LigneFacture l : f.getLignes()) {
				cell(table, l.getDesignation(), normal, Element.ALIGN_LEFT);
				cell(table, String.valueOf(l.getQuantite()), normal, Element.ALIGN_RIGHT);
				cell(table, fmt(l.getPrixUnitaire()), normal, Element.ALIGN_RIGHT);
				cell(table, fmt(l.getTotalLigne()), normal, Element.ALIGN_RIGHT);
			}
			doc.add(table);
			doc.add(new Paragraph(" "));

			right(doc, "Total HT : " + fmt(f.getMontantHT()), normal);
			right(doc, "TVA (" + fmt(f.getTauxTVA() * 100) + " %) : " + fmt(f.getMontantTVA()), normal);
			right(doc, "Total TTC : " + fmt(f.getMontantTTC()), gras);
			right(doc, "Déjà payé : " + fmt(f.getMontantPaye()), normal);
			right(doc, "Reste à payer : " + fmt(f.getResteAPayer()), gras);

			if (f.getPaiements() != null && !f.getPaiements().isEmpty()) {
				doc.add(new Paragraph(" "));
				doc.add(new Paragraph("Paiements", gras));
				for (Paiement p : f.getPaiements()) {
					doc.add(new Paragraph(p.getDatePaiement() + " - " + p.getModePaiement() + " - " + fmt(p.getMontant())
							+ (p.getReference() != null ? " (réf. " + p.getReference() + ")" : ""), normal));
				}
			}

			doc.close();
			return out.toByteArray();
		} catch (DocumentException e) {
			throw new BusinessException("Erreur lors de la génération du PDF : " + e.getMessage());
		}
	}

	private String fmt(double v) {
		return String.format(java.util.Locale.FRANCE, "%.3f", v);
	}

	private void header(PdfPTable t, String text, Font font, int align) {
		PdfPCell c = new PdfPCell(new Phrase(text, font));
		c.setHorizontalAlignment(align);
		c.setPadding(5);
		t.addCell(c);
	}

	private void cell(PdfPTable t, String text, Font font, int align) {
		PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, font));
		c.setHorizontalAlignment(align);
		c.setPadding(5);
		t.addCell(c);
	}

	private void right(Document doc, String text, Font font) throws DocumentException {
		Paragraph p = new Paragraph(text, font);
		p.setAlignment(Element.ALIGN_RIGHT);
		doc.add(p);
	}
}
