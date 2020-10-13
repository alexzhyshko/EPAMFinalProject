package main.java.service;

import java.util.Optional;
import java.util.UUID;

import application.context.annotation.component.Component;
import application.context.annotation.inject.Inject;
import main.java.auth.AuthContext;
import main.java.dto.request.LoginRequest;
import main.java.dto.request.LogoutRequest;
import main.java.dto.request.RegisterRequest;
import main.java.dto.response.LoginResponse;
import main.java.dto.response.RefreshTokenResponse;
import main.java.entity.User;
import main.java.exception.AuthException;
import main.java.exception.DuplicateLoginException;
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
		if (!userService.userExists(user)) {
			return false;
		}
		try {
			LoginRegister.removeFromRegisterIfLoggedIn(user.getUsername())
					.orElseThrow(() -> new LogoutException("User is not logged in"));
		} catch (Exception e) {

		}
		AuthContext.deauthorize(user);
		return true;
	}

	private boolean login(User user) {
		if (!userService.userExistsWithPasswordEquals(user)) {
			return false;
		}
		LoginRegister.addToRegisterIfNotLoggedIn(user.getUsername())
				.orElseThrow(() -> new DuplicateLoginException("User is already logged in"));
		AuthContext.authorize(user);
		return true;
	}

	public LoginResponse login(LoginRequest requestObj, String userLocale) {
		String refreshToken = UUID.randomUUID().toString();
		User user = buildUserFromLoginRequest(requestObj, refreshToken);
		boolean login = login(user);
		if (!login) {
			throw new AuthException(localizator.getPropertyByLocale(userLocale, "incorrectUsernameOrPassword"));
		}
		String jwt = tokenService.generateJwt(user);
		user.setToken(jwt);
		userService.updateToken(user, jwt);
		userService.updateRefreshToken(user, refreshToken);
		AuthContext.authorize(user);
		return buildLoginResponse(jwt, refreshToken, user.getUsername());
	}

	private LoginResponse buildLoginResponse(String jwt, String refreshToken, String username) {
		return LoginResponse.builder().refreshToken(refreshToken).token(jwt).username(username).build();
	}

	private User buildUserFromLoginRequest(LoginRequest requestObj, String refreshToken) {
		return User.builder().username(requestObj.getUsername())
				.password(hashService.hashStringMD5(requestObj.getPassword())).refreshToken(refreshToken).build();
	}

	public User register(RegisterRequest requestObj, String userLocale) {
		User user = User.builder().name(requestObj.getName()).username(requestObj.getUsername())
				.surname(requestObj.getSurname()).password(hashService.hashStringMD5(requestObj.getPassword())).build();
		if (userService.tryCreateUser(user))
			return user;
		throw new DuplicateUserException(localizator.getPropertyByLocale(userLocale, "userExists"));
	}

	public RefreshTokenResponse refreshToken(String token, String userLocale) {
		User user = AuthContext.getUserByToken(token)
				.orElseThrow(() -> new AuthException(localizator.getPropertyByLocale(userLocale, "notAuthorized")));
		String refreshTokenOfUser = getRefreshTokenOfUser(user).orElseThrow(
				() -> new IllegalArgumentException(localizator.getPropertyByLocale(userLocale, "refreshTokenNull")));
		String newToken = tokenService.generateJwt(user);
		LoginRegister.addToRegisterIfNotLoggedIn(user.getUsername())
				.orElseThrow(() -> new DuplicateLoginException("User is already logged in"));
		user.setToken(newToken);
		userService.updateToken(user, newToken);
		return buildRefreshTokenResponse(newToken, refreshTokenOfUser);
	}

	private RefreshTokenResponse buildRefreshTokenResponse(String newToken, String refreshTokenOfUser) {
		return RefreshTokenResponse.builder().refreshToken(refreshTokenOfUser).token(newToken).build();
	}

	private Optional<String> getRefreshTokenOfUser(User user) {
		return Optional.ofNullable(user.getRefreshToken());
	}

	public void logout(LogoutRequest request) {
		String token = request.getToken();
		User user = AuthContext.getUserByToken(token).orElseThrow(() -> new LogoutException("Could not logout"));
		userService.deleteToken(token);
		logout(user);
	}

}
