/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 * @author Netzbegruenung e.V.
 * @author verdigado eG
 */

package netzbegruenung.keycloak.authenticator.gateway;

import java.util.Map;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import org.jboss.logging.Logger;
import org.apache.commons.codec.binary.Base64;

public class ApiSmsService implements SmsService{

	private final String apiurl;
	private final Boolean urlencode;

	private final String apitoken;
	private final String apiuser;

	private final String from;
	private final String countrycode;

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

		countrycode = config.getOrDefault("countrycode", "");
		from = config.get("senderId");

		apitokenattribute = config.getOrDefault("apitokenattribute", "");
		messageattribute = config.get("messageattribute");
		receiverattribute = config.get("receiverattribute");
		senderattribute = config.get("senderattribute");
	}

	public void send(String phoneNumber, String message) {
		phoneNumber = clean_phone_number(phoneNumber, countrycode);
		Builder request_builder;
		if (urlencode) {
			request_builder = urlencoded_request(phoneNumber, message);
		} else {
			request_builder = json_request(phoneNumber, message);
		}

		HttpRequest request;
		if (apiuser != "") {
			request = request_builder.setHeader("Authorization", get_auth_header(apiuser, apitoken)).build();
		} else {
			request = request_builder.build();
		}

		var client = HttpClient.newHttpClient();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			LOG.warn(String.format("Sent SMS to %s; API response: %s", phoneNumber, response.toString()));
		} catch (Exception e){
			LOG.warn(String.format("Failed to send message to %s with request %s", apiurl, request.toString()));
			e.printStackTrace();
		}
	}

	public Builder json_request(String phoneNumber, String message) {
		String sendJson = "{"
			.concat(apitokenattribute != "" ? String.format("\"%s\":\"%s\",", apitokenattribute, apitoken): "")
			.concat(String.format("\"%s\":\"%s\",", messageattribute, message))
			.concat(String.format("\"%s\":\"%s\",", receiverattribute, phoneNumber))
			.concat(String.format("\"%s\":\"%s\"", senderattribute, from))
			.concat("}");

		 return HttpRequest.newBuilder()
			.uri(URI.create(apiurl))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(sendJson));
	}

	public Builder urlencoded_request(String phoneNumber, String message) {
		Map<String, String> formData = new HashMap<>();
		if (apitokenattribute != "") {
			formData.put(apitokenattribute, apitoken);
		}
		formData.put(messageattribute, message);
		formData.put(receiverattribute, phoneNumber);
		formData.put(senderattribute, from);
		String form_data = getFormDataAsString(formData);

		return HttpRequest.newBuilder(URI.create(apiurl))
				.POST(HttpRequest.BodyPublishers.ofString(form_data))
				.setHeader("Authorization", get_auth_header(apiuser, apitoken));
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

	private static String get_auth_header(String apiuser, String apitoken) {
		String authString = apiuser + ":" + apitoken;
		return String.format("Basic %s" + new String(Base64.encodeBase64(authString.getBytes())));
	}

	private static String clean_phone_number(String phone_number, String countrycode) {
		if (phone_number.startsWith("00")) {
			return phone_number.replaceFirst("00", countrycode);
		}
		if (phone_number.startsWith("0")) {
			return phone_number.replaceFirst("0", countrycode);
		}
		return phone_number;
	}
}
