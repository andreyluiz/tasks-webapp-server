package br.com.tasks.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Confirmation {

	private String username;
	private String key;
	private Long expiration;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Long getExpiration() {
		return expiration;
	}

	public void setExpiration(Long expiration) {
		this.expiration = expiration;
	}

}
