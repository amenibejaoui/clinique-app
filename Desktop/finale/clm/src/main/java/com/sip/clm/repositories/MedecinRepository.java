package com.sip.clm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sip.clm.entities.*;

public interface MedecinRepository extends JpaRepository<Medecin, Long> {

	@Query("select m from Medecin m where lower(m.nom) like lower(concat('%', :q, '%')) "
			+ "or lower(m.prenom) like lower(concat('%', :q, '%')) or lower(m.specialite) like lower(concat('%', :q, '%'))")
	Page<Medecin> search(@Param("q") String q, Pageable pageable);

}
