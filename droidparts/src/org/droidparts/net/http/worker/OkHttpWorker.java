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

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import org.droidparts.net.http.CookieJar;
import org.droidparts.net.http.UserAgent;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;

public class OkHttpWorker extends HttpURLConnectionWorker {

	private final OkHttpClient okHttp;

	public OkHttpWorker(Context ctx) {
		this(ctx, UserAgent.getDefault());
	}

	public OkHttpWorker(Context ctx, String userAgent) {
		super(ctx, userAgent);
		okHttp = new OkHttpClient();
	}

	@Override
	protected void enableCache(Context ctx) {
		// pass
	}

	@Override
	public void setProxy(Proxy proxy) {
		okHttp.setProxy(proxy);
	}

	@Override
	public void setCookieJar(CookieJar cookieJar) {
		okHttp.setCookieHandler(cookieJar);
	}

	@Override
	protected HttpURLConnection openConnection(URL url) throws Exception {
		return okHttp.open(url);
	}

}
