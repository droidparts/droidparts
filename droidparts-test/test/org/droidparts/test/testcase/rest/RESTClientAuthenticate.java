/**
 * Copyright 2017 Alex Yanchenko
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
package org.droidparts.test.testcase.rest;

import org.junit.Test;

import org.droidparts.net.http.HTTPResponse;
import org.droidparts.net.http.RESTClient;
import org.droidparts.net.http.UserAgent;
import org.droidparts.net.http.worker.HTTPWorker;
import org.droidparts.net.http.worker.HttpClientWorker;
import org.droidparts.test.testcase.activity.ActivityTestCase;

import static org.junit.Assert.assertEquals;

public class RESTClientAuthenticate extends ActivityTestCase {

	private static final String AUTH_URL = "http://www.httpwatch.com/httpgallery/authentication/authenticatedimage/default.aspx";
	private static final String AUTH_LOGIN = "httpwatch";

	@Test
	public void testHttpBasicAuth() throws Exception {
		testHttpBasicAuth(null);
	}

	@Test
	public void testHttpBasicAuthLegacy() throws Exception {
		HTTPWorker worker = new HttpClientWorker(UserAgent.getDefault());
		testHttpBasicAuth(worker);
	}

	//

	private void testHttpBasicAuth(HTTPWorker worker) throws Exception {
		RESTClient client;
		if (worker == null) {
			client = new RESTClient(getActivity());
		} else {
			client = new RESTClient(getActivity(), worker);
		}
		testUnauthenticated(client);
		testAuthenticatedWrongCredentials(client);
		testAuthenticated(client);
	}

	private void testUnauthenticated(RESTClient client) throws Exception {
		HTTPResponse resp = client.get(AUTH_URL);
		assertEquals(401, resp.code);
	}

	private void testAuthenticatedWrongCredentials(RESTClient client) throws Exception {
		client.authenticateBasic("wtf", AUTH_LOGIN);
		HTTPResponse resp = client.get(AUTH_URL);
		assertEquals(401, resp.code);
	}

	private void testAuthenticated(RESTClient client) throws Exception {
		client.authenticateBasic(AUTH_LOGIN, AUTH_LOGIN);
		HTTPResponse resp = client.get(AUTH_URL);
		assertEquals(200, resp.code);
	}
}
