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
package org.droidparts.net.http.worker;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import org.droidparts.net.http.CookieJar;
import org.droidparts.net.http.HTTPResponse;
import org.droidparts.net.http.worker.wrapper.HttpMimeWrapper;

import static org.apache.http.client.params.CookiePolicy.BROWSER_COMPATIBILITY;

import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.contract.Constants.UTF8;
import static org.droidparts.contract.HTTP.Header.ACCEPT_ENCODING;

// For API < 10
public class HttpClientWorker extends HTTPWorker {

	private final DefaultHttpClient httpClient;

	public HttpClientWorker(String userAgent) {
		httpClient = new DefaultHttpClient();
		HttpParams params = httpClient.getParams();
		if (userAgent != null) {
			HttpProtocolParams.setUserAgent(params, userAgent);
		}
		HttpConnectionParams.setStaleCheckingEnabled(params, false);
		setFollowRedirects(followRedirects);
		HttpConnectionParams.setConnectionTimeout(params, SOCKET_OPERATION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, SOCKET_OPERATION_TIMEOUT);
		HttpConnectionParams.setSocketBufferSize(params, BUFFER_SIZE);
		HttpClientParams.setCookiePolicy(params, BROWSER_COMPATIBILITY);
	}

	@Override
	public void setFollowRedirects(boolean follow) {
		HttpClientParams.setRedirecting(httpClient.getParams(), followRedirects);
	}

	@Override
	public void setCookieJar(CookieJar cookieJar) {
		httpClient.setCookieStore(cookieJar);
	}

	public final DefaultHttpClient getHttpClient() {
		return httpClient;
	}

	public static StringEntity buildStringEntity(String contentType, String data) throws UnsupportedEncodingException {
		StringEntity entity = new StringEntity(data, UTF8);
		entity.setContentType(contentType);
		return entity;
	}

	public static HttpEntity buildMultipartEntity(String name, String contentType, String fileName, InputStream is) throws IOException {
		return HttpMimeWrapper.buildMultipartEntity(name, contentType, fileName, is);
	}

	public HTTPResponse getResponse(HttpUriRequest req, boolean body) throws IOException {
		HttpResponse resp = getHttpResponse(req);
		int code = getResponseCode(resp);
		Map<String, List<String>> headers = getHeaders(resp);
		HTTPInputStream is = HTTPInputStream.getInstance(resp);
		String respBody = null;
		HTTPInputStream respStream = null;
		if (body) {
			respBody = is.readAndClose();
		} else {
			respStream = is;
		}
		return new HTTPResponse(code, headers, respBody, respStream);
	}

	private HttpResponse getHttpResponse(HttpUriRequest req) throws IOException {
		for (String key : headers.keySet()) {
			req.addHeader(key, headers.get(key));
		}
		req.setHeader(ACCEPT_ENCODING, "gzip,deflate");
		return httpClient.execute(req);
	}

	private static int getResponseCode(HttpResponse resp) {
		return resp.getStatusLine().getStatusCode();
	}

	private static Map<String, List<String>> getHeaders(HttpResponse resp) {
		HashMap<String, List<String>> headers = new HashMap<String, List<String>>();
		for (Header header : resp.getAllHeaders()) {
			String name = header.getName();
			if (!headers.containsKey(name)) {
				headers.put(name, new ArrayList<String>());
			}
			headers.get(name).add(header.getValue());
		}
		return headers;
	}

}