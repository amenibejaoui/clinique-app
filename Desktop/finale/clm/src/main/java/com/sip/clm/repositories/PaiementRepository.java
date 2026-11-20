package com.sip.clm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.sip.clm.entities.*;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

	List<Paiement> findByFactureIdOrderByDatePaiementDesc(Long factureId);

}
