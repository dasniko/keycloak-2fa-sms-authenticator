package netzbegruenung.keycloak.authenticator.gateway;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.*;
import javax.json.*;
import org.jboss.logging.Logger;

/**
 * @author Netzbegrünung e.V.
 */
public class ApiSmsService implements SmsService{

	private final String apiurl;
	private final Boolean urlencode;

	private final String apitoken;
	private final String apiuser;

	private final String from;

	private final String apitokenattribute;
	private final String messageattribute;
	private final String receiverattribute;
	private final String senderattribute;

	private static final Logger LOG = Logger.getLogger(SmsServiceFactory.class);

	ApiSmsService(Map<String, String> config) {
		apiurl = config.get("apiurl");
		urlencode = Boolean.parseBoolean(config.getOrDefault("urlencode", "false"));

		apitoken = config.getOrDefault("apitoken", "");
		apiuser = config.getOrDefault("apiuser", "");

		from = config.get("senderId");

		apitokenattribute = config.getOrDefault("apitokenattribute", "");
		messageattribute = config.get("messageattribute");
		receiverattribute = config.get("receiverattribute");
		senderattribute = config.get("senderattribute");
	}

	public void send(String phoneNumber, String message) {
		if (urlencode) {
			send_urlencoded(phoneNumber, message);
			LOG.warn(String.format("Trying to send %s to %s via URL encoded request", message, phoneNumber));
		} else {
			send_json(phoneNumber, message);
			LOG.warn(String.format("Trying to send %s to %s via JSON body", message, phoneNumber));
		}
	}

	public void send_json(String phoneNumber, String message) {
        String sendJson = "{"
            .concat(apitokenattribute != "" ? String.format("\"%s\":\"%s\",", apitokenattribute, apitoken): "")
            .concat(String.format("\"%s\":\"%s\",", messageattribute, message))
            .concat(String.format("\"%s\":\"%s\",", receiverattribute, phoneNumber))
            .concat(String.format("\"%s\":\"%s\"", senderattribute, from))
            .concat("}");

        var request = HttpRequest.newBuilder()
            .uri(URI.create(apiurl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendJson))
            .build();

        var client = HttpClient.newHttpClient();

        HttpResponse<String> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			LOG.warn(String.format("Sent message to %s with body %s; Response: %s ", apiurl, sendJson, response));
		} catch (IOException e) {
			LOG.warn(String.format("Failed to send message to %s with body %s", apiurl, sendJson));
			e.printStackTrace();
		} catch (InterruptedException e) {
			LOG.warn(String.format("Failed to send message to %s with body %s", apiurl, sendJson));
			e.printStackTrace();
		}
	}

	public void send_urlencoded(String phoneNumber, String message) {
		Map<String, String> formData = new HashMap<>();
		if (apitokenattribute != "") {
			formData.put(apitokenattribute, apitoken);
		}
		formData.put(messageattribute, message);
		formData.put(receiverattribute, phoneNumber);
		formData.put(senderattribute, from);

	    var client = HttpClient.newHttpClient();
	    var form_data = getFormDataAsString(formData);
	    var request = HttpRequest.newBuilder(URI.create(apiurl))
	            .POST(HttpRequest.BodyPublishers.ofString(form_data))
	            .build();
	    try {
			client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			LOG.warn(String.format("Failed to send message to %s with params %s", apiurl, form_data));
			e.printStackTrace();
		} catch (InterruptedException e) {
			LOG.warn(String.format("Failed to send message to %s with params %s", apiurl, form_data));
			e.printStackTrace();
		}
	}

	private static String getFormDataAsString(Map<String, String> formData) {
	    StringBuilder formBodyBuilder = new StringBuilder();
	    for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
	        if (formBodyBuilder.length() > 0) {
	            formBodyBuilder.append("&");
	        }
	        formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
	        formBodyBuilder.append("=");
	        formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
	    }
	    return formBodyBuilder.toString();
	}
}
