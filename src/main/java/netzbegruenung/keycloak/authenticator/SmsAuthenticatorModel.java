package netzbegruenung.keycloak.authenticator;

import netzbegruenung.keycloak.authenticator.SmsAuthenticatorData;
import netzbegruenung.keycloak.authenticator.SmsAuthenticatorModel;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;

public class SmsAuthenticatorModel extends CredentialModel {
	public static final String TYPE = "MOBILE_NUMBER";

	private final SmsAuthenticatorData mobileNumber;


	private SmsAuthenticatorModel(SmsAuthenticatorData mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	private SmsAuthenticatorModel(String mobileNumberString) {
		mobileNumber = new SmsAuthenticatorData(mobileNumberString);
	}

	public static SmsAuthenticatorModel createFromModel(CredentialModel credentialModel){
		try {
			SmsAuthenticatorData credentialData = JsonSerialization.readValue(credentialModel.getCredentialData(), SmsAuthenticatorData.class);

			SmsAuthenticatorModel SmsAuthenticatorModel = new SmsAuthenticatorModel(credentialData);
			SmsAuthenticatorModel.setUserLabel(credentialModel.getUserLabel());
			SmsAuthenticatorModel.setCreatedDate(credentialModel.getCreatedDate());
			SmsAuthenticatorModel.setType(TYPE);
			SmsAuthenticatorModel.setId(credentialModel.getId());
			SmsAuthenticatorModel.setCredentialData(credentialModel.getCredentialData());
			return SmsAuthenticatorModel;
		} catch (IOException e){
			throw new RuntimeException(e);
		}
	}


	public static SmsAuthenticatorModel createSmsAuthenticator(String mobileNumber) {
		SmsAuthenticatorModel credentialModel = new SmsAuthenticatorModel(mobileNumber);
		credentialModel.fillCredentialModelFields();
		return credentialModel;
	}

	public SmsAuthenticatorData getSmsAuthenticatorData() {
		return mobileNumber;
	}

	private void fillCredentialModelFields(){
		try {
			setCredentialData(JsonSerialization.writeValueAsString(mobileNumber));
			setType(TYPE);
			setCreatedDate(Time.currentTimeMillis());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
