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

import static org.droidparts.contract.Constants.UTF8;
import static org.droidparts.contract.HTTP.ContentType.MULTIPART;
import static org.droidparts.contract.HTTP.Header.ACCEPT_CHARSET;
import static org.droidparts.contract.HTTP.Header.ACCEPT_ENCODING;
import static org.droidparts.contract.HTTP.Header.CACHE_CONTROL;
import static org.droidparts.contract.HTTP.Header.CONNECTION;
import static org.droidparts.contract.HTTP.Header.CONTENT_TYPE;
import static org.droidparts.contract.HTTP.Header.KEEP_ALIVE;
import static org.droidparts.contract.HTTP.Header.NO_CACHE;
import static org.droidparts.contract.HTTP.Header.USER_AGENT;
import static org.droidparts.util.IOUtils.readToByteArray;
import static org.droidparts.util.IOUtils.silentlyClose;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import org.droidparts.contract.HTTP.Method;
import org.droidparts.net.http.CookieJar;
import org.droidparts.net.http.HTTPException;
import org.droidparts.net.http.HTTPResponse;
import org.droidparts.net.http.worker.wrapper.HttpResponseCacheWrapper;
import org.droidparts.util.L;

import android.content.Context;
import android.os.Build;

public class HttpURLConnectionWorker extends HTTPWorker {

	private static final String CRLF = "\r\n";
	private static final String TWO_HYPHENS = "--";
	private static final String BOUNDARY = "*****";

	private final String userAgent;

	private Proxy proxy;

	public HttpURLConnectionWorker(Context ctx, String userAgent) {
		this.userAgent = userAgent;
		enableCache(ctx);
	}

	protected void enableCache(Context ctx) {
		if (Build.VERSION.SDK_INT >= 14) {
			try {
				HttpResponseCacheWrapper.install(ctx);
			} catch (IOException e) {
				L.w(e);
			}
		}
	}

	@Override
	public void setCookieJar(CookieJar cookieJar) {
		CookieHandler.setDefault(cookieJar);
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	public HttpURLConnection getConnection(String urlStr, String requestMethod)
			throws HTTPException {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = openConnection(url);
			for (String key : headers.keySet()) {
				conn.addRequestProperty(key, headers.get(key));
			}
			if (userAgent != null) {
				conn.setRequestProperty(USER_AGENT, userAgent);
			}
			conn.setRequestProperty(ACCEPT_ENCODING, "gzip,deflate");
			conn.setRequestMethod(requestMethod);
			conn.setInstanceFollowRedirects(followRedirects);
			if (Method.PUT.equals(requestMethod)
					|| Method.POST.equals(requestMethod)) {
				conn.setDoOutput(true);
			}
			return conn;
		} catch (Exception e) {
			throwIfNetworkOnMainThreadException(e);
			throw new HTTPException(e);
		}
	}

	// Override to provide a different implementation.
	protected HttpURLConnection openConnection(URL url) throws Exception {
		if (proxy != null) {
			return (HttpURLConnection) url.openConnection(proxy);
		} else {
			return (HttpURLConnection) url.openConnection();
		}
	}

	public static void postOrPut(HttpURLConnection conn, String contentType,
			String data) throws HTTPException {
		conn.setRequestProperty(ACCEPT_CHARSET, UTF8);
		conn.setRequestProperty(CONTENT_TYPE, contentType);
		OutputStream os = null;
		try {
			os = conn.getOutputStream();
			os.write(data.getBytes(UTF8));
		} catch (Exception e) {
			throwIfNetworkOnMainThreadException(e);
			throw new HTTPException(e);
		} finally {
			silentlyClose(os);
		}
	}

	public static void postMultipart(HttpURLConnection conn, String name,
			String contentType, File file) throws HTTPException {
		conn.setDoOutput(true);
		conn.setRequestProperty(CACHE_CONTROL, NO_CACHE);
		conn.setRequestProperty(CONNECTION, KEEP_ALIVE);
		conn.setRequestProperty(CONTENT_TYPE, MULTIPART + ";boundary="
				+ BOUNDARY);
		DataOutputStream request = null;
		try {
			request = new DataOutputStream(conn.getOutputStream());

			request.writeBytes(TWO_HYPHENS + BOUNDARY + CRLF);
			request.writeBytes("Content-Disposition: form-data; name=\"" + name
					+ "\";filename=\"" + file.getName() + "\"" + CRLF);
			if (contentType != null) {
				request.writeBytes("Content-Type: " + contentType + CRLF);
			}
			request.writeBytes(CRLF);
			//
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				request.write(readToByteArray(fis));
			} finally {
				silentlyClose(fis);
			}
			//
			request.writeBytes(CRLF);
			request.writeBytes(TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + CRLF);

			request.flush();
		} catch (Exception e) {
			throwIfNetworkOnMainThreadException(e);
			throw new HTTPException(e);
		} finally {
			silentlyClose(request);
		}
	}

	public static HTTPResponse getResponse(HttpURLConnection conn, boolean body)
			throws HTTPException {
		HTTPResponse response = new HTTPResponse();
		response.code = connectAndGetResponseCodeOrThrow(conn);
		response.headers = conn.getHeaderFields();
		HTTPInputStream is = HTTPInputStream.getInstance(conn, false);
		if (body) {
			response.body = is.readAndClose();
		} else {
			response.inputStream = is;
		}
		return response;
	}

	private static int connectAndGetResponseCodeOrThrow(HttpURLConnection conn)
			throws HTTPException {
		try {
			conn.connect();
			int respCode = conn.getResponseCode();
			if (isErrorResponseCode(respCode)) {
				HTTPInputStream is = HTTPInputStream.getInstance(conn,
						(conn.getErrorStream() != null));
				throw new HTTPException(respCode, is.readAndClose());
			}
			return respCode;
		} catch (HTTPException e) {
			throw e;
		} catch (Exception e) {
			throwIfNetworkOnMainThreadException(e);
			throw new HTTPException(e);
		}

	}

}