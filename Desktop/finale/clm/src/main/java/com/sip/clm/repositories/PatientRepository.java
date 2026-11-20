package com.sip.clm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sip.clm.entities.*;

public interface PatientRepository extends JpaRepository<Patient, Long> {

	boolean existsByCin(String cin);

	Optional<Patient> findByCin(String cin);

	@Query("select p from Patient p where lower(p.nom) like lower(concat('%', :q, '%')) "
			+ "or lower(p.prenom) like lower(concat('%', :q, '%')) or p.cin like concat('%', :q, '%')")
	Page<Patient> search(@Param("q") String q, Pageable pageable);

}
