package main.java.service;

import java.util.UUID;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.auth.AuthContext;
import main.java.dto.request.LoginRequest;
import main.java.dto.request.LogoutRequest;
import main.java.dto.request.RegisterRequest;
import main.java.dto.response.LoginResponse;
import main.java.dto.response.RefreshTokenResponse;
import main.java.entity.User;
import main.java.exception.AuthException;
import main.java.exception.DuplicateUserException;
import main.java.exception.LogoutException;

@Component
public class AuthService {

	
	@Inject
	TokenService tokenService;

	@Inject
	LocalizationService localizator;
	
	@Inject
	private HashService hashService;
	
	
	@Inject
	private UserService userService;

	public boolean logout(User user) {
		if(!userService.userExists(user)) {
			return false;
		}
		AuthContext.deauthorize(user);
		return true;
	}
	
	private boolean login(User user) {
		if(!userService.userExistsWithPasswordEquals(user)) {
			return false;
		}
		AuthContext.authorize(user);
		return true;
	}

	public LoginResponse login(LoginRequest requestObj, String userLocale) {
		String refreshToken = UUID.randomUUID().toString();
		User user = User.builder().username(requestObj.getUsername()).password(hashService.hashStringMD5(requestObj.getPassword()))
				.refreshToken(refreshToken).build();
		boolean login = login(user);
		if (!login) {
			throw new AuthException(localizator.getPropertyByLocale(userLocale, "incorrectUsernameOrPassword"));
		}
		String jwt = tokenService.generateJwt(user);
		user.setToken(jwt);
		userService.updateToken(user, jwt);
		userService.updateRefreshToken(user, refreshToken);
		AuthContext.authorize(user);
		return LoginResponse.builder()
				.refreshToken(refreshToken)
				.token(jwt)
				.username(user.getUsername())
				.build();
	}

	public User register(RegisterRequest requestObj, String userLocale) {
		User user = User.builder().name(requestObj.getName()).username(requestObj.getUsername())
				.surname(requestObj.getSurname()).password(hashService.hashStringMD5(requestObj.getPassword())).build();
		if(userService.tryCreateUser(user))
			return user;
		throw new DuplicateUserException(localizator.getPropertyByLocale(userLocale, "userExists"));
	}

	public RefreshTokenResponse refreshToken(String token, String refreshToken, String userLocale) {
		User user = AuthContext.getUserByToken(token);
		if (user == null) {
			throw new AuthException(localizator.getPropertyByLocale(userLocale, "notAuthorized"));
		}
		String refreshTokenOfUser = user.getRefreshToken();
		if (refreshTokenOfUser == null || !refreshTokenOfUser.equals(refreshToken)) {
			throw new IllegalArgumentException(localizator.getPropertyByLocale(userLocale, "refreshTokenNull"));
		}
		String newToken = tokenService.generateJwt(user);
		user.setToken(newToken);
		userService.updateToken(user, newToken);
		return RefreshTokenResponse.builder().refreshToken(refreshTokenOfUser)
				.token(newToken).build();
	}

	public void logout(LogoutRequest request) {
		String token = request.getToken();
		User user = AuthContext.getUserByToken(token);
		if (user == null) {
			throw new LogoutException("Could not logout");
		}
		AuthContext.deauthorize(user);
		userService.deleteToken(token);
	}
	
}
