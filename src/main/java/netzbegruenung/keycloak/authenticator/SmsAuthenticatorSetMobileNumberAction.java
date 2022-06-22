package netzbegruenung.keycloak.authenticator;

import netzbegruenung.keycloak.authenticator.SmsAuthenticator;
import netzbegruenung.keycloak.authenticator.SmsAuthenticatorModel;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.credential.CredentialProvider;
import javax.ws.rs.core.Response;

public class SmsAuthenticatorSetMobileNumberAction implements RequiredActionProvider {

	public static String PROVIDER_ID = "SET_MOBILE_NUMBER";

	@Override
	public void evaluateTriggers(RequiredActionContext requiredActionContext) {}

	@Override
	public void requiredActionChallenge(RequiredActionContext context) {
		Response challenge = context.form().createForm("mobile_number_form.ftl");
		context.challenge(challenge);
	}

	@Override
	public void processAction(RequiredActionContext context) {
		// Save input in mobile_number attribute
		/*
		String answer = (context.getHttpRequest().getDecodedFormParameters().getFirst("mobile_number"));
		UserCredentialModel model = new UserCredentialModel();
		model.setValue(answer);
		model.setType(SmsAuthenticator.CREDENTIAL_TYPE);
		context.getUser().updateCredentialDirectly(model);
		context.success();
		*/
		String mobileNumber = (context.getHttpRequest().getDecodedFormParameters().getFirst("mobile_number"));
	        SmsMobileNumberProvider smnp = (SmsMobileNumberProvider) context.getSession().getProvider(CredentialProvider.class, "mobile_number");
	        smnp.createCredential(context.getRealm(), context.getUser(), SmsAuthenticatorModel.createSmsAuthenticator(mobileNumber));
	        context.success();
	}

	public void close() {}
}
