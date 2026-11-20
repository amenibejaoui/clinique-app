package com.sip.clm.exceptions;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, Map<String, String> errors) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now().toString());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		if (errors != null && !errors.isEmpty()) {
			body.put("errors", errors);
		}
		return ResponseEntity.status(status).body(body);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
		return build(ex.getStatus(), ex.getMessage(), null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
			errors.put(fe.getField(), fe.getDefaultMessage());
		}
		return build(HttpStatus.BAD_REQUEST, "Validation échouée", errors);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, Object>> handleConstraint(ConstraintViolationException ex) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
			errors.put(v.getPropertyPath().toString(), v.getMessage());
		}
		return build(HttpStatus.BAD_REQUEST, "Validation échouée", errors);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException ex) {
		return build(HttpStatus.CONFLICT, "Opération impossible : contrainte d'intégrité ou valeur déjà existante", null);
	}
}
