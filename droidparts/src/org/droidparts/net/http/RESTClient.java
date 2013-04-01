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
package org.droidparts.net.http;

import java.net.HttpURLConnection;
import java.util.Date;

import org.apache.http.auth.AuthScope;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.droidparts.contract.HTTP.Header;
import org.droidparts.contract.HTTP.Method;
import org.droidparts.net.http.worker.HTTPWorker;
import org.droidparts.net.http.worker.HttpClientWorker;
import org.droidparts.net.http.worker.HttpURLConnectionWorker;
import org.droidparts.util.L;

import android.content.Context;
import android.os.Build;

public class RESTClient {

	private final Context ctx;

	private final boolean forceApacheHttpClient;

	private final HttpClientWorker httpClientWorker;
	private final HttpURLConnectionWorker httpURLConnectionWorker;
	private static volatile CookieJar cookieJar;

	public static String getUserAgent(String nameHint) {
		return ((nameHint != null) ? nameHint : " DroidParts.org")
				+ " (Android " + Build.VERSION.RELEASE + "; " + Build.MODEL
				+ " Build/" + Build.ID + ")";
	}

	public RESTClient(Context ctx) {
		this(ctx, getUserAgent(null), false);
	}

	public RESTClient(Context ctx, String userAgent,
			boolean forceApacheHttpClient) {
		this.ctx = ctx.getApplicationContext();
		this.forceApacheHttpClient = forceApacheHttpClient;
		httpClientWorker = useHttpURLConnection() ? null
				: new HttpClientWorker(userAgent);
		httpURLConnectionWorker = useHttpURLConnection() ? new HttpURLConnectionWorker(
				userAgent) : null;
		if (cookieJar == null) {
			cookieJar = new CookieJar(ctx);
		}
		if (Build.VERSION.SDK_INT >= 14) {
			HttpURLConnectionWorker.setHttpResponseCacheEnabled(ctx, true);
		}
	}

	public Context getContext() {
		return ctx;
	}

	public void setCookieCacheEnabled(boolean enabled, boolean persistent) {
		cookieJar.setPersistent(persistent);
		getWorker().setCookieJar(enabled ? cookieJar : null);
	}

	public void putHeader(String key, String value) {
		getWorker().putHeader(key, value);
	}

	public void authenticateBasic(String username, String password) {
		authenticateBasic(username, password, AuthScope.ANY);
	}

	public void authenticateBasic(String username, String password,
			AuthScope scope) {
		getWorker().authenticateBasic(username, password, scope);
	}

	//

	public HTTPResponse get(String uri) throws HTTPException {
		return get(uri, -1, null, true);
	}

	public HTTPResponse getInputStream(String uri) throws HTTPException {
		return get(uri, -1, null, false);
	}

	public HTTPResponse get(String uri, long ifModifiedSince, String etag,
			boolean body) throws HTTPException {
		L.i("GET on '%s', If-Modified-Since: '%d', ETag: '%s', body: '%b'.",
				uri, ifModifiedSince, etag, body);
		HTTPResponse response;
		if (useHttpURLConnection()) {
			HttpURLConnection conn = httpURLConnectionWorker.getConnection(uri,
					Method.GET);
			if (ifModifiedSince > 0) {
				conn.setIfModifiedSince(ifModifiedSince);
			}
			if (etag != null) {
				conn.addRequestProperty(Header.IF_NONE_MATCH, etag);
			}
			response = HttpURLConnectionWorker.getReponse(conn, body);
		} else {
			HttpGet req = new HttpGet(uri);
			if (ifModifiedSince > 0) {
				req.addHeader(Header.IF_MODIFIED_SINCE, new Date(
						ifModifiedSince).toGMTString());
			}
			if (etag != null) {
				req.addHeader(Header.IF_NONE_MATCH, etag);
			}
			response = httpClientWorker.getReponse(req, body);
		}
		L.d(response);
		return response;
	}

	public HTTPResponse post(String uri, String contentType, String data)
			throws HTTPException {
		L.i("POST on '%s', data: '%s'.", uri, data);
		HTTPResponse response;
		if (useHttpURLConnection()) {
			HttpURLConnection conn = httpURLConnectionWorker.getConnection(uri,
					Method.POST);
			HttpURLConnectionWorker.postOrPut(conn, contentType, data);
			response = HttpURLConnectionWorker.getReponse(conn, true);
		} else {
			HttpPost req = new HttpPost(uri);
			req.setEntity(HttpClientWorker.buildStringEntity(contentType, data));
			response = httpClientWorker.getReponse(req, true);
		}
		L.d(response);
		return response;
	}

	public HTTPResponse put(String uri, String contentType, String data)
			throws HTTPException {
		L.i("PUT on '%s', data: '%s'.", uri, data);
		HTTPResponse response;
		if (useHttpURLConnection()) {
			HttpURLConnection conn = httpURLConnectionWorker.getConnection(uri,
					Method.PUT);
			HttpURLConnectionWorker.postOrPut(conn, contentType, data);
			response = HttpURLConnectionWorker.getReponse(conn, true);
		} else {
			HttpPut req = new HttpPut(uri);
			req.setEntity(HttpClientWorker.buildStringEntity(contentType, data));
			response = httpClientWorker.getReponse(req, true);
		}
		L.d(response);
		return response;
	}

	public HTTPResponse delete(String uri) throws HTTPException {
		L.i("DELETE on '%s'.", uri);
		HTTPResponse response;
		if (useHttpURLConnection()) {
			HttpURLConnection conn = httpURLConnectionWorker.getConnection(uri,
					Method.DELETE);
			response = HttpURLConnectionWorker.getReponse(conn, true);
		} else {
			HttpDelete req = new HttpDelete(uri);
			response = httpClientWorker.getReponse(req, true);
		}
		L.d(response);
		return response;
	}

	//

	protected final HTTPWorker getWorker() {
		HTTPWorker worker = (httpClientWorker != null) ? httpClientWorker
				: httpURLConnectionWorker;
		return worker;
	}

	private boolean useHttpURLConnection() {
		// http://android-developers.blogspot.com/2011/09/androids-http-clients.html
		boolean recentAndroid = Build.VERSION.SDK_INT >= 10;
		return recentAndroid && !forceApacheHttpClient;
	}
}
