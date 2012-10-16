/**
 * Copyright 2012 Alex Yanchenko
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
package org.droidparts.http;

import static android.text.TextUtils.isEmpty;
import static org.droidparts.http.wrapper.HttpClientWrapper.useHttpURLConnection;
import static org.droidparts.http.wrapper.HttpURLConnectionWrapper.DELETE;
import static org.droidparts.http.wrapper.HttpURLConnectionWrapper.GET;
import static org.droidparts.http.wrapper.HttpURLConnectionWrapper.POST;
import static org.droidparts.http.wrapper.HttpURLConnectionWrapper.PUT;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.droidparts.http.wrapper.ConsumingInputStream;
import org.droidparts.http.wrapper.DefaultHttpClientWrapper;
import org.droidparts.http.wrapper.HttpClientWrapper;
import org.droidparts.http.wrapper.HttpURLConnectionWrapper;
import org.droidparts.util.L;

import android.content.Context;
import android.os.Build;
import android.util.Pair;

public class RESTClient {

	private final Context ctx;

	private final String userAgent;
	private final HashMap<String, String> headers = new HashMap<String, String>();

	private String authUser, authPassword;
	private String proxyUrl, proxyUser, proxyPassword;

	public RESTClient(Context ctx, String userAgent) {
		this.ctx = ctx.getApplicationContext();
		this.userAgent = userAgent;
		if (Build.VERSION.SDK_INT >= 14) {
			setHttpResponseCacheEnabled(true);
		}
	}

	public void setHttpResponseCacheEnabled(boolean enabled) {
		HttpURLConnectionWrapper.setHttpResponseCacheEnabled(ctx, enabled);
	}

	public void setHeader(String key, String value) {
		if (isEmpty(key) || isEmpty(value)) {
			throw new IllegalArgumentException("Key: " + key + ", value: "
					+ value + " should be non-null.");
		} else {
			headers.put(key, value);
		}
	}

	public void setProxy(String proxy, String username, String password) {
		this.proxyUrl = proxy;
		this.proxyUser = username;
		this.proxyPassword = password;
	}

	public void authenticateBasic(String username, String password) {
		this.authUser = username;
		this.authPassword = password;
	}

	//

	public HTTPResponse get(String uri) throws HTTPException {
		L.d("GET on " + uri);
		HTTPResponse response;
		if (useHttpURLConnection()) {
			HttpURLConnection conn = getModern().getConnection(uri, GET);
			response = HttpURLConnectionWrapper.getReponse(conn);
		} else {
			HttpGet req = new HttpGet(uri);
			response = DefaultHttpClientWrapper.getReponse(getLegacy(), req);
		}
		L.d(response);
		return response;
	}

	public HTTPResponse post(String uri, String contentType, String data)
			throws HTTPException {
		L.d("POST on " + uri + ", data: " + data);
		HTTPResponse response;
		if (useHttpURLConnection()) {
			HttpURLConnection conn = getModern().getConnection(uri, POST);
			HttpURLConnectionWrapper.postOrPut(conn, contentType, data);
			response = HttpURLConnectionWrapper.getReponse(conn);
		} else {
			HttpPost req = new HttpPost(uri);
			req.setEntity(DefaultHttpClientWrapper.buildStringEntity(
					contentType, data));
			response = DefaultHttpClientWrapper.getReponse(getLegacy(), req);
		}
		L.d(response);
		return response;
	}

	public HTTPResponse put(String uri, String contentType, String data)
			throws HTTPException {
		L.d("PUT on " + uri + ", data: " + data);
		HTTPResponse response;
		if (useHttpURLConnection()) {
			HttpURLConnection conn = getModern().getConnection(uri, PUT);
			HttpURLConnectionWrapper.postOrPut(conn, contentType, data);
			response = HttpURLConnectionWrapper.getReponse(conn);
		} else {
			HttpPut req = new HttpPut(uri);
			req.setEntity(DefaultHttpClientWrapper.buildStringEntity(
					contentType, data));
			response = DefaultHttpClientWrapper.getReponse(getLegacy(), req);
		}
		L.d(response);
		return response;
	}

	public HTTPResponse delete(String uri) throws HTTPException {
		L.d("DELETE on " + uri);
		HTTPResponse response;
		if (useHttpURLConnection()) {
			HttpURLConnection conn = getModern().getConnection(uri, DELETE);
			response = HttpURLConnectionWrapper.getReponse(conn);
		} else {
			HttpDelete req = new HttpDelete(uri);
			response = DefaultHttpClientWrapper.getReponse(getLegacy(), req);
		}
		L.d(response);
		return response;
	}

	public Pair<Integer, BufferedInputStream> getInputStream(String uri)
			throws HTTPException {
		L.d("InputStream on " + uri);
		int contentLength = -1;
		ConsumingInputStream cis = null;
		if (useHttpURLConnection()) {
			HttpURLConnectionWrapper wrapper = getModern();
			HttpURLConnection conn = wrapper.getConnection(uri, GET);
			HttpURLConnectionWrapper.connectAndGetResponseCodeOrThrow(conn);
			contentLength = conn.getContentLength();
			cis = new ConsumingInputStream(
					HttpURLConnectionWrapper.getUnpackedInputStream(conn), conn);
		} else {
			DefaultHttpClientWrapper wrapper = getLegacy();
			HttpGet req = new HttpGet(uri);
			HttpResponse resp = wrapper.getResponse(req);
			HttpEntity entity = resp.getEntity();
			// 2G limit
			contentLength = (int) entity.getContentLength();
			cis = new ConsumingInputStream(
					DefaultHttpClientWrapper.getUnpackedInputStream(entity),
					entity);
		}
		L.d("Content-Length: " + contentLength);
		return new Pair<Integer, BufferedInputStream>(contentLength, cis);
	}

	//

	private DefaultHttpClientWrapper getLegacy() {
		DefaultHttpClientWrapper wrapper = new DefaultHttpClientWrapper(
				userAgent);
		initWrapper(wrapper);
		return wrapper;
	}

	private HttpURLConnectionWrapper getModern() {
		HttpURLConnectionWrapper wrapper = new HttpURLConnectionWrapper(
				userAgent);
		initWrapper(wrapper);
		return wrapper;
	}

	private void initWrapper(HttpClientWrapper<?> wrapper) {
		wrapper.setHeaders(headers);
		if (proxyUrl != null) {
			wrapper.setProxy(proxyUrl, proxyUser, proxyPassword);
		}
		if (authUser != null && authPassword != null) {
			wrapper.authenticateBasic(authUser, authPassword);
		}
	}
}
