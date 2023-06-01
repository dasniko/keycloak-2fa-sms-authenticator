package dasniko.keycloak.authenticator.gateway;

import dasniko.keycloak.authenticator.SmsConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
@Slf4j
public class SmsServiceFactory {

	public static SmsService get(Map<String, String> config) {
		if (Boolean.parseBoolean(config.getOrDefault(SmsConstants.SIMULATION_MODE, "false"))) {
			return (phoneNumber, message) ->
				log.warn(String.format("***** SIMULATION MODE ***** Would send SMS to %s with text: %s", phoneNumber, message));
		} else {
			return new AwsSmsService(config);
		}
	}

}
