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
import static org.apache.http.auth.AuthScope.ANY_HOST;
import static org.apache.http.auth.AuthScope.ANY_PORT;
import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY;
import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.contract.Constants.UTF8;
import static org.droidparts.util.io.IOUtils.silentlyClose;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.droidparts.util.L;

import android.util.Pair;

public class RESTClient {

	private static final int SOCKET_OPERATION_TIMEOUT = 60 * 1000;

	private DefaultHttpClient defaultHttpClient;

	private final HashMap<String, String> headers = new HashMap<String, String>();

	public RESTClient(String userAgent) {
		initClient(userAgent);
	}

	//

	public void setHeader(String key, String value) {
		if (isEmpty(key) || isEmpty(value)) {
			throw new IllegalArgumentException("Key: " + key + ", value: "
					+ value + " should be non-null.");
		} else {
			headers.put(key, value);
		}
	}

	public void setProxy(URL proxy, String username, String password) {
		HttpHost proxyHost = new HttpHost(proxy.getHost(), proxy.getPort(),
				proxy.getProtocol());
		defaultHttpClient.getParams().setParameter(DEFAULT_PROXY, proxyHost);
		if (!isEmpty(username) && !isEmpty(password)) {
			AuthScope authScope = new AuthScope(proxy.getHost(),
					proxy.getPort());
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
					username, password);
			// TODO androidHttpClient
			defaultHttpClient.getCredentialsProvider().setCredentials(
					authScope, credentials);
		}
	}

	public void authenticateBasic(String username, String password) {
		AuthScope authScope = new AuthScope(ANY_HOST, ANY_PORT);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
				username, password);
		// TODO androidHttpClient
		defaultHttpClient.getCredentialsProvider().setCredentials(authScope,
				credentials);
	}

	//

	public String get(String uri) throws HTTPException {
		L.d("GET on " + uri);
		HttpGet req = new HttpGet(uri);
		HttpResponse resp = getResponse(req);
		String respStr = getResponseBody(resp);
		consumeResponse(resp);
		return respStr;
	}

	public String put(String uri, String contentEncoding, String data)
			throws HTTPException {
		L.d("PUT on " + uri + ", data: " + data);
		HttpPut req = new HttpPut(uri);
		try {
			StringEntity entity = new StringEntity(data, UTF8);
			entity.setContentType(contentEncoding);
			req.setEntity(entity);
		} catch (UnsupportedEncodingException e) {
			throw new HTTPException(e);
		}
		HttpResponse resp = getResponse(req);
		Header loc = resp.getLastHeader("Location");
		consumeResponse(resp);
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
		HttpPost req = new HttpPost(uri);
		try {
			StringEntity entity = new StringEntity(data, UTF8);
			entity.setContentType(contentEncoding);
			req.setEntity(entity);
		} catch (UnsupportedEncodingException e) {
			L.e(e);
			throw new HTTPException(e);
		}
		HttpResponse resp = getResponse(req);
		String respStr = getResponseBody(resp);
		consumeResponse(resp);
		return respStr;
	}

	public void delete(String uri) throws HTTPException {
		L.d("DELETE on " + uri);
		HttpDelete req = new HttpDelete(uri);
		HttpResponse resp = getResponse(req);
		consumeResponse(resp);
	}

	public Pair<Integer, BufferedInputStream> getInputStream(String uri)
			throws HTTPException {
		L.d("InputStream on " + uri);
		HttpGet req = new HttpGet(uri);
		HttpResponse resp = getResponse(req);
		HttpEntity entity = resp.getEntity();
		// 2G limit
		int contentLength = (int) entity.getContentLength();
		try {
			InputStream is = getUnpackedInputStream(entity);
			BufferedInputStream bis = new EntityInputStream(is, entity);
			return new Pair<Integer, BufferedInputStream>(contentLength, bis);
		} catch (Exception e) {
			throw new HTTPException(e);
		}
	}

	protected HttpResponse getResponse(HttpUriRequest req) throws HTTPException {
		for (String name : headers.keySet()) {
			req.setHeader(name, headers.get(name));
		}
		req.setHeader("Accept-Encoding", "gzip,deflate");
		try {
			HttpResponse resp;
			synchronized (SingleClientConnManager.class) {
				resp = defaultHttpClient.execute(req);
			}
			int respCode = resp.getStatusLine().getStatusCode();
			if (respCode >= 400) {
				consumeResponse(resp);
				throw new HTTPException(respCode);
			}
			return resp;
		} catch (IOException e) {
			throw new HTTPException(e);
		}
	}

	protected void consumeResponse(HttpResponse resp) {
		try {
			resp.getEntity().consumeContent();
		} catch (IOException e) {
			L.d(e);
		}
	}

	protected String getResponseBody(HttpResponse resp) throws HTTPException {
		StringBuilder sb = new StringBuilder();
		HttpEntity entity = resp.getEntity();
		BufferedReader br = null;
		try {
			InputStream is = getUnpackedInputStream(entity);
			br = new BufferedReader(new InputStreamReader(is, UTF8),
					BUFFER_SIZE);
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			throw new HTTPException(e);
		} finally {
			silentlyClose(br);
		}
		String respStr = sb.toString();
		L.d("response: " + respStr);
		return respStr;
	}

	protected InputStream getUnpackedInputStream(HttpEntity entity)
			throws IOException {
		InputStream is = entity.getContent();
		Header contentEncodingHeader = entity.getContentEncoding();
		L.d(contentEncodingHeader);
		if (contentEncodingHeader != null) {
			String contentEncoding = contentEncodingHeader.getValue();
			if (!isEmpty(contentEncoding)) {
				contentEncoding = contentEncoding.toLowerCase();
				if (contentEncoding.contains("gzip")) {
					return new GZIPInputStream(is);
				} else if (contentEncoding.contains("deflate")) {
					return new InflaterInputStream(is);
				}
			}
		}
		return is;
	}

	private void initClient(String userAgent) {
		defaultHttpClient = new DefaultHttpClient();
		HttpParams params = defaultHttpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params,
				SOCKET_OPERATION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, SOCKET_OPERATION_TIMEOUT);
		HttpConnectionParams.setSocketBufferSize(params, BUFFER_SIZE);
		if (userAgent != null) {
			HttpProtocolParams.setUserAgent(params, userAgent);
		}
	}

	protected static final class EntityInputStream extends BufferedInputStream {

		private final HttpEntity entity;

		public EntityInputStream(InputStream in, HttpEntity entity) {
			super(in, BUFFER_SIZE);
			this.entity = entity;
		}

		@Override
		public synchronized void close() throws IOException {
			super.close();
			entity.consumeContent();
		}

	}
}
