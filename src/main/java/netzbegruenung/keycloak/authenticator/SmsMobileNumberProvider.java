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
 */
package netzbegruenung.keycloak.authenticator;

import netzbegruenung.keycloak.authenticator.SmsAuthenticatorModel;

import org.jboss.logging.Logger;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialTypeMetadata;
import org.keycloak.credential.CredentialTypeMetadataContext;
import org.keycloak.credential.UserCredentialStore;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import java.util.Optional;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SmsMobileNumberProvider implements CredentialProvider<SmsAuthenticatorModel>, CredentialInputValidator {
    private static final Logger logger = Logger.getLogger(SmsMobileNumberProvider.class);

    protected KeycloakSession session;

    public SmsMobileNumberProvider(KeycloakSession session) {
        this.session = session;
    }

    private UserCredentialStore getCredentialStore() {
        return session.userCredentialManager();
    }


    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!(input instanceof UserCredentialModel)) {
            logger.debug("Expected instance of UserCredentialModel for CredentialInput");
            return false;
        }
        if (!input.getType().equals(getType())) {
            return false;
        }
        String challengeResponse = input.getChallengeResponse();
        if (challengeResponse == null) {
            return false;
        }
        CredentialModel credentialModel = getCredentialStore().getStoredCredentialById(realm, user, input.getCredentialId());
        SmsAuthenticatorModel sqcm = getCredentialFromModel(credentialModel);
        return sqcm.getSmsAuthenticatorData().getMobileNumber().equals(challengeResponse);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return getType().equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        if (!supportsCredentialType(credentialType)) return false;
        return getCredentialStore().getStoredCredentialsByTypeStream(realm, user, credentialType).count() > 0;
    }

    @Override
    public CredentialModel createCredential(RealmModel realm, UserModel user, SmsAuthenticatorModel credentialModel) {
        credentialModel.setCreatedDate(Time.currentTimeMillis());
        logger.warn(String.format("Create Credentials in SmsMobileNumberProvider with credential model: [%s]", credentialModel.getCredentialData()));
        return getCredentialStore().createCredential(realm, user, credentialModel);
    }

    public void updateCredential(RealmModel realm, UserModel user) {
        Optional<CredentialModel> model = getCredentialStore().getStoredCredentialsByTypeStream(realm, user, SmsAuthenticatorModel.TYPE).findFirst();
        if (model.isPresent()) {
            CredentialModel credentialModel = model.get();
            getCredentialStore().updateCredential(realm, user, credentialModel);
            logger.warn(String.format("Update Credentials in SmsMobileNumberProvider with credential model: [%s]", credentialModel.getCredentialData()));
        } else {
            logger.error("Error occurred during model update");
        }
    }

    @Override
    public boolean deleteCredential(RealmModel realm, UserModel user, String credentialId) {
        return getCredentialStore().removeStoredCredential(realm, user, credentialId);
    }

    @Override
    public SmsAuthenticatorModel getCredentialFromModel(CredentialModel model) {
        return SmsAuthenticatorModel.createFromModel(model);
    }

    @Override
    public CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext metadataContext) {
        return CredentialTypeMetadata.builder()
                .type(getType())
                .category(CredentialTypeMetadata.Category.TWO_FACTOR)
                .displayName(SmsMobileNumberProviderFactory.PROVIDER_ID)
                .helpText("secret-question-text")
                .createAction(SmsAuthenticatorFactory.PROVIDER_ID)
                .removeable(false)
                .build(session);
    }

    @Override
    public String getType() {
        return SmsAuthenticatorModel.TYPE;
    }
}
