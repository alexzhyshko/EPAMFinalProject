package main.java.service;

import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

import application.context.annotation.component.Component;

@Component
public class HashService {

	public String hashStringMD5(String stringToHash) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(stringToHash.getBytes());
			byte[] digest = md.digest();
			return DatatypeConverter.printHexBinary(digest).toUpperCase();
		} catch (Exception e) {
			throw new IllegalArgumentException("Couldn't hash password");
		}
	}

}
