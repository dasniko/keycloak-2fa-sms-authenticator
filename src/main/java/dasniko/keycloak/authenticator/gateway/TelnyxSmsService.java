package dasniko.keycloak.authenticator.gateway;
import com.telnyx.sdk.ApiClient;
import com.telnyx.sdk.ApiException;
import com.telnyx.sdk.Configuration;
import com.telnyx.sdk.auth.*;
import com.telnyx.sdk.model.*;
import com.telnyx.sdk.api.MessagesApi;
import java.util.Map;

public class TelnyxSmsService implements SmsService{

	private static final String YOUR_TELNYX_API_KEY = "MyGo9OH09fU8YhtP3b93XWYt";

	@Override
	public void send(String telnyxNumber, String phoneNumber, String message) {
		ApiClient defaultClient = Configuration.getDefaultApiClient();
		defaultClient.setBasePath("https://api.telnyx.com/v2");

		// Configure HTTP bearer authorization: bearerAuth
		HttpBearerAuth bearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("bearerAuth");
		bearerAuth.setBearerToken(YOUR_TELNYX_API_KEY);

		MessagesApi apiInstance = new MessagesApi(defaultClient);
		// CreateMessageRequest | Message payload
		CreateMessageRequest createMessageRequest = new CreateMessageRequest()
			.from(telnyxNumber)
			.to(phoneNumber)
			.text(message);
		try {
			MessageResponse result = apiInstance.createMessage(createMessageRequest);
			System.out.println(result);
		} catch (ApiException e) {
			System.err.println("Exception when calling MessagesApi#createLongCodeMessage");
			System.err.println("Status code: " + e.getCode());
			System.err.println("Reason: " + e.getResponseBody());
			System.err.println("Response headers: " + e.getResponseHeaders());
			e.printStackTrace();
		}
	}
}
