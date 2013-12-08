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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.droidparts.net.http.HTTPException;
import org.droidparts.net.http.HTTPResponse;
import org.droidparts.net.http.RESTClient;
import org.droidparts.net.http.RESTClient2;
import org.droidparts.net.http.worker.HttpClientWorker;

import android.test.AndroidTestCase;

public class RESTClientTestCase extends AndroidTestCase {

	private static final String AUTH_URL = "http://www.httpwatch.com/httpgallery/authentication/authenticatedimage/default.aspx";
	private static final String AUTH_LOGIN = "httpwatch";
	// Simple server for testing POST requests. Supports multipart/form-data
	// file uploads.
	private static final String POST_MULTIPART_URL = "http://posttestserver.com/post.php?dump";
	private static final String POST_MULTIPART_FILE_NAME = "test";
	private static final String POST_MULTIPART_FILE_BODY = "Test POST multipart file";

	public void _testHttpBasicAuth() {
		RESTClient2 client = new RESTClient2(getContext());
		testUnauthenticated(client);
		testAuthenticatedWrongCredentials(client);
		testAuthenticated(client);
	}

	public void _testHttpBasicAuthLegacy() {
		RESTClient2 client = new RESTClient2(getContext(),
				new HttpClientWorker(RESTClient.getUserAgent(null)));
		testUnauthenticated(client);
		testAuthenticatedWrongCredentials(client);
		testAuthenticated(client);
	}

	public void testPostMultipartFile() throws Exception {
		RESTClient client = new RESTClient(getContext());
		HTTPResponse resp = client.postMultipartFile(POST_MULTIPART_URL,
				POST_MULTIPART_FILE_NAME, getTestFile());
		assertPostMultipartFileResponse(resp);
	}

	public void testPostMultipartFileLegacy() throws Exception {
		RESTClient client = new RESTClient(getContext(), new HttpClientWorker(
				RESTClient.getUserAgent(null)));
		HTTPResponse resp = client.postMultipartFile(POST_MULTIPART_URL,
				POST_MULTIPART_FILE_NAME, getTestFile());
		assertPostMultipartFileResponse(resp);
	}

	private void assertPostMultipartFileResponse(HTTPResponse response)
			throws HTTPException {
		assertNotNull(response);
		String body = response.body;

		// Get uploaded file URL
		Pattern pattern = Pattern.compile("Uploaded File: (.+)");
		Matcher matcher = pattern.matcher(body);
		assertTrue(matcher.find());

		String fileUrl = matcher.group(1);
		RESTClient2 client = new RESTClient2(getContext());
		assertEquals(client.get(fileUrl).body, POST_MULTIPART_FILE_BODY);
	}

	private File getTestFile() throws IOException {
		File file = new File(getContext().getFilesDir(), "test.txt");
		writeFile(file, POST_MULTIPART_FILE_BODY);
		return file;
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

	private void writeFile(File file, String string) throws IOException {
		file.getParentFile().mkdirs();
		DataOutputStream dout = new DataOutputStream(new FileOutputStream(file));
		dout.write(string.getBytes());
		dout.close();
	}
}
