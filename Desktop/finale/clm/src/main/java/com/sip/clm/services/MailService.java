package com.sip.clm.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.sip.clm.entities.Facture;
import com.sip.clm.entities.Paiement;
import com.sip.clm.entities.RendezVous;
import com.sip.clm.entities.User;

@Service
public class MailService {

	private static final Logger log = LoggerFactory.getLogger(MailService.class);

	@Autowired(required = false)
	private JavaMailSender mailSender;

	@Value("${clm.mail.enabled:false}")
	private boolean enabled;

	@Value("${clm.admin.email}")
	private String adminEmail;

	@Value("${spring.mail.username:}")
	private String from;

	/** Envoie un email. Si clm.mail.enabled=false, le message est seulement ecrit dans la console. */
	public void send(String to, String subject, String text) {
		if (to == null || to.trim().isEmpty()) {
			return;
		}
		if (!enabled) {
			log.info("[EMAIL desactive] A: {} | Sujet: {} | {}", to, subject, text);
			return;
		}
		if (mailSender == null) {
			throw new IllegalStateException("Le service SMTP est activé mais aucun JavaMailSender n'est configuré");
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			if (from != null && !from.isEmpty()) {
				message.setFrom(from);
			}
			message.setTo(to);
			message.setSubject(subject);
			message.setText(text);
			mailSender.send(message);
		} catch (MailException e) {
			log.error("Échec d'envoi de l'email à {} : {}", to, e.getMessage());
			throw e;
		}
	}

	/** Inscription d'un nouvel utilisateur : email envoye a l'admin pour validation. */
	public void notifyAdminNewUser(User user) {
		send(adminEmail, "Clinic Manager - Nouvelle inscription a valider",
				"Un nouvel utilisateur s'est inscrit et attend la validation de son compte :\n\n" + "Nom : "
						+ user.getNom() + " " + user.getPrenom() + "\nEmail : " + user.getEmail() + "\nTel : "
						+ user.getTel() + "\n\nConnectez-vous en tant qu'administrateur pour activer ce compte "
						+ "(Utilisateurs > Activer).");
	}

	public void notifyUserActivated(User user) {
		send(user.getEmail(), "Clinic Manager - Compte activé",
				"Bonjour " + user.getPrenom() + ",\n\nVotre compte a été activé. Vous pouvez maintenant vous connecter.");
	}

	/** Confirmation de rendez-vous envoyee au patient (optionnel). */
	public void notifyRendezVous(RendezVous r) {
		if (r.getPatient() == null || r.getPatient().getEmail() == null) {
			return;
		}
		send(r.getPatient().getEmail(), "Confirmation de votre rendez-vous",
				"Bonjour " + r.getPatient().getPrenom() + " " + r.getPatient().getNom() + ",\n\nVotre rendez-vous avec "
						+ "Dr " + r.getMedecin().getNom() + " est enregistré le " + r.getDate() + " a "
						+ r.getHeureDebut() + ".\n\nCordialement,\nClinic Manager");
	}

	/** Alerte facture en retard envoyee a l'admin (optionnel). */
	public void alertFactureEnRetard(Facture f) {
		send(adminEmail, "Facture en retard de paiement : " + f.getNumero(),
				"La facture " + f.getNumero() + " (échéance " + f.getDateEcheance() + ") n'est pas soldee. "
						+ "Reste à payer : " + f.getResteAPayer());
	}

	public void notifyFactureCreated(Facture f) {
		if (f.getPatient() == null) {
			return;
		}
		send(f.getPatient().getEmail(), "Votre facture " + f.getNumero(),
				"Bonjour " + f.getPatient().getPrenom() + " " + f.getPatient().getNom() + ",\n\n"
						+ "Votre facture " + f.getNumero() + " d'un montant de " + f.getMontantTTC()
						+ " est disponible. Échéance : " + f.getDateEcheance() + ".\n\nClinic Manager");
	}

	public void notifyPaiement(Paiement paiement) {
		Facture facture = paiement.getFacture();
		if (facture == null || facture.getPatient() == null) {
			return;
		}
		send(facture.getPatient().getEmail(), "Confirmation de paiement - " + facture.getNumero(),
				"Nous avons enregistré votre paiement de " + paiement.getMontant() + " pour la facture "
						+ facture.getNumero() + ". Reste à payer : " + facture.getResteAPayer() + ".");
	}
}
