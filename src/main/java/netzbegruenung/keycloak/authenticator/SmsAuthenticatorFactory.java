package netzbegruenung.keycloak.authenticator;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.authentication.RequiredActionContext;

import java.util.List;

/**
 * @author Netzbegruenung e.V.
 */
public class SmsAuthenticatorFactory implements AuthenticatorFactory {

	public static final String PROVIDER_ID = "sms-authenticator";

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public Authenticator create(KeycloakSession session) {
		return new SmsAuthenticator();
	}

	private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
		AuthenticationExecutionModel.Requirement.REQUIRED,
		AuthenticationExecutionModel.Requirement.ALTERNATIVE
	};
	@Override
	public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
		return REQUIREMENT_CHOICES;
	}

	@Override
	public boolean isConfigurable() {
		return true;
	}

	@Override
	public boolean isUserSetupAllowed() {
		return true;
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return List.of(
			new ProviderConfigProperty("length", "Code length", "The number of digits of the generated code.", ProviderConfigProperty.STRING_TYPE, 6),
			new ProviderConfigProperty("ttl", "Time-to-live", "The time to live in seconds for the code to be valid.", ProviderConfigProperty.STRING_TYPE, "300"),
			new ProviderConfigProperty("senderId", "SenderId", "The sender ID is displayed as the message sender on the receiving device.", ProviderConfigProperty.STRING_TYPE, "Keycloak"),
			new ProviderConfigProperty("simulation", "Simulation mode", "In simulation mode, the SMS won't be sent, but printed to the server logs", ProviderConfigProperty.BOOLEAN_TYPE, true),
			new ProviderConfigProperty("smsapi", "SMS API URL", "The path to the API that receives an HTTP request.", ProviderConfigProperty.STRING_TYPE, "https://example.com/api/sms/send"),
			new ProviderConfigProperty("type", "Request Type", "'JSON': send data as JSON in HTTP POST. 'GET': send data as HTTP GET parameters", ProviderConfigProperty.STRING_TYPE, "GET"),
			new ProviderConfigProperty("apitokenattribute", "API Secret Token Attribute", "Name of attribute that contains your API token/secret.", ProviderConfigProperty.STRING_TYPE, ""),
			new ProviderConfigProperty("apitoken", "API Secret Token", "Your API secret/token.", ProviderConfigProperty.STRING_TYPE, "changeme"),
			new ProviderConfigProperty("apiuser", "Basic Auth Username", "If set, Basic Auth will be performed. Leave empty if not required.", ProviderConfigProperty.STRING_TYPE, ""),
			new ProviderConfigProperty("messageattribute", "Message Atrribute", "The attribute that contains the SMS message.", ProviderConfigProperty.STRING_TYPE, "text"),
			new ProviderConfigProperty("receiverattribute", "Receiver Phone Number Attribute", "The attribute that contains the reciever phone number.", ProviderConfigProperty.STRING_TYPE, "to"),
			new ProviderConfigProperty("senderattribute", "Sender Phone Number Attribute", "The attribute that contains the sender phone number. Leave empty if not required.", ProviderConfigProperty.STRING_TYPE, "from")
		);
	}

	@Override
	public String getHelpText() {
		return "Validates an OTP sent via SMS to the users mobile phone.";
	}

	@Override
	public String getDisplayType() {
		return "SMS Authentication (2FA)";
	}

	@Override
	public String getReferenceCategory() {
		return "otp";
	}

	@Override
	public void init(Config.Scope config) {
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {
	}

	@Override
	public void close() {
	}

}
