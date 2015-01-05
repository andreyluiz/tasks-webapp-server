package br.com.tasks.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Login {

	private String username;
	private String hash;
	private Boolean remember;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Boolean getRemember() {
		return remember;
	}

	public void setRemember(Boolean remember) {
		this.remember = remember;
	}

}
