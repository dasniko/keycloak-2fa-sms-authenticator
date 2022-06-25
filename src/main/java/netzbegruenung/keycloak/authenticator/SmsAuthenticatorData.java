package netzbegruenung.keycloak.authenticator;

import com.fasterxml.jackson.annotation.JsonProperty;

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
