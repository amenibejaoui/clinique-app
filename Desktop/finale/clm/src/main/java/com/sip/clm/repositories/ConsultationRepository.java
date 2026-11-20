package com.sip.clm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.sip.clm.entities.*;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

	boolean existsByRendezVousId(Long rendezVousId);

	List<Consultation> findByRendezVousPatientIdOrderByDateConsultationDesc(Long patientId);

	List<Consultation> findByRendezVousMedecinIdOrderByDateConsultationDesc(Long medecinId);

	@Query("select m.id, m.nom, m.prenom, count(c) from Consultation c join c.rendezVous r join r.medecin m "
			+ "group by m.id, m.nom, m.prenom order by count(c) desc")
	List<Object[]> topMedecins(Pageable pageable);

}
