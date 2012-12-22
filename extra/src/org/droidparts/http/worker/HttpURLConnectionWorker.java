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

import static org.droidparts.contract.Constants.UTF8;
import static org.droidparts.util.io.IOUtils.readAndCloseInputStream;
import static org.droidparts.util.io.IOUtils.silentlyClose;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;

import org.droidparts.http.CookieJar;
import org.droidparts.http.HTTPException;
import org.droidparts.http.HTTPResponse;
import org.droidparts.util.L;

import android.content.Context;
import android.util.Pair;

public class HttpURLConnectionWorker extends HTTPWorker {

	public static final String GET = "GET";
	public static final String PUT = "PUT";
	public static final String POST = "POST";
	public static final String DELETE = "DELETE";

	private Proxy proxy;
	private PasswordAuthentication passAuth;

	// ICS+
	public static void setHttpResponseCacheEnabled(Context ctx, boolean enabled) {
		File cacheDir = new File(ctx.getCacheDir(), "http");
		long cacheSize = 10 * 1024 * 1024; // 10 MiB
		try {
			Class<?> cls = Class.forName("android.net.http.HttpResponseCache");
			if (enabled) {
				cls.getMethod("install", File.class, long.class).invoke(null,
						cacheDir, cacheSize);
			} else {
				Object instance = cls.getMethod("getInstalled").invoke(null);
				if (instance != null) {
					cls.getMethod("delete").invoke(instance);
				}
			}
		} catch (Exception e) {
			L.e(e);
		}
	}

	public HttpURLConnectionWorker(String userAgent) {
		super(userAgent);
	}

	@Override
	public void setCookieJar(CookieJar cookieJar) {
		CookieHandler.setDefault(cookieJar);
	}

	@Override
	public void authenticateBasic(String user, String password) {
		passAuth = new PasswordAuthentication(user, password.toCharArray());
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	public HttpURLConnection getConnection(String urlStr, String requestMethod)
			throws HTTPException {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn;
			if (proxy != null) {
				conn = (HttpURLConnection) url.openConnection(proxy);
			} else {
				conn = (HttpURLConnection) url.openConnection();
			}
			for (String key : headers.keySet()) {
				for (String val : headers.get(key)) {
					conn.addRequestProperty(key, val);
				}
			}
			conn.setRequestProperty("http.agent", userAgent);
			if (passAuth != null) {
				Authenticator.setDefault(new FixedAuthenticator(passAuth));
			}
			conn.setRequestMethod(requestMethod);
			if (PUT.equals(requestMethod) || POST.equals(requestMethod)) {
				conn.setDoOutput(true);
			}
			return conn;
		} catch (Exception e) {
			throw new HTTPException(e);
		}
	}

	public static void postOrPut(HttpURLConnection conn, String contentType,
			String data) throws HTTPException {
		conn.setRequestProperty("Accept-Charset", UTF8);
		conn.setRequestProperty("Content-Type", contentType);
		OutputStream os = null;
		try {
			os = conn.getOutputStream();
			os.write(data.getBytes(UTF8));
		} catch (Exception e) {
			throw new HTTPException(e);
		} finally {
			silentlyClose(os);
		}
	}

	public static HTTPResponse getReponse(HttpURLConnection conn)
			throws HTTPException {
		HTTPResponse response = new HTTPResponse();
		response.code = connectAndGetResponseCodeOrThrow(conn);
		response.headers = conn.getHeaderFields();
		response.body = getResponseBodyAndDisconnect(conn);
		return response;
	}

	public Pair<Integer, BufferedInputStream> getInputStream(String uri)
			throws HTTPException {
		HttpURLConnection conn = getConnection(uri, GET);
		HttpURLConnectionWorker.connectAndGetResponseCodeOrThrow(conn);
		int contentLength = conn.getContentLength();
		ConsumingInputStream cis = new ConsumingInputStream(
				getUnpackedInputStream(conn), conn);
		return new Pair<Integer, BufferedInputStream>(contentLength, cis);
	}

	private static int connectAndGetResponseCodeOrThrow(HttpURLConnection conn)
			throws HTTPException {
		try {
			conn.connect();
			int respCode = conn.getResponseCode();
			if (isErrorResponseCode(respCode)) {
				InputStream is = (conn.getErrorStream() != null) ? conn
						.getErrorStream() : conn.getInputStream();
				String respBody = readAndCloseInputStream(is);
				conn.disconnect();
				throw new HTTPException(respCode, respBody);
			}
			return respCode;
		} catch (Exception e) {
			if (e instanceof HTTPException) {
				throw (HTTPException) e;
			} else {
				throw new HTTPException(e);
			}
		}
	}

	private static InputStream getUnpackedInputStream(HttpURLConnection conn)
			throws HTTPException {
		try {
			return conn.getInputStream();
		} catch (Exception e) {
			throw new HTTPException(e);
		}
	}

	private static String getResponseBodyAndDisconnect(HttpURLConnection conn)
			throws HTTPException {
		try {
			return readAndCloseInputStream(conn.getInputStream());
		} catch (Exception e) {
			throw new HTTPException(e);
		} finally {
			conn.disconnect();
		}
	}

	private static class FixedAuthenticator extends Authenticator {

		private PasswordAuthentication passAuth;

		public FixedAuthenticator(PasswordAuthentication passAuth) {
			this.passAuth = passAuth;
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			try {
				return passAuth;
			} finally {
				passAuth = null;
			}
		}

	}

}
