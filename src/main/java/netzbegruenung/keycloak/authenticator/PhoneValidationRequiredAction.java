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
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 * @author Netzbegruenung e.V.
 * @author verdigado eG
 */

package netzbegruenung.keycloak.authenticator;

import netzbegruenung.keycloak.authenticator.gateway.SmsServiceFactory;

import org.jboss.logging.Logger;
import org.keycloak.authentication.CredentialRegistrator;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.theme.Theme;

import java.util.Locale;
import javax.ws.rs.core.Response;

public class PhoneValidationRequiredAction implements RequiredActionProvider, CredentialRegistrator {
	private static final Logger LOG = Logger.getLogger(PhoneValidationRequiredAction.class);
	public static final String PROVIDER_ID = "phone_validation_config";

	@Override
	public void evaluateTriggers(RequiredActionContext context) {
	}

	@Override
	public void requiredActionChallenge(RequiredActionContext context) {
		context.getUser().addRequiredAction(PhoneNumberRequiredAction.PROVIDER_ID);
		try {
			UserModel user = context.getUser();

			AuthenticationSessionModel authSession = context.getAuthenticationSession();
			// TODO: get the alias from somewhere else or move config into realm or application scope
			AuthenticatorConfigModel config = context.getRealm().getAuthenticatorConfigByAlias("sms-2fa");

			String mobileNumber = authSession.getAuthNote("mobile_number");
			LOG.info(String.format("%s", mobileNumber));

			int length = Integer.parseInt(config.getConfig().get("length"));
			int ttl = Integer.parseInt(config.getConfig().get("ttl"));

			String code = SecretGenerator.getInstance().randomString(length, SecretGenerator.DIGITS);
			authSession.setAuthNote("code", code);
			authSession.setAuthNote("ttl", Long.toString(System.currentTimeMillis() + (ttl * 1000L)));

			Theme theme = context.getSession().theme().getTheme(Theme.Type.LOGIN);
			Locale locale = context.getSession().getContext().resolveLocale(user);
			String smsAuthText = theme.getMessages(locale).getProperty("smsAuthText");
			String smsText = String.format(smsAuthText, code, Math.floorDiv(ttl, 60));

			SmsServiceFactory.get(config.getConfig()).send(mobileNumber, smsText);

			Response challenge = context.form()
				.setAttribute("realm", context.getRealm())
				.createForm("login-sms.ftl");
			context.challenge(challenge);
		} catch (Exception e) {
			e.printStackTrace();
			context.failure();
		}

	}

	@Override
	public void processAction(RequiredActionContext context) {
		String enteredCode = context.getHttpRequest().getDecodedFormParameters().getFirst("code");

		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		String mobileNumber = authSession.getAuthNote("mobile_number");
		String code = authSession.getAuthNote("code");
		String ttl = authSession.getAuthNote("ttl");

		if (code == null || ttl == null || enteredCode == null) {
			LOG.warn("Phone number is not set");
			handleInvalidSmsCode(context);
			return;
		}

		boolean isValid = enteredCode.equals(code);
		if (isValid && Long.parseLong(ttl) > System.currentTimeMillis()) {
			// valid
			SmsMobileNumberProvider smnp = (SmsMobileNumberProvider) context.getSession().getProvider(CredentialProvider.class, "mobile-number");
			if (!smnp.isConfiguredFor(context.getRealm(), context.getUser(), SmsAuthenticatorModel.TYPE)) {
				smnp.createCredential(context.getRealm(), context.getUser(), SmsAuthenticatorModel.createSmsAuthenticator(mobileNumber));
			} else {
				smnp.updateCredential(
					context.getRealm(),
					context.getUser(),
					new UserCredentialModel("random_id", "mobile-number", mobileNumber)
				);
			}
			context.getUser().removeRequiredAction(PhoneNumberRequiredAction.PROVIDER_ID);
			context.success();
		} else {
			// invalid or expired
			handleInvalidSmsCode(context);
		}
	}

	private void handleInvalidSmsCode(RequiredActionContext context) {
		Response challenge = context
			.form()
			.setAttribute("realm", context.getRealm())
			.setError("smsAuthCodeInvalid")
			.createForm("login-sms.ftl");
		context.challenge(challenge);
	}

	@Override
	public void close() {
	}
}
