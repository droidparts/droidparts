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
package org.droidparts.net.http.wrapper;

import static android.text.TextUtils.isEmpty;
import static org.apache.http.auth.AuthScope.ANY_HOST;
import static org.apache.http.auth.AuthScope.ANY_PORT;
import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY;
import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.util.io.IOUtils.silentlyClose;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.droidparts.net.http.HTTPException;
import org.droidparts.util.L;
import org.droidparts.util.io.IOUtils;

// For API < 10
public class DefaultHttpClientWrapper extends HttpClientWrapper {

	private final DefaultHttpClient httpClient;

	public DefaultHttpClientWrapper(String userAgent) {
		super(userAgent);
		httpClient = new DefaultHttpClient();
		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params,
				SOCKET_OPERATION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, SOCKET_OPERATION_TIMEOUT);
		HttpConnectionParams.setSocketBufferSize(params, BUFFER_SIZE);
		if (userAgent != null) {
			HttpProtocolParams.setUserAgent(params, userAgent);
		}
	}

	@Override
	public void setProxy(String proxyUrl, String proxyUser, String proxyPassword) {
		URL proxy;
		try {
			proxy = new URL(proxyUrl);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		HttpHost proxyHost = new HttpHost(proxy.getHost(), proxy.getPort(),
				proxy.getProtocol());
		httpClient.getParams().setParameter(DEFAULT_PROXY, proxyHost);
		if (!isEmpty(proxyUser) && !isEmpty(proxyPassword)) {
			AuthScope authScope = new AuthScope(proxy.getHost(),
					proxy.getPort());
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
					proxyUser, proxyPassword);
			httpClient.getCredentialsProvider().setCredentials(authScope,
					credentials);
		}
		// TODO tweak to resemble
		// http://developer.android.com/reference/android/net/http/AndroidHttpClient.html
	}

	@Override
	public void authenticateBasic(String authUser, String authPassword) {
		AuthScope authScope = new AuthScope(ANY_HOST, ANY_PORT);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
				authUser, authPassword);
		httpClient.getCredentialsProvider().setCredentials(authScope,
				credentials);
	}

	//

	public HttpResponse getResponse(HttpUriRequest req) throws HTTPException {
		for (String name : headers.keySet()) {
			req.setHeader(name, headers.get(name));
		}
		req.setHeader("Accept-Encoding", "gzip,deflate");
		try {
			HttpResponse resp = httpClient.execute(req);
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

	public String getResponseBody(HttpResponse resp) throws HTTPException {
		InputStream is = null;
		HttpEntity entity = resp.getEntity();
		try {
			is = getUnpackedInputStream(entity);
			return IOUtils.readAndCloseInputStream(is);
		} catch (IOException e) {
			silentlyClose(is);
			throw new HTTPException(e);
		}
	}

	public InputStream getUnpackedInputStream(HttpEntity entity)
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

	public void consumeResponse(HttpResponse resp) {
		try {
			resp.getEntity().consumeContent();
		} catch (IOException e) {
			L.d(e);
		}
	}

	public static class EntityInputStream extends BufferedInputStream {

		private final HttpEntity entity;

		public EntityInputStream(InputStream in, HttpEntity entity) {
			super(in, BUFFER_SIZE);
			this.entity = entity;
		}

		@Override
		public void close() throws IOException {
			super.close();
			entity.consumeContent();
		}

	}

}
