package main.java.jwt;

import java.util.logging.Logger;

import application.context.annotation.component.Component;
import application.context.annotation.inject.Inject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.DefaultClaims;
import main.java.service.TokenService;

@Component
public class JwtParser {

	static Logger logger = Logger.getLogger("main");
	
	@Inject
	private TokenService tokenService;

	public Claims parseClaimsFromJwt(String jwt) {
		Claims claims = new DefaultClaims();
		try {
			claims = Jwts.parser().setSigningKey(tokenService.getPrivateKey()).parseClaimsJws(jwt).getBody();
		} catch (SignatureException e) {
			logger.severe(e.getMessage());
			throw new NullPointerException("Claims parse failed");
		}
		return claims;

	}

}
