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
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author Netzbegruenung e.V.
 * @author verdigado eG
 */

package netzbegruenung.keycloak.authenticator;

import org.keycloak.authentication.CredentialRegistrator;
import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.credential.WebAuthnCredentialModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;

public class PhoneNumberRequiredAction implements RequiredActionProvider, CredentialRegistrator {

	public static String PROVIDER_ID = "mobile_number_config";
	private static final Logger logger = Logger.getLogger(PhoneNumberRequiredAction.class);

	@Override
	public InitiatedActionSupport initiatedActionSupport() {
		return InitiatedActionSupport.SUPPORTED;
	}

	@Override
	public void evaluateTriggers(RequiredActionContext context) {
		// TODO: get the alias from somewhere else or move config into realm or application scope
		AuthenticatorConfigModel config = context.getRealm().getAuthenticatorConfigByAlias("sms-2fa");
		Boolean forceSecondFactorEnabled = Boolean.parseBoolean(config.getConfig().get("forceSecondFactor"));
		if (forceSecondFactorEnabled) {
			Stream<CredentialModel> credentials = context
				.getSession()
				.userCredentialManager()
				.getStoredCredentialsStream(context.getRealm(), context.getUser());
			List<String> secondFactors = Arrays.asList(
				SmsAuthenticatorModel.TYPE,
				WebAuthnCredentialModel.TYPE_TWOFACTOR,
				OTPCredentialModel.TYPE
			);
			List<CredentialModel> credentialList = credentials
				.filter(c -> secondFactors.contains(c.getType()))
				.collect(Collectors.toList());
			Set<String> requiredActions = context.getAuthenticationSession().getRequiredActions();
			if (
				credentialList.isEmpty()
				&& !requiredActions.contains(PhoneNumberRequiredAction.PROVIDER_ID)
				&& !requiredActions.contains(PhoneValidationRequiredAction.PROVIDER_ID)
			) {
				logger.info(
					String.format(
						"No 2FA method configured for user: %s, setting required action for SMS authenticator",
						context.getUser().getUsername()
					)
				);
				context.getUser().addRequiredAction(PhoneNumberRequiredAction.PROVIDER_ID);
			}
		}
	}

	@Override
	public void requiredActionChallenge(RequiredActionContext context) {
		Response challenge = context.form().createForm("mobile_number_form.ftl");
		context.challenge(challenge);
	}

	@Override
	public void processAction(RequiredActionContext context) {
		String mobileNumber = (context.getHttpRequest().getDecodedFormParameters().getFirst("mobile_number")).replaceAll("[^0-9+]", "");
		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		authSession.setAuthNote("mobile_number", mobileNumber);
		logger.debug(String.format("Add required action for phone validation: [%s]", mobileNumber));
		context.getAuthenticationSession().addRequiredAction(PhoneValidationRequiredAction.PROVIDER_ID);
		context.success();
	}

	@Override
	public void close() {}

}
