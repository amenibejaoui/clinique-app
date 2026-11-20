package com.sip.clm.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonAlias;

public class JwtRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Le "username" est l'email de l'utilisateur. */
	@JsonAlias({ "email" })
	private String username;
	private String password;

	// default constructor for JSON Parsing
	public JwtRequest() {
	}

	public JwtRequest(String username, String password) {
		this.setUsername(username);
		this.setPassword(password);
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
