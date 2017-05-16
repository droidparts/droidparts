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
package org.droidparts.net.http;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Map;

import android.content.Context;
import android.os.Build;

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

public class RESTClient {

	private final Context ctx;

	private final HttpClientWorker httpClientWorker;
	private final HttpURLConnectionWorker httpURLConnectionWorker;

	private static volatile CookieJar cookieJar;

	public RESTClient(Context ctx) {
		this(ctx, UserAgent.getDefault());
	}

	public RESTClient(Context ctx, String userAgent) {
		this(ctx, (Build.VERSION.SDK_INT >= 10) ? new HttpURLConnectionWorker(ctx, userAgent)
				: new HttpClientWorker(userAgent));
	}

	public RESTClient(Context ctx, HTTPWorker worker) {
		this.ctx = ctx.getApplicationContext();
		this.httpClientWorker = (worker instanceof HttpClientWorker) ? (HttpClientWorker) worker : null;
		this.httpURLConnectionWorker = (worker instanceof HttpURLConnectionWorker) ? (HttpURLConnectionWorker) worker
				: null;
		if (cookieJar == null) {
			cookieJar = new CookieJar(ctx);
		}
	}

	protected Context getContext() {
		return ctx;
	}

	public void setCookieCacheEnabled(boolean enabled, boolean persistent) {
		cookieJar.setPersistent(persistent);
		getWorker().setCookieJar(enabled ? cookieJar : null);
	}

	public void setFollowRedirects(boolean follow) {
		getWorker().setFollowRedirects(follow);
	}

	public void setHeader(String key, String value) {
		getWorker().setHeader(key, value);
	}

	public Map<String, String> getHeaders() {
		return getWorker().headers;
	}

	public void authenticateBasic(String username, String password) {
		getWorker().authenticateBasic(username, password);
	}

	//

	public HTTPResponse get(String uri) throws HTTPException {
		return get(uri, -1, null, true);
	}

	public HTTPResponse getInputStream(String uri) throws HTTPException {
		return get(uri, -1, null, false);
	}

	public HTTPResponse get(String uri, long ifModifiedSince, String etag, boolean body) throws HTTPException {
		L.d("HTTP GET '%s', If-Modified-Since: '%d', ETag: '%s', body: '%b'.", uri, ifModifiedSince, etag, body);
		HTTPResponse response;
		if (httpURLConnectionWorker != null) {
			HttpURLConnection conn = httpURLConnectionWorker.getConnection(uri, Method.GET);
			if (ifModifiedSince > 0) {
				conn.setIfModifiedSince(ifModifiedSince);
			}
			if (etag != null) {
				conn.addRequestProperty(Header.IF_NONE_MATCH, etag);
			}
			response = httpURLConnectionWorker.getResponse(conn, body);
		} else {
			HttpGet req = new HttpGet(uri);
			if (ifModifiedSince > 0) {
				req.addHeader(Header.IF_MODIFIED_SINCE, new Date(ifModifiedSince).toGMTString());
			}
			if (etag != null) {
				req.addHeader(Header.IF_NONE_MATCH, etag);
			}
			response = httpClientWorker.getResponse(req, body);
		}
		L.d(response);
		return response;
	}

	public HTTPResponse post(String uri, String contentType, String data) throws HTTPException {
		L.d("HTTP POST '%s', data: '%s'.", uri, data);
		HTTPResponse response;
		if (httpURLConnectionWorker != null) {
			HttpURLConnection conn = httpURLConnectionWorker.getConnection(uri, Method.POST);
			httpURLConnectionWorker.postOrPut(conn, contentType, data);
			response = httpURLConnectionWorker.getResponse(conn, true);
		} else {
			HttpPost req = new HttpPost(uri);
			req.setEntity(HttpClientWorker.buildStringEntity(contentType, data));
			response = httpClientWorker.getResponse(req, true);
		}
		L.d(response);
		return response;
	}

	public HTTPResponse postMultipart(String uri, String name, String contentType, String fileName, InputStream is)
			throws HTTPException {
		L.d("HTTP POST, name: '%s', file: '%s' .", uri, name, fileName);
		HTTPResponse response;
		if (httpURLConnectionWorker != null) {
			HttpURLConnection conn = httpURLConnectionWorker.getConnection(uri, Method.POST);
			httpURLConnectionWorker.postMultipart(conn, name, contentType, fileName, is);
			response = httpURLConnectionWorker.getResponse(conn, true);
		} else {
			HttpPost req = new HttpPost(uri);
			req.setEntity(HttpClientWorker.buildMultipartEntity(name, contentType, fileName, is));
			response = httpClientWorker.getResponse(req, true);
		}
		return response;
	}

	public HTTPResponse put(String uri, String contentType, String data) throws HTTPException {
		L.d("HTTP PUT '%s', data: '%s'.", uri, data);
		HTTPResponse response;
		if (httpURLConnectionWorker != null) {
			HttpURLConnection conn = httpURLConnectionWorker.getConnection(uri, Method.PUT);
			httpURLConnectionWorker.postOrPut(conn, contentType, data);
			response = httpURLConnectionWorker.getResponse(conn, true);
		} else {
			HttpPut req = new HttpPut(uri);
			req.setEntity(HttpClientWorker.buildStringEntity(contentType, data));
			response = httpClientWorker.getResponse(req, true);
		}
		L.d(response);
		return response;
	}

	public HTTPResponse delete(String uri) throws HTTPException {
		L.d("HTTP DELETE '%s'.", uri);
		HTTPResponse response;
		if (httpURLConnectionWorker != null) {
			HttpURLConnection conn = httpURLConnectionWorker.getConnection(uri, Method.DELETE);
			response = httpURLConnectionWorker.getResponse(conn, true);
		} else {
			HttpDelete req = new HttpDelete(uri);
			response = httpClientWorker.getResponse(req, true);
		}
		L.d(response);
		return response;
	}

	protected final HTTPWorker getWorker() {
		HTTPWorker worker = (httpClientWorker != null) ? httpClientWorker : httpURLConnectionWorker;
		return worker;
	}

}
