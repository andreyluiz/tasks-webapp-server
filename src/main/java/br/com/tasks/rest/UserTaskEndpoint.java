package br.com.tasks.rest;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import br.com.tasks.model.Task;
import br.com.tasks.model.User;
import br.com.tasks.model.UserResponseEntity;

@Stateless
@Path("/users")
public class UserTaskEndpoint {

    @PersistenceContext(unitName = "tasks-webapp-persistence-unit")
    private EntityManager em;
    
    @POST
    @Path("/{id:[0-9][0-9]*}/tasks")
    @Produces("application/json")
    public Response createTask(@PathParam("id") Long userId, Task task) {
	String hql = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.tasks WHERE u.id = :entityId ORDER BY u.id"; 
	TypedQuery<User> findByIdQuery = em.createQuery(hql, User.class);
	findByIdQuery.setParameter("entityId", userId);
	User user;
	try {
	    user = findByIdQuery.getSingleResult();
	} catch (NoResultException nre) {
	    user = null;
	}
	if (user == null) {
	    return Response.status(Status.NOT_FOUND)
		    	   .entity(new UserResponseEntity(Status.NOT_FOUND.getStatusCode(), "User " + userId + " not found."))
		    	   .type(MediaType.APPLICATION_JSON).build();
	}
	em.persist(task);
	user.getTasks().add(task);
	try {
	    user = em.merge(user);
	} catch (OptimisticLockException e) {
	    return Response.status(Response.Status.CONFLICT).entity(e.getEntity()).build();
	}
	return Response.noContent().build();
    }
    
    @GET
    @Path("/{id:[0-9][0-9]*}/tasks")
    @Produces("application/json")
    public Response listAllTasksByUser(@PathParam("id") Long userId) {
	String hql = "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.tasks WHERE u.id = :entityId ORDER BY u.id";
	TypedQuery<User> findByIdQuery = em.createQuery(hql, User.class);
	findByIdQuery.setParameter("entityId", userId);
	User user;
	try {
	    user = findByIdQuery.getSingleResult();
	} catch (NoResultException nre) {
	    user = null;
	}
	if (user == null) {
	    return Response.status(Status.NOT_FOUND)
		    	   .entity(new UserResponseEntity(Status.NOT_FOUND.getStatusCode(), "User " + userId + " not found."))
		    	   .type(MediaType.APPLICATION_JSON).build();
	}
	return Response.ok(user.getTasks()).build();
    }
    
}
