package dasniko.keycloak.authenticator.gateway;

import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EcallSmsService implements SmsService {

	private static class EcallRequestContent {
		public String type = "Text";
		public String text;
	}

	private static class EcallRequestBody {
		public String channel = "sms";
		public String from;
		public String to;
		public EcallRequestContent content;
	}

	private final String senderNr;
	private final String username;
	private final String password;
	private final KeycloakSession session;

	EcallSmsService(Map<String, String> config, KeycloakSession session) {
		this.senderNr = config.get("senderNr");
		this.username = config.get("username");
		this.password = config.get("password");
		this.session = session;
	}

	@Override
	public void send(String phoneNumber, String message) {
		EcallRequestContent content = new EcallRequestContent();
		content.text = message;
		EcallRequestBody body = new EcallRequestBody();
		body.from = this.senderNr;
		body.to = phoneNumber;
		body.content = content;
		try {
			SimpleHttp.Response response = SimpleHttp
				.doPost("https://rest.ecall.ch/api/message", this.session)
				.json(body)
				.authBasic(this.username, this.password)
				.acceptJson()
				.asResponse();
		} catch (IOException ex) {
			log.error("Ecall API call failed", ex);
		}

	}

}
