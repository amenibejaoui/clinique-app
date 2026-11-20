package com.sip.clm.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sip.clm.services.StatsService;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

	@Autowired
	private StatsService statsService;

	/** ADMIN : statistiques completes ; SECRETAIRE : sans chiffre d'affaires. */
	@GetMapping
	public Map<String, Object> stats(Authentication authentication) {
		boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		return statsService.stats(admin);
	}
}
