package br.com.tasks.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserResponseEntity {

    private int status = 500;
    private String message;
    
    public UserResponseEntity(int status, String message) {
	this.status = status;
	this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}
