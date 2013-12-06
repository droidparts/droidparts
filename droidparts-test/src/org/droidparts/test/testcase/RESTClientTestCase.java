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

import android.content.Context;
import org.droidparts.net.http.HTTPException;
import org.droidparts.net.http.HTTPResponse;
import org.droidparts.net.http.RESTClient;
import org.droidparts.net.http.RESTClient2;

import android.test.AndroidTestCase;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RESTClientTestCase extends AndroidTestCase {

	private static final String AUTH_URL = "http://www.httpwatch.com/httpgallery/authentication/authenticatedimage/default.aspx";
	private static final String AUTH_LOGIN = "httpwatch";
    private static final String POST_MULTIPART_URL = "http://posttestserver.com/post.php";
    private static final String POST_MULTIPART_FILE_NAME = "test";

	public void _testHttpBasicAuth() {
		RESTClient2 client = new RESTClient2(getContext());
		testUnauthenticated(client);
		testAuthenticatedWrongCredentials(client);
		testAuthenticated(client);
	}

	public void _testHttpBasicAuthLegacy() {
		RESTClient2 client = new RESTClient2(getContext(),
				RESTClient.getUserAgent(null), true);
		testUnauthenticated(client);
		testAuthenticatedWrongCredentials(client);
		testAuthenticated(client);
	}

    public void testPostMultipartFile() throws Exception {
        RESTClient client = new HttURLConnectionClient (getContext());
        HTTPResponse resp = client.postMultipartFile(POST_MULTIPART_URL, POST_MULTIPART_FILE_NAME, getTestFile());
        assertNotNull(resp);
    }

    public void testPostMultipartFileLegacy() throws Exception {
        RESTClient client = new RESTClient(getContext(), RESTClient.getUserAgent(null), true);
        HTTPResponse resp = client.postMultipartFile(POST_MULTIPART_URL, POST_MULTIPART_FILE_NAME, getTestFile());
        assertNotNull(resp);
    }

    private File getTestFile() throws IOException {
        File file = new File(getContext().getFilesDir(), "test.txt");
        writeFile(file, "Test POST multipart file");
        return file;
    }

    class HttURLConnectionClient extends RESTClient {

        public HttURLConnectionClient(Context ctx) {
            super(ctx);
        }

        @Override
        protected boolean useHttpURLConnection() {
            return true;
        }
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
