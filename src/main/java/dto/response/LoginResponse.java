package main.java.dto.response;

public class LoginResponse {
	private String username;
	private String refreshToken;
	private String token;
	
	public static Builder builder() {
		return new LoginResponse().new Builder();
	}
	
	public class Builder{
		
		public Builder username(String username) {
			LoginResponse.this.username = username;
			return this;
		}
		
		public Builder refreshToken(String refreshToken) {
			LoginResponse.this.refreshToken = refreshToken;
			return this;
		}
		
		public Builder token(String token) {
			LoginResponse.this.token = token;
			return this;
		}
		
		public LoginResponse build() {
			return LoginResponse.this;
		}
		
	}
	
}
