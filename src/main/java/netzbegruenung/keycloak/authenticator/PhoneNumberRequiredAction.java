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
import org.keycloak.models.UserCredentialModel;
import org.keycloak.credential.CredentialProvider;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;

public class PhoneNumberRequiredAction implements RequiredActionProvider, CredentialRegistrator {

	public static String PROVIDER_ID = "mobile_number_config";
	private static final Logger LOG = Logger.getLogger(PhoneNumberRequiredAction.class);

	@Override
    public InitiatedActionSupport initiatedActionSupport() {
        return InitiatedActionSupport.SUPPORTED;
    }

	@Override
	public void evaluateTriggers(RequiredActionContext requiredActionContext) {}

	@Override
	public void requiredActionChallenge(RequiredActionContext context) {
		LOG.info("Create RequiredActionProvider challege");
		Response challenge = context.form().createForm("mobile_number_form.ftl");
		context.challenge(challenge);
	}

	@Override
	public void processAction(RequiredActionContext context) {
		String mobileNumber = (context.getHttpRequest().getDecodedFormParameters().getFirst("mobile_number")).replaceAll("[^0-9+]", "");
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
		LOG.info(String.format("Process Action completed, mobile number extracted from form: [%s]", mobileNumber));
		context.getAuthenticationSession().addRequiredAction("phone_validation_config");
		context.success();
	}

	@Override
	public void close() {}

}
