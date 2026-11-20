package com.sip.clm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sip.clm.entities.*;

public interface FactureRepository extends JpaRepository<Facture, Long> {

	Optional<Facture> findTopByNumeroStartingWithOrderByNumeroDesc(String prefix);

	List<Facture> findByPatientIdOrderByDateEmissionDesc(Long patientId);

	boolean existsByPatientId(Long patientId);

	boolean existsByRendezVousId(Long rendezVousId);

	List<Facture> findByDateEcheanceBeforeAndStatutIn(LocalDate date, Collection<StatutFacture> statuts);

	@Query("select f from Facture f where (:statut is null or f.statut = :statut)")
	Page<Facture> filter(@Param("statut") StatutFacture statut, Pageable pageable);

	@Query("select sum(f.montantTTC) from Facture f where f.statut <> :exclu "
			+ "and f.dateEmission >= :debut and f.dateEmission <= :fin")
	Double sumTTC(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin,
			@Param("exclu") StatutFacture exclu);

	@Query("select sum(f.resteAPayer) from Facture f where f.statut in (:statuts)")
	Double sumResteAPayer(@Param("statuts") Collection<StatutFacture> statuts);

}
