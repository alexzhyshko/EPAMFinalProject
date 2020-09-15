package main.java.dto;

import java.util.UUID;

public class User {

	private UUID id;
	private String username;
	private String name;
	private String surname;
	private String refreshToken;
	private String token;
	private float rating;
	private Role role;
	
	private String password;
	
	public static Builder builder() {
		return new User().new Builder();
	}
	
	public class Builder{
		
		public Builder id(UUID id) {
			User.this.id = id;
			return this;
		}
		
		public Builder username(String text) {
			User.this.username = text;
			return this;
		}
		
		public Builder name(String text) {
			User.this.name = text;
			return this;
		}
		
		public Builder surname(String text) {
			User.this.surname = text;
			return this;
		}
		
		public Builder token(String text) {
			User.this.token = text;
			return this;
		}
		
		public Builder refreshToken(String text) {
			User.this.refreshToken = text;
			return this;
		}
		
		public Builder rating(float rating) {
			User.this.rating = rating;
			return this;
		}
		
		public Builder password(String password) {
			User.this.password = password;
			return this;
		}
		
		public Builder role(Role role) {
			User.this.role = role;
			return this;
		}
		
		public User build() {
			return User.this;
		}
		
		
	}

	public String getUsername() {
		return username;
	}

	public String getName() {
		return name;
	}

	public String getSurname() {
		return surname;
	}

	public String getPassword() {
		return password;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public String getToken() {
		return token;
	}

	public Role getRole() {
		return role;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	public void setRole(Role role) {
		this.role = role;
	}
	
}
