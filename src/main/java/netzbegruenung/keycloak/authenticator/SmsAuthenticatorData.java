package netzbegruenun.keycloak.authenticator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

public class SmsAuthenticatorData {

	private final String mobileNumber;

	//@JsonCreator
	public SmsAuthenticatorData (@JsonProperty("mobile_number") String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}
}
