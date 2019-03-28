package ai.atlaslabs.keycloak.kafka;

public class RegisterEvent {

	private String userId;
	private String email;

	public RegisterEvent(String userId, String email) {
		this.userId = userId;
		this.email = email;
	}

	public String getUserId() {
		return userId;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		return "RegisterEvent [userId=" + userId + ", email=" + email + "]";
	}

}
