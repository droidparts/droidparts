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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.droidparts.net.http.HTTPException;
import org.droidparts.util.L;
import org.droidparts.util.io.IOUtils;

import android.content.Context;

public class HttpURLConnectionWrapper extends HttpClientWrapper {

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
	public void setProxy(String proxy, String username, String password) {
		// TODO

	}

	@Override
	public void authenticateBasic(String username, String password) {
		// TODO
	}

	public HttpURLConnection getConnectedHttpURLConnection(String urlStr,
			String requestMethod) throws HTTPException {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			for (String name : headers.keySet()) {
				conn.setRequestProperty(name, headers.get(name));
			}
			conn.setRequestMethod(requestMethod);
			if (userAgent != null) {
				conn.setRequestProperty("http.agent", userAgent);
			}
			conn.setDoOutput(true);
			conn.connect();
			int respCode = conn.getResponseCode();
			if (respCode >= 400) {
				conn.disconnect();
				throw new HTTPException(respCode);
			}
			return conn;
		} catch (IOException e) {
			throw new HTTPException(e);
		}
	}

	public String getResponseBodyAndDisconnect(HttpURLConnection conn)
			throws HTTPException {
		try {
			return IOUtils.readAndCloseInputStream(conn.getInputStream());
		} catch (IOException e) {
			throw new HTTPException(e);
		} finally {
			conn.disconnect();
		}
	}

}
