/**
 * Copyright 2017 Alex Yanchenko
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

import java.util.HashMap;

import android.util.Base64;

import org.droidparts.net.http.CookieJar;

import static android.util.Base64.NO_WRAP;

public abstract class HTTPWorker {

	protected static final int SOCKET_OPERATION_TIMEOUT = 60 * 1000;

	public final HashMap<String, String> headers = new HashMap<String, String>();
	protected boolean followRedirects = true;

	public final void authenticateBasic(String user, String password) {
		String val = null;
		if (user != null && password != null) {
			String userPass = Base64.encodeToString((user + ":" + password).getBytes(), NO_WRAP);
			val = "Basic " + userPass;
		}
		setHeader("Authorization", val);
	}

	public final void setHeader(String key, String val) {
		if (val != null) {
			headers.put(key, val);
		} else {
			headers.remove(key);
		}
	}

	public void setFollowRedirects(boolean follow) {
		this.followRedirects = follow;
	}

	public abstract void setCookieJar(CookieJar cookieJar);

}
