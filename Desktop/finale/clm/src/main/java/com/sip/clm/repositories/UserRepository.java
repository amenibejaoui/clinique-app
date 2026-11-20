package com.sip.clm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sip.clm.entities.*;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	List<User> findByPatientId(Long patientId);

	List<User> findByMedecinId(Long medecinId);

	@Query("select u from User u where lower(u.nom) like lower(concat('%', :q, '%')) "
			+ "or lower(u.prenom) like lower(concat('%', :q, '%')) or lower(u.email) like lower(concat('%', :q, '%'))")
	Page<User> search(@Param("q") String q, Pageable pageable);

}
