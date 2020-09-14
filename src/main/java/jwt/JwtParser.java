package main.java.jwt;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.DefaultClaims;
import main.java.service.TokenService;

@Component
public class JwtParser {

	@Inject
	private TokenService tokenService;

	public Claims parseClaimsFromJwt(String jwt) {
		Claims claims = new DefaultClaims();
		try {
			claims = Jwts.parser().setSigningKey(tokenService.getPrivateKey()).parseClaimsJws(jwt).getBody();
		} catch (SignatureException e) {
			e.printStackTrace();
			throw new NullPointerException("Claims parse failed");
		}
		return claims;

	}

}
