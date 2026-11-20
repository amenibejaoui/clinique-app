package com.sip.clm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sip.clm.entities.*;

public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

	List<RendezVous> findByPatientIdOrderByDateDescHeureDebutDesc(Long patientId);

	List<RendezVous> findByMedecinIdOrderByDateDescHeureDebutDesc(Long medecinId);

	List<RendezVous> findByMedecinIdAndDateOrderByHeureDebutAsc(Long medecinId, LocalDate date);

	List<RendezVous> findByMedecinIdAndDateAndStatutNotIn(Long medecinId, LocalDate date,
			Collection<StatutRendezVous> statuts);

	boolean existsByPatientId(Long patientId);

	boolean existsByMedecinId(Long medecinId);

	long countByDate(LocalDate date);

	long countByStatut(StatutRendezVous statut);

	@Query("select r from RendezVous r where (:date is null or r.date = :date) "
			+ "and (:medecinId is null or r.medecin.id = :medecinId) "
			+ "and (:statut is null or r.statut = :statut) order by r.date desc, r.heureDebut desc")
	Page<RendezVous> filter(@Param("date") LocalDate date, @Param("medecinId") Long medecinId,
			@Param("statut") StatutRendezVous statut, Pageable pageable);

}
