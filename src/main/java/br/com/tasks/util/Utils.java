package br.com.tasks.util;

import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import br.com.tasks.model.Session;
import br.com.tasks.model.User;
import br.com.tasks.model.UserResponseEntity;

public class Utils {

	public static Response createResponse(Status status, String message) {
		return Response
				.status(status)
				.entity(new UserResponseEntity(status.getStatusCode(), message))
				.type(MediaType.APPLICATION_JSON).build();
	}
	
	public static Session createSession(User user, Boolean remember) {
		Session session = new Session();
		Calendar calendar = Calendar.getInstance();
		session.setId(calendar.getTimeInMillis());
		session.setCreation(calendar.getTime());
		if (!remember) {
			Date datePlus10Min = new Date(calendar.getTimeInMillis() + (15 * 60000));
			session.setExpiration(datePlus10Min);
		}
		User returnUser = new User();
		returnUser.setEmail(user.getEmail());
		returnUser.setFirstname(user.getFirstname());
		returnUser.setSurname(user.getSurname());
		returnUser.setUsername(user.getUsername());
		returnUser.setRole(user.getRole());
		session.setUser(returnUser);
		return session;
	}
	
}
