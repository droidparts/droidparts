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
package org.droidparts.http.worker;

import static org.apache.http.auth.AuthScope.ANY_HOST;
import static org.apache.http.auth.AuthScope.ANY_PORT;
import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY;
import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.contract.Constants.UTF8;
import static org.droidparts.util.Strings.isNotEmpty;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.droidparts.http.HTTPException;
import org.droidparts.http.HTTPResponse;
import org.droidparts.util.L;
import org.droidparts.util.io.IOUtils;

import android.util.Pair;

// For API < 10
public class HttpClientWorker extends HTTPWorker<HttpResponse> {

	private final DefaultHttpClient httpClient;

	public HttpClientWorker(String userAgent) {
		super(userAgent);
		httpClient = new DefaultHttpClient();
		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setStaleCheckingEnabled(params, false);
		HttpClientParams.setRedirecting(params, false);
		HttpConnectionParams.setConnectionTimeout(params,
				SOCKET_OPERATION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, SOCKET_OPERATION_TIMEOUT);
		HttpConnectionParams.setSocketBufferSize(params, BUFFER_SIZE);
		if (userAgent != null) {
			HttpProtocolParams.setUserAgent(params, userAgent);
		}
	}

	@Override
	protected void setProxy(String protocol, String host, int port,
			String user, String password) {
		HttpHost proxyHost = new HttpHost(host, port, protocol);
		httpClient.getParams().setParameter(DEFAULT_PROXY, proxyHost);
		if (isNotEmpty(user) && isNotEmpty(password)) {
			AuthScope authScope = new AuthScope(host, port);
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
					user, password);
			httpClient.getCredentialsProvider().setCredentials(authScope,
					credentials);
		}
	}

	@Override
	public void authenticateBasic(String user, String password) {
		AuthScope authScope = new AuthScope(ANY_HOST, ANY_PORT);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
				user, password);
		httpClient.getCredentialsProvider().setCredentials(authScope,
				credentials);
	}

	public static StringEntity buildStringEntity(String contentType, String data)
			throws HTTPException {
		try {
			StringEntity entity = new StringEntity(data, UTF8);
			entity.setContentType(contentType);
			return entity;
		} catch (UnsupportedEncodingException e) {
			L.e(e);
			throw new HTTPException(e);
		}
	}

	public HTTPResponse getReponse(HttpUriRequest req) throws HTTPException {
		HTTPResponse response = new HTTPResponse();
		HttpResponse resp = getHttpResponse(req);
		response.code = getResponseCodeOrThrow(resp);
		response.headers = getHeaders(resp);
		response.body = getResponseBodyAndConsume(resp);
		return response;
	}

	public Pair<Integer, BufferedInputStream> getInputStream(String uri)
			throws HTTPException {
		HttpGet req = new HttpGet(uri);
		HttpResponse resp = getHttpResponse(req);
		HttpEntity entity = resp.getEntity();
		// 2G limit
		int contentLength = (int) entity.getContentLength();
		ConsumingInputStream cis = new ConsumingInputStream(
				getUnpackedInputStream(entity), entity);
		return new Pair<Integer, BufferedInputStream>(contentLength, cis);
	}

	private HttpResponse getHttpResponse(HttpUriRequest req)
			throws HTTPException {
		for (String key : headers.keySet()) {
			for (String val : headers.get(key)) {
				req.addHeader(key, val);
			}
		}
		req.setHeader("Accept-Encoding", "gzip,deflate");
		try {
			return httpClient.execute(req);
		} catch (Exception e) {
			throw new HTTPException(e);
		}
	}

	private static InputStream getUnpackedInputStream(HttpEntity entity)
			throws HTTPException {
		try {
			InputStream is = entity.getContent();
			Header contentEncodingHeader = entity.getContentEncoding();
			if (contentEncodingHeader != null) {
				String contentEncoding = contentEncodingHeader.getValue();
				L.d("Content-Encoding: " + contentEncoding);
				if (isNotEmpty(contentEncoding)) {
					contentEncoding = contentEncoding.toLowerCase();
					if (contentEncoding.contains("gzip")) {
						return new GZIPInputStream(is);
					} else if (contentEncoding.contains("deflate")) {
						return new InflaterInputStream(is);
					}
				}
			}
			return is;
		} catch (Exception e) {
			throw new HTTPException(e);
		}
	}

	private static int getResponseCodeOrThrow(HttpResponse resp)
			throws HTTPException {
		int respCode = resp.getStatusLine().getStatusCode();
		if (respCode >= 400) {
			String respBody = getResponseBodyAndConsume(resp);
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

	private static String getResponseBodyAndConsume(HttpResponse resp)
			throws HTTPException {
		HttpEntity entity = resp.getEntity();
		InputStream is = getUnpackedInputStream(entity);
		try {
			return IOUtils.readAndCloseInputStream(is);
		} catch (Exception e) {
			throw new HTTPException(e);
		} finally {
			try {
				resp.getEntity().consumeContent();
			} catch (Exception e) {
				L.d(e);
			}
		}
	}

}
