package br.com.tasks.rest;

import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import br.com.tasks.model.Confirmation;
import br.com.tasks.model.Login;
import br.com.tasks.model.Session;
import br.com.tasks.model.Task;
import br.com.tasks.model.User;
import br.com.tasks.model.UserRole;
import br.com.tasks.util.Utils;

@Stateless
@Path("/users")
public class UserEndpoint {

	private static final String SECURITY_KEY = "05l0518Zj6S7pGl8XoeGNhymyrZXSscO";
	private static final String SECURITY_KEY2 = "CzGJg6VGf3vk0L9l5N88qmQ571TM961s";
	
	@PersistenceContext(unitName = "tasks-webapp-persistence-unit")
	private EntityManager em;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response create(User entity) {
		em.persist(entity);
		URI createdResource = UriBuilder.fromResource(UserEndpoint.class)
				.path(String.valueOf(entity.getId())).build();
		return Response.created(createdResource).build();
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Long userId) {
		User user = em.find(User.class, userId);
		if (user == null) {
			return Utils.createResponse(Status.NOT_FOUND, "User " + userId
					+ " not found.");
		}
		em.remove(user);
		return Response.noContent().build();
	}

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findById(@PathParam("id") Long userId) {
		String hql = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.tasks WHERE u.id = :entityId ORDER BY u.id";
		TypedQuery<User> findByIdQuery = em.createQuery(hql, User.class);
		findByIdQuery.setParameter("entityId", userId);
		User user;
		try {
			user = findByIdQuery.getSingleResult();
		} catch (NoResultException nre) {
			return Utils.createResponse(Status.NOT_FOUND, "User " + userId
					+ " not found.");
		}
		return Response.ok(user).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> listAll(@QueryParam("start") Integer startPosition,
			@QueryParam("max") Integer maxResult) {
		String hql = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.tasks ORDER BY u.id";
		TypedQuery<User> findAllQuery = em.createQuery(hql, User.class);
		if (startPosition != null) {
			findAllQuery.setFirstResult(startPosition);
		}
		if (maxResult != null) {
			findAllQuery.setMaxResults(maxResult);
		}
		final List<User> results = findAllQuery.getResultList();
		return results;
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("id") Long userId, User entity) {
		try {
			entity = em.merge(entity);
		} catch (OptimisticLockException e) {
			return Response.status(Response.Status.CONFLICT)
					.entity(e.getEntity()).build();
		}
		return Response.noContent().build();
	}

	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(Login login) {
		String hql = "SELECT DISTINCT u FROM User u WHERE u.username = :username AND u.active = true AND u.confirmed = true";
		TypedQuery<User> query = em.createQuery(hql, User.class);
		query.setParameter("username", login.getUsername());
		List<User> users = query.getResultList();
		if (users.isEmpty()) {
			return Utils.createResponse(Status.NOT_FOUND,
					"User '" + login.getUsername() + "' not found.");
		}
		User user = users.get(0);
		String userPassword = user.getPassword();
		if (!login.getHash().equals(userPassword)) {
			return Utils.createResponse(Status.FORBIDDEN,
					"Invalid password.");
		}
		Session session = Utils.createSession(user, login.getRemember());
		return Response.status(Status.OK).entity(session)
				.type(MediaType.APPLICATION_JSON).build();
	}

	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(User user) {
		String hql = "SELECT DISTINCT u FROM User u WHERE u.username = :username AND u.active = true";
		TypedQuery<User> query = em.createQuery(hql, User.class);
		query.setParameter("username", user.getUsername());
		List<User> users = query.getResultList();
		if (!users.isEmpty()) {
			return Utils.createResponse(Status.FORBIDDEN,
					"User '" + user.getUsername() + "' already exists.");
		}
		hql = "SELECT DISTINCT r FROM UserRole r WHERE r.name = 'restrict'";
		TypedQuery<UserRole> roleQuery = em.createQuery(hql, UserRole.class);
		List<UserRole> roles = roleQuery.getResultList();
		UserRole role;
		if (roles.isEmpty()) {
			role = new UserRole();
			role.setName("restrict");
			em.persist(role);
		} else {
			role = roles.get(0);
		}
		user.setRole(role);
		user.setPassword(getMD5(user.getPassword()));
		em.persist(user);
		sendEmailConfirmation(user);
		return Response.status(Status.CREATED).build();
	}
	
	@POST
	@Path("/confirm")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response confirm(Confirmation confirmation) {
		String username = confirmation.getUsername();
		String key = confirmation.getKey();
		Long expiration = confirmation.getExpiration();
		if (Calendar.getInstance().getTimeInMillis() > expiration) {
			return Utils.createResponse(Status.BAD_REQUEST, "This confirmation link has expired. Please register or resend the confirmation e-mail.");
		}
		String passwordHash = key.replace(SECURITY_KEY, "").replace(SECURITY_KEY2, "");
		String hql = "SELECT DISTINCT u FROM User u WHERE u.username = :username AND u.active = true AND u.confirmed = false";
		TypedQuery<User> query = em.createQuery(hql, User.class);
		query.setParameter("username", username);
		List<User> users = query.getResultList();
		if (users.isEmpty()) {
			return Utils.createResponse(Status.BAD_REQUEST, "The user '" + username + "' has already confirmed it's registration.");
		}
		User user = users.get(0);
		String userPassword = user.getPassword();
		if (!userPassword.equals(passwordHash)) {
			return Utils.createResponse(Status.FORBIDDEN, "This confirmation link is invalid.");
		}
		user.setConfirmed(true);
		em.persist(user);
		return Response.status(Status.ACCEPTED).build();
	}

	private void sendEmailConfirmation(User user) {
		final String username = "andrey.heur@gmail.com";
		final String password = "xapijeysfqjukiuh";
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		javax.mail.Session session = javax.mail.Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });
 
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(user.getEmail()));
			message.setSubject("Registration confirmation at Tasks App");
			message.setContent("<h3>Hello" + (isBlank(user.getFirstname()) ? ", there!" : ", " + user.getFirstname() + "!") + "</h3>"
					+ "<br>"
					+ "Here's your link for the registration confirm at Tasks App:"
					+ "<br>"
					+ "<strong>" + generateConfirmationLink(user) + "</strong>"
					+ "<br><br>"
					+ "Hope to see you soon! Bye bye."
					+ "<br><br>"
					+ "The Tasks App team.", "text/html");
			Transport.send(message);
			System.out.println("Done");
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	private String generateConfirmationLink(User user) {
		String userPassword = user.getPassword();
		String fullKey = SECURITY_KEY + userPassword + SECURITY_KEY2;
		Long expirationTime = Calendar.getInstance().getTimeInMillis();
		expirationTime = expirationTime + (12 * 60 * 60 * 1000);
		return "http://localhost:8000/#/confirm?u=" + user.getUsername() + "&a=" + fullKey + "&e=" + expirationTime;
	}
	
	private String getMD5(String value) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(value.getBytes(), 0, value.length());
			return new BigInteger(1, m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	@GET
	@Path("/seed")
	public Response seed() {
		UserRole role1 = new UserRole();
		role1.setName("admin");
		em.persist(role1);
		UserRole role2 = new UserRole();
		role2.setName("restrict");
		em.persist(role2);

		Task task1 = new Task();
		task1.setDescription("Finish tasks app");
		task1.setCreation(new Date());
		em.persist(task1);
		Task task2 = new Task();
		task2.setDescription("Distribute tasks app");
		task2.setCreation(new Date());
		em.persist(task2);

		Set<Task> tasks = new HashSet<>();
		tasks.add(task1);
		tasks.add(task2);

		User adminSeed = new User();
		adminSeed.setFirstname("Admin");
		adminSeed.setUsername("admin");
		adminSeed.setEmail("admin@localhost.com");
		adminSeed.setPassword("823560");
		adminSeed.setRole(role1);
		em.persist(adminSeed);

		User userSeed = new User();
		userSeed.setFirstname("Andrey");
		userSeed.setSurname("Luiz");
		userSeed.setUsername("andrey");
		userSeed.setEmail("andreyluiz@live.it");
		userSeed.setPassword("1494");
		userSeed.setRole(role2);
		userSeed.setTasks(tasks);
		em.persist(userSeed);

		return Response.ok().build();
	}
	
	public boolean isBlank(String value) {
		return value == null || value.equals("");
	}

}
