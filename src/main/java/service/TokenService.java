package main.java.service;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;

import application.context.annotation.component.Component;
import application.context.annotation.inject.Inject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import main.java.entity.User;
import main.java.jwt.JwtParser;
import main.java.jwt.JwtProvider;

@Component
public class TokenService {

	private KeyStore keyStore;
	
	@Inject
	JwtParser jwtParser;
	
	@Inject
	JwtProvider jwtProvider;
	
	public TokenService() {
		try {
			keyStore = KeyStore.getInstance("JKS");
			keyStore.load(TokenService.class.getClassLoader().getResource("privatekey.jks").openStream(), "secret".toCharArray());
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public String generateJwt(User user) {
		return this.jwtProvider.generateJwt(user);
	}
	
	public boolean validateToken(String jwt) {
		Jwts.parser().setSigningKey(getPrivateKey()).parseClaimsJws(jwt);
		return true;
	}
	
	public User getUserFromJwt(String jwt) {
		Claims claims = jwtParser.parseClaimsFromJwt(jwt);
		return User.builder()
				.username((String)claims.get("username"))
				.password((String)claims.get("password"))
				.refreshToken((String)claims.get("refreshToken"))
				.build();
	}
	
	public String getUsernameFromJwt(String jwt) {
		Claims claims = jwtParser.parseClaimsFromJwt(jwt);
		return (String)claims.get("username");
	}
	
	public PrivateKey getPrivateKey() {
		try {
			return (PrivateKey)keyStore.getKey("tutorialspedia", "secret".toCharArray());
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
