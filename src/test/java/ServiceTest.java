package test.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

import application.context.ApplicationContext;
import main.java.entity.User;
import main.java.repository.impl.UserRepositoryImpl;
import main.java.service.UserService;
import test.java.mock.UserRepositoryMock;

public class ServiceTest {

	UserService userService;

	@Test
	public void testCreateUser() {
		userService = new UserService();
		userService.userRepository = new UserRepositoryMock();
		User user = User.builder().id(UUID.randomUUID()).name("mock").username("mockmock")
				.surname("mock").password("pwd").build();
		assertTrue(userService.tryCreateUser(user));
		User retreivedUser = userService.getUserByUsername("mockmock").get();
		assertEquals("mock", retreivedUser.getName());
		UUID id = retreivedUser.getId();
		User retreivedUser2 = userService.getUserById(id);
		assertEquals("mock", retreivedUser2.getSurname());
		userService.userRepository = (UserRepositoryImpl)ApplicationContext.getInstance(UserRepositoryImpl.class);
	}
	
	
	
}
