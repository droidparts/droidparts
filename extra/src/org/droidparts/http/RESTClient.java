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

import static org.droidparts.http.worker.HttpURLConnectionWorker.DELETE;
import static org.droidparts.http.worker.HttpURLConnectionWorker.GET;
import static org.droidparts.http.worker.HttpURLConnectionWorker.POST;
import static org.droidparts.http.worker.HttpURLConnectionWorker.PUT;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.droidparts.http.worker.HTTPWorker;
import org.droidparts.http.worker.HttpClientWorker;
import org.droidparts.http.worker.HttpURLConnectionWorker;
import org.droidparts.util.L;

import android.content.Context;
import android.os.Build;
import android.util.Pair;

public class RESTClient {

	private final Context ctx;
	private final boolean forceApacheHttpClient;

	private final HttpClientWorker httpClientWorker;
	private final HttpURLConnectionWorker httpURLConnectionWorker;
	private static volatile CookieJar cookieJar;

	public RESTClient(Context ctx, String userAgent) {
		this(ctx, userAgent, false);
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
			setHttpResponseCacheEnabled(true);
		}
		setCookieCacheEnabled(true, false);
	}

	public void setHttpResponseCacheEnabled(boolean enabled) {
		HttpURLConnectionWorker.setHttpResponseCacheEnabled(ctx, enabled);
	}

	public void setCookieCacheEnabled(boolean enabled, boolean persistent) {
		cookieJar.setPersistent(persistent);
		getWorker().setCookieJar(enabled ? cookieJar : null);
	}

	public void addHeader(String key, String value) {
		getWorker().addHeader(key, value);
	}

	public void setProxy(String proxy, String username, String password) {
		getWorker().setProxy(proxy, username, password);
	}

	public void authenticateBasic(String username, String password) {
		getWorker().authenticateBasic(username, password);
	}

	//

	public HTTPResponse get(String uri) throws HTTPException {
		L.d("GET on " + uri);
		HTTPResponse response;
		if (useHttpURLConnection()) {
			HttpURLConnection conn = httpURLConnectionWorker.getConnection(uri,
					GET);
			response = HttpURLConnectionWorker.getReponse(conn);
		} else {
			HttpGet req = new HttpGet(uri);
			response = httpClientWorker.getReponse(req);
		}
		L.d(response);
		return response;
	}

	public HTTPResponse post(String uri, String contentType, String data)
			throws HTTPException {
		L.d("POST on " + uri + ", data: " + data);
		HTTPResponse response;
		if (useHttpURLConnection()) {
			HttpURLConnection conn = httpURLConnectionWorker.getConnection(uri,
					POST);
			HttpURLConnectionWorker.postOrPut(conn, contentType, data);
			response = HttpURLConnectionWorker.getReponse(conn);
		} else {
			HttpPost req = new HttpPost(uri);
			req.setEntity(HttpClientWorker.buildStringEntity(contentType, data));
			response = httpClientWorker.getReponse(req);
		}
		L.d(response);
		return response;
	}

	public HTTPResponse put(String uri, String contentType, String data)
			throws HTTPException {
		L.d("PUT on " + uri + ", data: " + data);
		HTTPResponse response;
		if (useHttpURLConnection()) {
			HttpURLConnection conn = httpURLConnectionWorker.getConnection(uri,
					PUT);
			HttpURLConnectionWorker.postOrPut(conn, contentType, data);
			response = HttpURLConnectionWorker.getReponse(conn);
		} else {
			HttpPut req = new HttpPut(uri);
			req.setEntity(HttpClientWorker.buildStringEntity(contentType, data));
			response = httpClientWorker.getReponse(req);
		}
		L.d(response);
		return response;
	}

	public HTTPResponse delete(String uri) throws HTTPException {
		L.d("DELETE on " + uri);
		HTTPResponse response;
		if (useHttpURLConnection()) {
			HttpURLConnection conn = httpURLConnectionWorker.getConnection(uri,
					DELETE);
			response = HttpURLConnectionWorker.getReponse(conn);
		} else {
			HttpDelete req = new HttpDelete(uri);
			response = httpClientWorker.getReponse(req);
		}
		L.d(response);
		return response;
	}

	public Pair<Integer, BufferedInputStream> getInputStream(String uri)
			throws HTTPException {
		L.d("InputStream on " + uri);
		Pair<Integer, BufferedInputStream> resp = null;
		if (useHttpURLConnection()) {
			resp = httpURLConnectionWorker.getInputStream(uri);
		} else {
			resp = httpClientWorker.getInputStream(uri);
		}
		L.d("Content-Length: " + resp.first);
		return resp;
	}

	//

	private HTTPWorker<?> getWorker() {
		HTTPWorker<?> worker = (httpClientWorker != null) ? httpClientWorker
				: httpURLConnectionWorker;
		return worker;
	}

	private boolean useHttpURLConnection() {
		// http://android-developers.blogspot.com/2011/09/androids-http-clients.html
		boolean recentAndroid = Build.VERSION.SDK_INT >= 10;
		return recentAndroid && !forceApacheHttpClient;
	}
}
