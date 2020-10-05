package main.java.dto.response;

public class RefreshTokenResponse {
	private String refreshToken;
	private String token;
	
	public static Builder builder() {
		return new RefreshTokenResponse().new Builder();
	}
	
	public class Builder{
		
		public Builder refreshToken(String refreshToken) {
			RefreshTokenResponse.this.refreshToken = refreshToken;
			return this;
		}
		
		public Builder token(String token) {
			RefreshTokenResponse.this.token = token;
			return this;
		}
		
		public RefreshTokenResponse build() {
			return RefreshTokenResponse.this;
		}
		
	}
	
}
