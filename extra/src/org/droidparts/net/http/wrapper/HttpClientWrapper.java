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

import java.util.HashMap;

public abstract class HttpClientWrapper {

	protected static final int SOCKET_OPERATION_TIMEOUT = 60 * 1000;

	protected final HashMap<String, String> headers = new HashMap<String, String>();
	protected final String userAgent;

	public HttpClientWrapper(String userAgent) {
		this.userAgent = userAgent;
	}

	public void setHeaders(HashMap<String, String> headers) {
		this.headers.clear();
		this.headers.putAll(headers);
	}

	public abstract void setProxy(String proxy, String username, String password);

	public abstract void authenticateBasic(String username, String password);
}
