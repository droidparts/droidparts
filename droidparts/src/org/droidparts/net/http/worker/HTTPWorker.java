/**
 * Copyright 2013 Alex Yanchenko
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

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.auth.AuthScope;
import org.droidparts.net.http.CookieJar;

public abstract class HTTPWorker {

	public static void throwIfNetworkOnMainThreadException(Exception e) {
		if (e.getClass().getName()
				.equals("android.os.NetworkOnMainThreadException")) {
			throw (RuntimeException) e;
		}
	}

	protected static final int SOCKET_OPERATION_TIMEOUT = 60 * 1000;

	protected final HashMap<String, ArrayList<String>> headers = new HashMap<String, ArrayList<String>>();
	protected final String userAgent;

	public HTTPWorker(String userAgent) {
		this.userAgent = userAgent;
	}

	public final void putHeader(String key, String val) {
		if (val != null) {
			if (!headers.containsKey(key)) {
				headers.put(key, new ArrayList<String>());
			}
			headers.get(key).add(val);
		} else {
			headers.remove(key);
		}
	}

	public abstract void setCookieJar(CookieJar cookieJar);

	public abstract void authenticateBasic(String user, String password,
			AuthScope scope);

	protected static final boolean isErrorResponseCode(int responseCode) {
		return responseCode >= 400;
	}

}
