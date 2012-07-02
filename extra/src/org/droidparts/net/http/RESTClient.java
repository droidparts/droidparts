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
package org.droidparts.net.http;

import static android.text.TextUtils.isEmpty;
import static org.droidparts.contract.Constants.UTF8;
import static org.droidparts.net.http.wrapper.HttpClientWrapper.useHttpURLConnection;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.HashMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.droidparts.net.http.wrapper.DefaultHttpClientWrapper;
import org.droidparts.net.http.wrapper.DefaultHttpClientWrapper.EntityInputStream;
import org.droidparts.net.http.wrapper.HttpClientWrapper;
import org.droidparts.net.http.wrapper.HttpURLConnectionWrapper;
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
		setHttpResponseCacheEnabled(true);
	}

	public void setHttpResponseCacheEnabled(boolean enabled) {
		if (Build.VERSION.SDK_INT >= 14) {
			HttpURLConnectionWrapper.setHttpResponseCacheEnabled(ctx, enabled);
		} else {
			L.i("HTTP response cache not supported.");
		}
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

	public String get(String uri) throws HTTPException {
		L.d("GET on " + uri);
		String respStr;
		if (useHttpURLConnection()) {
			HttpURLConnectionWrapper wrapper = getModern();
			HttpURLConnection conn = wrapper.getConnectedHttpURLConnection(uri,
					"GET");
			respStr = wrapper.getResponseBodyAndDisconnect(conn);
		} else {
			DefaultHttpClientWrapper wrapper = getLegacy();
			HttpGet req = new HttpGet(uri);
			HttpResponse resp = wrapper.getResponse(req);
			respStr = wrapper.getResponseBody(resp);
			wrapper.consumeResponse(resp);
		}
		return respStr;
	}

	public String put(String uri, String contentEncoding, String data)
			throws HTTPException {
		L.d("PUT on " + uri + ", data: " + data);
		// TODO useModern()
		HttpPut req = new HttpPut(uri);
		try {
			StringEntity entity = new StringEntity(data, UTF8);
			entity.setContentType(contentEncoding);
			req.setEntity(entity);
		} catch (UnsupportedEncodingException e) {
			throw new HTTPException(e);
		}
		DefaultHttpClientWrapper wrapper = getLegacy();
		HttpResponse resp = wrapper.getResponse(req);
		Header loc = resp.getLastHeader("Location");
		wrapper.consumeResponse(resp);
		if (loc != null) {
			String[] parts = loc.getValue().split("/");
			String location = parts[parts.length - 1];
			L.d("location: " + location);
			return location;
		} else {
			return null;
		}
	}

	public String post(String uri, String contentEncoding, String data)
			throws HTTPException {
		L.d("POST on " + uri + ", data: " + data);
		// TODO useModern()
		HttpPost req = new HttpPost(uri);
		try {
			StringEntity entity = new StringEntity(data, UTF8);
			entity.setContentType(contentEncoding);
			req.setEntity(entity);
		} catch (UnsupportedEncodingException e) {
			L.e(e);
			throw new HTTPException(e);
		}
		DefaultHttpClientWrapper wrapper = getLegacy();
		HttpResponse resp = wrapper.getResponse(req);
		String respStr = wrapper.getResponseBody(resp);
		wrapper.consumeResponse(resp);
		return respStr;
	}

	public void delete(String uri) throws HTTPException {
		L.d("DELETE on " + uri);
		// TODO useModern()
		DefaultHttpClientWrapper wrapper = getLegacy();
		HttpDelete req = new HttpDelete(uri);
		HttpResponse resp = wrapper.getResponse(req);
		wrapper.consumeResponse(resp);
	}

	public Pair<Integer, BufferedInputStream> getInputStream(String uri)
			throws HTTPException {
		L.d("InputStream on " + uri);
		// TODO useModern()
		DefaultHttpClientWrapper wrapper = getLegacy();
		HttpGet req = new HttpGet(uri);
		HttpResponse resp = wrapper.getResponse(req);
		HttpEntity entity = resp.getEntity();
		// 2G limit
		int contentLength = (int) entity.getContentLength();
		try {
			InputStream is = wrapper.getUnpackedInputStream(entity);
			BufferedInputStream bis = new EntityInputStream(is, entity);
			return new Pair<Integer, BufferedInputStream>(contentLength, bis);
		} catch (Exception e) {
			throw new HTTPException(e);
		}
	}

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

	private void initWrapper(HttpClientWrapper wrapper) {
		wrapper.setHeaders(headers);
		if (proxyUrl != null) {
			wrapper.setProxy(proxyUrl, proxyUser, proxyPassword);
		}
		if (authUser != null && authPassword != null) {
			wrapper.authenticateBasic(authUser, authPassword);
		}
	}
}
