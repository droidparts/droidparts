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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.os.Build;

public abstract class HTTPWorker<T> {

	public static boolean useHttpURLConnection() {
		// http://android-developers.blogspot.com/2011/09/androids-http-clients.html
		return Build.VERSION.SDK_INT >= 10;
	}

	protected static final int SOCKET_OPERATION_TIMEOUT = 60 * 1000;

	protected final HashMap<String, ArrayList<String>> headers = new HashMap<String, ArrayList<String>>();
	protected final String userAgent;

	public HTTPWorker(String userAgent) {
		this.userAgent = userAgent;
	}

	public void addHeader(String key, String val) {
		if (!headers.containsKey(key)) {
			headers.put(key, new ArrayList<String>());
		}
		headers.get(key).add(val);
	}

	public abstract void authenticateBasic(String user, String password);

	public final void setProxy(String proxy, String user, String password) {
		URL proxyUrl;
		try {
			proxyUrl = new URL(proxy);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		setProxy(proxyUrl.getProtocol(), proxyUrl.getHost(),
				proxyUrl.getPort(), user, password);
	}

	protected abstract void setProxy(String protocol, String host, int port,
			String user, String password);

}
