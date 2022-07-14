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
 * @author <a href="mailto:alistair.doswald@elca.ch">Alistair Doswald</a>
 * @author Netzbegruenung e.V.
 * @author verdigado eG
 */

package netzbegruenung.keycloak.authenticator;

import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;

public class SmsAuthenticatorModel extends CredentialModel {
	public static final String TYPE = "mobile-number";

	private final SmsAuthenticatorData mobileNumber;


	private SmsAuthenticatorModel(SmsAuthenticatorData mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	private SmsAuthenticatorModel(String mobileNumberString) {
		mobileNumber = new SmsAuthenticatorData(mobileNumberString);
	}

	public static SmsAuthenticatorModel createFromModel(CredentialModel credentialModel){
		try {
			SmsAuthenticatorData credentialData = JsonSerialization.readValue(credentialModel.getCredentialData(), SmsAuthenticatorData.class);

			SmsAuthenticatorModel smsAuthenticatorModel = new SmsAuthenticatorModel(credentialData);
			smsAuthenticatorModel.setUserLabel(
					"Mobile Number: ***" + credentialData.getMobileNumber().substring(
							Math.max(credentialData.getMobileNumber().length() - 3, 0)
					)
			);
			smsAuthenticatorModel.setCreatedDate(credentialModel.getCreatedDate());
			smsAuthenticatorModel.setType(TYPE);
			smsAuthenticatorModel.setId(credentialModel.getId());
			smsAuthenticatorModel.setCredentialData(credentialModel.getCredentialData());
			return smsAuthenticatorModel;
		} catch (IOException e){
			throw new RuntimeException(e);
		}
	}


	public static SmsAuthenticatorModel createSmsAuthenticator(String mobileNumber) {
		SmsAuthenticatorModel credentialModel = new SmsAuthenticatorModel(mobileNumber);
		credentialModel.fillCredentialModelFields();
		return credentialModel;
	}

	public SmsAuthenticatorData getSmsAuthenticatorData() {
		return mobileNumber;
	}

	private void fillCredentialModelFields(){
		try {
			setCredentialData(JsonSerialization.writeValueAsString(mobileNumber));
			setType(TYPE);
			setCreatedDate(Time.currentTimeMillis());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
