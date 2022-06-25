package netzbegruenung.keycloak.authenticator;

import netzbegruenung.keycloak.authenticator.SmsAuthenticatorData;
import netzbegruenung.keycloak.authenticator.SmsAuthenticatorModel;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.util.JsonSerialization;
import org.jboss.logging.Logger;

import java.io.IOException;

public class SmsAuthenticatorModel extends CredentialModel {
	public static final String TYPE = "mobile-number";

	private final SmsAuthenticatorData mobileNumber;
	private static final Logger LOG = Logger.getLogger(SmsAuthenticatorSetMobileNumberAction.class);


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
			SmsAuthenticatorModel.setUserLabel("Mobile Number");
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
			LOG.warn(String.format("Filling credential model in SmsAuthenticationModel with mobile number: [%s], serialized: [%s]", mobileNumber.getMobileNumber(), JsonSerialization.writeValueAsString(mobileNumber)));
			setCredentialData(JsonSerialization.writeValueAsString(mobileNumber));
			setType(TYPE);
			setCreatedDate(Time.currentTimeMillis());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
