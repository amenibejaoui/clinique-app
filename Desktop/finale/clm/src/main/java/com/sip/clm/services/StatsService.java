package com.sip.clm.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.sip.clm.entities.StatutFacture;
import com.sip.clm.entities.StatutRendezVous;
import com.sip.clm.repositories.ConsultationRepository;
import com.sip.clm.repositories.FactureRepository;
import com.sip.clm.repositories.MedecinRepository;
import com.sip.clm.repositories.PatientRepository;
import com.sip.clm.repositories.RendezVousRepository;
import com.sip.clm.util.MoneyUtil;

@Service
public class StatsService {

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private MedecinRepository medecinRepository;

	@Autowired
	private RendezVousRepository rendezVousRepository;

	@Autowired
	private FactureRepository factureRepository;

	@Autowired
	private ConsultationRepository consultationRepository;

	/**
	 * @param admin true : statistiques complètes (avec chiffre d'affaires) ; false (secrétaire) : sans chiffre
	 *              d'affaires.
	 */
	public Map<String, Object> stats(boolean admin) {
		Map<String, Object> s = new LinkedHashMap<>();
		s.put("nombrePatients", patientRepository.count());
		s.put("nombreMedecins", medecinRepository.count());
		s.put("rendezVousDuJour", rendezVousRepository.countByDate(LocalDate.now()));

		Map<String, Long> parStatut = new LinkedHashMap<>();
		for (StatutRendezVous st : StatutRendezVous.values()) {
			parStatut.put(st.name(), rendezVousRepository.countByStatut(st));
		}
		s.put("rendezVousParStatut", parStatut);

		if (admin) {
			LocalDate debut = LocalDate.now().withDayOfMonth(1);
			LocalDate fin = debut.plusMonths(1).minusDays(1);
			Double ca = factureRepository.sumTTC(debut, fin, StatutFacture.ANNULEE);
			s.put("chiffreAffairesMois", MoneyUtil.round(ca == null ? 0 : ca));
		}

		Double impaye = factureRepository.sumResteAPayer(
				Arrays.asList(StatutFacture.EN_ATTENTE, StatutFacture.PARTIELLEMENT_PAYEE));
		s.put("montantFacturesImpayees", MoneyUtil.round(impaye == null ? 0 : impaye));

		List<Map<String, Object>> top = new ArrayList<>();
		for (Object[] row : consultationRepository.topMedecins(PageRequest.of(0, 5))) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("medecinId", row[0]);
			m.put("nom", row[1]);
			m.put("prenom", row[2]);
			m.put("nombreConsultations", row[3]);
			top.add(m);
		}
		s.put("topMedecins", top);
		return s;
	}
}
