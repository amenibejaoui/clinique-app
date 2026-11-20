package com.sip.clm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.sip.clm.entities.*;

public interface RoleRepository extends JpaRepository<Role, Long> {

	Optional<Role> findByLibelle(String libelle);

}
