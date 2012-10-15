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
package org.droidparts.http.wrapper;

import static org.droidparts.util.Strings.isEmpty;

import java.io.File;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;

import org.droidparts.http.HTTPException;
import org.droidparts.util.L;
import org.droidparts.util.io.IOUtils;

import android.content.Context;

public class HttpURLConnectionWrapper extends HttpClientWrapper {

	public static final String GET = "GET";
	public static final String PUT = "PUT";
	public static final String POST = "POST";
	public static final String DELETE = "DELETE";

	private Proxy proxy;
	private Authenticator auth;

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

	public HttpURLConnectionWrapper(String userAgent) {
		super(userAgent);
	}

	@Override
	protected void setProxy(String protocol, String host, int port,
			String user, String password) {
		proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
		if (!isEmpty(user) && !isEmpty(password)) {
			// FIXME
			authenticateBasic(user, password);
		}
	}

	@Override
	public void authenticateBasic(final String username, final String password) {
		auth = new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username,
						password.toCharArray());
			}
		};
	}

	public HttpURLConnection getConnection(String urlStr, String requestMethod)
			throws HTTPException {
		if (auth != null) {
			Authenticator.setDefault(auth);
		}
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn;
			if (proxy != null) {
				conn = (HttpURLConnection) url.openConnection(proxy);
			} else {
				conn = (HttpURLConnection) url.openConnection();
			}
			for (String name : headers.keySet()) {
				conn.setRequestProperty(name, headers.get(name));
			}
			conn.setRequestMethod(requestMethod);
			if (userAgent != null) {
				conn.setRequestProperty("http.agent", userAgent);
			}
			if (PUT.equals(requestMethod) || POST.equals(requestMethod)) {
				conn.setDoOutput(true);
			}
			return conn;
		} catch (Exception e) {
			throw new HTTPException(e);
		}
	}

	public static void connectAndCheckResponseCode(HttpURLConnection conn)
			throws HTTPException {
		try {
			conn.connect();
			int respCode = conn.getResponseCode();
			if (respCode >= 400) {
				String respBody = IOUtils.readAndCloseInputStream(conn
						.getInputStream());
				conn.disconnect();
				throw new HTTPException(respCode, respBody);
			}
		} catch (Exception e) {
			if (e instanceof HTTPException) {
				throw (HTTPException) e;
			} else {
				throw new HTTPException(e);
			}
		}
	}

	public static String getResponseBodyAndDisconnect(HttpURLConnection conn)
			throws HTTPException {
		try {
			return IOUtils.readAndCloseInputStream(conn.getInputStream());
		} catch (Exception e) {
			throw new HTTPException(e);
		} finally {
			conn.disconnect();
			Authenticator.setDefault(null);
		}
	}

	public static InputStream getUnpackedInputStream(HttpURLConnection conn)
			throws HTTPException {
		try {
			return conn.getInputStream();
		} catch (Exception e) {
			throw new HTTPException(e);
		}
	}

}
