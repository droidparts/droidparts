/**
 * Copyright 2014 Alex Yanchenko
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

import static org.apache.http.client.params.CookiePolicy.BROWSER_COMPATIBILITY;
import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.contract.Constants.UTF8;
import static org.droidparts.contract.HTTP.Header.ACCEPT_ENCODING;

import java.io.File;
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
import org.droidparts.net.http.HTTPException;
import org.droidparts.net.http.HTTPResponse;
import org.droidparts.net.http.worker.wrapper.HttpMimeWrapper;

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
		HttpConnectionParams.setConnectionTimeout(params,
				SOCKET_OPERATION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, SOCKET_OPERATION_TIMEOUT);
		HttpConnectionParams.setSocketBufferSize(params, BUFFER_SIZE);
		HttpClientParams.setCookiePolicy(params, BROWSER_COMPATIBILITY);
	}

	@Override
	public void setFollowRedirects(boolean follow) {
		HttpClientParams
				.setRedirecting(httpClient.getParams(), followRedirects);
	}

	@Override
	public void setCookieJar(CookieJar cookieJar) {
		httpClient.setCookieStore(cookieJar);
	}

	public final DefaultHttpClient getHttpClient() {
		return httpClient;
	}

	public static StringEntity buildStringEntity(String contentType, String data)
			throws HTTPException {
		try {
			StringEntity entity = new StringEntity(data, UTF8);
			entity.setContentType(contentType);
			return entity;
		} catch (UnsupportedEncodingException e) {
			throw new HTTPException(e);
		}
	}

	public static HttpEntity buildMultipartEntity(String name,
			String contentType, File file) {
		try {
			return HttpMimeWrapper
					.buildMultipartEntity(name, contentType, file);
		} catch (Exception e) {
			throw new IllegalStateException(
					"You have to add Apache HttpMime dependency in order to use multipart entities.",
					e);
		}
	}

	public HTTPResponse getResponse(HttpUriRequest req, boolean body)
			throws HTTPException {
		HTTPResponse response = new HTTPResponse();
		HttpResponse resp = getHttpResponse(req);
		response.code = getResponseCodeOrThrow(resp);
		response.headers = getHeaders(resp);
		HTTPInputStream is = HTTPInputStream.getInstance(resp);
		if (body) {
			response.body = is.readAndClose();
		} else {
			response.inputStream = is;
		}
		return response;
	}

	private HttpResponse getHttpResponse(HttpUriRequest req)
			throws HTTPException {
		for (String key : headers.keySet()) {
			req.addHeader(key, headers.get(key));
		}
		req.setHeader(ACCEPT_ENCODING, "gzip,deflate");
		try {
			return httpClient.execute(req);
		} catch (Exception e) {
			throwIfNetworkOnMainThreadException(e);
			throw new HTTPException(e);
		}
	}

	private static int getResponseCodeOrThrow(HttpResponse resp)
			throws HTTPException {
		int respCode = resp.getStatusLine().getStatusCode();
		if (isErrorResponseCode(respCode)) {
			String respBody = HTTPInputStream.getInstance(resp).readAndClose();
			throw new HTTPException(respCode, respBody);
		}
		return respCode;
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