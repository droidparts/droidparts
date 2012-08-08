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

import java.io.IOException;
import java.io.InputStream;
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

	// TODO tweak to resemble
	// http://developer.android.com/reference/android/net/http/AndroidHttpClient.html

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
	protected void setProxy(String protocol, String host, int port,
			String user, String password) {
		HttpHost proxyHost = new HttpHost(host, port, protocol);
		httpClient.getParams().setParameter(DEFAULT_PROXY, proxyHost);
		if (!isEmpty(user) && !isEmpty(password)) {
			AuthScope authScope = new AuthScope(host, port);
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
					user, password);
			httpClient.getCredentialsProvider().setCredentials(authScope,
					credentials);
		}
	}

	@Override
	public void authenticateBasic(String authUser, String authPassword) {
		AuthScope authScope = new AuthScope(ANY_HOST, ANY_PORT);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
				authUser, authPassword);
		httpClient.getCredentialsProvider().setCredentials(authScope,
				credentials);
	}

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
				// TODO read response body
				throw new HTTPException(respCode, null);
			}
			return resp;
		} catch (IOException e) {
			throw new HTTPException(e);
		}
	}

	//

	public static String getResponseBody(HttpResponse resp)
			throws HTTPException {
		HttpEntity entity = resp.getEntity();
		InputStream is = getUnpackedInputStream(entity);
		try {
			return IOUtils.readAndCloseInputStream(is);
		} catch (IOException e) {
			throw new HTTPException(e);
		}
	}

	public static InputStream getUnpackedInputStream(HttpEntity entity)
			throws HTTPException {
		try {
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
		} catch (Exception e) {
			throw new HTTPException(e);
		}
	}

	public static void consumeResponse(HttpResponse resp) {
		try {
			resp.getEntity().consumeContent();
		} catch (IOException e) {
			L.d(e);
		}
	}

}
