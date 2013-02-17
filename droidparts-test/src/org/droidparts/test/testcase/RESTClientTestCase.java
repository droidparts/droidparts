/**
 * Copyright 2013 Alex Yanchenko
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.droidparts.test.testcase;

import org.droidparts.http.HTTPException;
import org.droidparts.http.HTTPResponse;
import org.droidparts.http.RESTClient;
import org.droidparts.http.RESTClient2;

import android.test.AndroidTestCase;

public class RESTClientTestCase extends AndroidTestCase {

	private static final String AUTH_URL = "http://www.httpwatch.com/httpgallery/authentication/authenticatedimage/default.aspx";
	private static final String AUTH_LOGIN = "httpwatch";

	public void testHttpBasicAuth() {
		RESTClient2 client = new RESTClient2(getContext());
		testUnauthenticated(client);
		testAuthenticatedWrongCredentials(client);
		testAuthenticated(client);
	}

	public void testHttpBasicAuthLegacy() {
		RESTClient2 client = new RESTClient2(getContext(),
				RESTClient.getUserAgent(null), true);
		testUnauthenticated(client);
		testAuthenticatedWrongCredentials(client);
		testAuthenticated(client);
	}

	private void testUnauthenticated(RESTClient client) {
		try {
			HTTPResponse resp = client.get(AUTH_URL);
			assertNull(resp);
		} catch (HTTPException e) {
			assertEquals(401, e.getResponseCode());
		}
	}

	private void testAuthenticatedWrongCredentials(RESTClient client) {
		client.authenticateBasic("wtf", AUTH_LOGIN);
		try {
			HTTPResponse resp = client.get(AUTH_URL);
			assertNull(resp);
		} catch (HTTPException e) {
			assertEquals(401, e.getResponseCode());
		}
	}

	private void testAuthenticated(RESTClient client) {
		client.authenticateBasic(AUTH_LOGIN, AUTH_LOGIN);
		try {
			HTTPResponse resp = client.get(AUTH_URL);
			assertNotNull(resp);
		} catch (HTTPException e) {
			assertNull(e);
		}
	}

}
