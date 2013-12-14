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
package org.droidparts.test.testcase.rest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.droidparts.net.http.HTTPException;
import org.droidparts.net.http.HTTPResponse;
import org.droidparts.net.http.RESTClient;
import org.droidparts.net.http.RESTClient2;
import org.droidparts.net.http.UserAgent;
import org.droidparts.net.http.worker.HTTPWorker;
import org.droidparts.net.http.worker.HttpClientWorker;
import org.droidparts.net.http.worker.OkHttpWorker;

import android.test.AndroidTestCase;

public class RESTClientPostFile extends AndroidTestCase {

	// Simple server for testing POST requests. Supports multipart/form-data
	// file uploads.
	private static final String POST_MULTIPART_URL = "http://posttestserver.com/post.php?dump";
	private static final String POST_MULTIPART_FILE_NAME = "test";
	private static final String POST_MULTIPART_FILE_BODY = "Test POST multipart file";

	public void testPostMultipartFile() throws Exception {
		testPostMultipartFile(null);
	}

	public void testPostMultipartFileOkHttp() throws Exception {
		HTTPWorker worker = new OkHttpWorker(getContext());
		testPostMultipartFile(worker);
	}

	public void testPostMultipartFileLegacy() throws Exception {
		HTTPWorker worker = new HttpClientWorker(UserAgent.getDefault());
		testPostMultipartFile(worker);
	}

	//

	public void testPostMultipartFile(HTTPWorker worker) throws Exception {
		RESTClient client;
		if (worker == null) {
			client = new RESTClient(getContext());
		} else {
			client = new RESTClient(getContext(), worker);
		}
		File file = writeTestFile(POST_MULTIPART_FILE_BODY);
		HTTPResponse resp = client.postFile(POST_MULTIPART_URL,
				POST_MULTIPART_FILE_NAME, file);
		assertPostMultipartFileResponse(resp);
	}

	private File writeTestFile(String data) throws IOException {
		File file = new File(getContext().getCacheDir(), "test.txt");
		FileWriter fw = new FileWriter(file);
		fw.write(data);
		fw.close();
		return file;
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
}
