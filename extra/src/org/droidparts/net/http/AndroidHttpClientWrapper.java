/**
 * Copyright 2011 Alex Yanchenko
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
package org.droidparts.net.http;

import java.lang.reflect.Method;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.droidparts.util.L;

// API 8
public class AndroidHttpClientWrapper {

	private final Object androidHttpClient;
	private final Method close;
	private final Method execute;

	public AndroidHttpClientWrapper(String userAgent) {
		try {
			Class<?> cl = Class.forName("android.net.http.AndroidHttpClient");
			Method m = cl.getDeclaredMethod("newInstance", String.class);
			androidHttpClient = m.invoke(null, userAgent);
			execute = cl.getDeclaredMethod("execute", HttpUriRequest.class);
			close = cl.getDeclaredMethod("close");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public HttpResponse execute(HttpUriRequest request) {
		try {
			Object result = execute.invoke(androidHttpClient, request);
			return (HttpResponse) result;
		} catch (Exception e) {
			L.d(e);
			return null;
		}
	}

	public void close() {
		try {
			close.invoke(androidHttpClient);
		} catch (Exception e) {
			L.d(e);
		}
	}

}
