package com.sip.clm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.sip.clm.entities.*;

public interface DossierMedicalRepository extends JpaRepository<DossierMedical, Long> {

	Optional<DossierMedical> findByPatientId(Long patientId);

}
