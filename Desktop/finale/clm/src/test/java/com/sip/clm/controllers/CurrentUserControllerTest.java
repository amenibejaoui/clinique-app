package com.sip.clm.controllers;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sip.clm.entities.Consultation;
import com.sip.clm.entities.Medecin;
import com.sip.clm.entities.Patient;
import com.sip.clm.entities.Role;
import com.sip.clm.entities.RendezVous;
import com.sip.clm.entities.User;
import com.sip.clm.repositories.ConsultationRepository;
import com.sip.clm.repositories.FactureRepository;
import com.sip.clm.repositories.RendezVousRepository;
import com.sip.clm.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CurrentUserControllerTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RendezVousRepository rendezVousRepository;

	@Mock
	private ConsultationRepository consultationRepository;

	@Mock
	private FactureRepository factureRepository;

	@InjectMocks
	private CurrentUserController controller;

	@Test
	public void patientAppointmentsAreLoadedOnlyForTheLinkedPatient() {
		User user = userWithRole("PATIENT");
		Patient patient = new Patient();
		patient.setId(42L);
		user.setPatient(patient);
		Principal principal = () -> "patient@example.test";
		ArrayList<RendezVous> appointments = new ArrayList<>();

		when(userRepository.findByEmail(principal.getName())).thenReturn(Optional.of(user));
		when(rendezVousRepository.findByPatientIdOrderByDateDescHeureDebutDesc(42L)).thenReturn(appointments);

		assertSame(appointments, controller.rendezVous(principal));
		verify(rendezVousRepository).findByPatientIdOrderByDateDescHeureDebutDesc(42L);
	}

	@Test
	public void doctorConsultationsAreLoadedOnlyForTheLinkedDoctor() {
		User user = userWithRole("MEDECIN");
		Medecin medecin = new Medecin();
		medecin.setId(17L);
		user.setMedecin(medecin);
		Principal principal = () -> "doctor@example.test";
		ArrayList<Consultation> consultations = new ArrayList<>();

		when(userRepository.findByEmail(principal.getName())).thenReturn(Optional.of(user));
		when(consultationRepository.findByRendezVousMedecinIdOrderByDateConsultationDesc(17L))
				.thenReturn(consultations);

		assertSame(consultations, controller.consultations(principal));
		verify(consultationRepository).findByRendezVousMedecinIdOrderByDateConsultationDesc(17L);
	}

	private User userWithRole(String role) {
		User user = new User();
		user.setRoles(new java.util.HashSet<>(Arrays.asList(new Role(role))));
		return user;
	}
}
