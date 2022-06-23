package netzbegruenung.keycloak.authenticator.gateway;

import java.util.Map;

/**
 * @author Netzbegruenung e.V.
 */
public interface SmsService {

	void send(String phoneNumber, String message);

}
