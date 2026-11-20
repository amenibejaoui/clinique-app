package com.sip.clm.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Autowired
	private UserDetailsService jwtUserDetailsService;

	@Autowired
	private JwtRequestFilter jwtRequestFilter;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${clm.cors.origins:http://localhost:4200}")
	private String corsOrigins;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(Arrays.asList(corsOrigins.split(",")));
		config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(Arrays.asList("*"));
		config.setExposedHeaders(Arrays.asList("Content-Disposition"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf().disable().cors().and().authorizeRequests()
				// preflight + login : accès libre
				.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.antMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

				// ADMIN uniquement (accepte "ADMIN" et "ROLE_ADMIN")
				.antMatchers("/api/auth/register").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
				.antMatchers("/api/users/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")

				// accès personnels (filtrés par utilisateur dans le contrôleur)
				.antMatchers("/api/me/**").hasAnyAuthority(
						"ADMIN", "ROLE_ADMIN",
						"SECRETAIRE", "ROLE_SECRETAIRE",
						"MEDECIN", "ROLE_MEDECIN",
						"PATIENT", "ROLE_PATIENT")

				// dossiers cliniques : ADMIN et SECRETAIRE
				// (tout autre rôle est refusé sur /api/**, donc plus besoin de denyAll)
				.antMatchers("/api/**").hasAnyAuthority(
						"ADMIN", "ROLE_ADMIN",
						"SECRETAIRE", "ROLE_SECRETAIRE")

				// le reste (build Angular dans /static) est public
				.anyRequest().permitAll().and()

				.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
	}
}