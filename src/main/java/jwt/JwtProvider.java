package main.java.jwt;

import java.time.LocalDateTime;
import java.util.Date;

import application.context.annotation.component.Component;
import application.context.annotation.inject.Inject;
import application.context.reader.PropertyReader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import main.java.entity.User;
import main.java.service.TokenService;

@Component
public class JwtProvider {
	
	@Inject
	private TokenService tokenService;
	
	private static PropertyReader propertyReader = new PropertyReader();
	
	private final int jwtLifetime;
	
	public JwtProvider() {
		this.jwtLifetime = Integer.parseInt(propertyReader.getProperty("jwt.lifetime"));
	}
	
	public String generateJwt(User user) {
		String username = user.getUsername();
		//String password = user.getPassword();
		String refreshToken = user.getRefreshToken();
		return Jwts.builder()
				  .claim("username", username)
				  //.claim("password", password)
				  .claim("refreshToken", refreshToken)
				  .setIssuedAt(toDate(LocalDateTime.now()))
				  .setExpiration(toDate(LocalDateTime.now().plusMinutes(jwtLifetime)))
				  .signWith(SignatureAlgorithm.RS256, tokenService.getPrivateKey())
				  .compact();
	}
	
	private Date toDate(LocalDateTime dateTime) {
		return java.sql.Timestamp.valueOf(dateTime);
	}
	
}
