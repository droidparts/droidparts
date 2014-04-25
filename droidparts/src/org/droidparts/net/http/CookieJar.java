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
package org.droidparts.net.http;

import static android.content.Context.MODE_PRIVATE;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableList;
import static org.droidparts.util.Strings.join;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SM;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.message.BasicHeader;
import org.droidparts.util.L;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class CookieJar extends CookieHandler implements CookieStore {

	private final CookieSpec cookieSpec;
	private final SharedPreferences prefs;

	private boolean persistCookies;

	public CookieJar(Context ctx) {
		cookieSpec = new BrowserCompatSpec();
		prefs = ctx.getSharedPreferences("droidparts_restclient_cookies",
				MODE_PRIVATE);
	}

	public void setPersistent(boolean persistent) {
		persistCookies = persistent;
		if (persistCookies) {
			cookies.clear();
			restoreCookies();
		}
	}

	// HttpURLConnection

	@Override
	public Map<String, List<String>> get(URI uri,
			Map<String, List<String>> requestHeaders) throws IOException {
		clearExpired(new Date());
		ArrayList<String> cookies = new ArrayList<String>();
		for (Cookie cookie : getCookies(uri)) {
			cookies.add(cookie.getName() + "=" + cookie.getValue());
		}
		return singletonMap(SM.COOKIE, singletonList(join(cookies, SEP)));
	}

	@Override
	public void put(URI uri, Map<String, List<String>> responseHeaders)
			throws IOException {
		for (String key : responseHeaders.keySet()) {
			if (SM.SET_COOKIE.equalsIgnoreCase(key)
					|| SM.SET_COOKIE2.equalsIgnoreCase(key)) {
				List<Cookie> cookies = parseCookies(uri,
						responseHeaders.get(key));
				for (Cookie c : cookies) {
					addCookie(c);
				}
				return;
			}
		}
	}

	// HttpClient

	@Override
	public void addCookie(Cookie cookie) {
		L.d("Got a cookie: %s.", cookie);
		for (Iterator<Cookie> it = cookies.iterator(); it.hasNext();) {
			Cookie c = it.next();
			if (isEqual(cookie, c)) {
				it.remove();
			}
		}
		if (!cookie.isExpired(new Date())) {
			cookies.add(cookie);
		}
		if (persistCookies) {
			persistCookies();
		}
	}

	@Override
	public void clear() {
		cookies.clear();
		if (persistCookies) {
			persistCookies();
		}
	}

	@Override
	public boolean clearExpired(Date date) {
		boolean purged = false;
		for (Iterator<Cookie> it = cookies.iterator(); it.hasNext();) {
			Cookie cookie = it.next();
			if (cookie.isExpired(date)) {
				it.remove();
				purged = true;
			}
		}
		if (persistCookies && purged) {
			persistCookies();
		}
		return purged;
	}

	@Override
	public List<Cookie> getCookies() {
		L.d("Cookie count: %d.", cookies.size());
		return unmodifiableList(cookies);
	}

	// Custom

	private final CopyOnWriteArrayList<Cookie> cookies = new CopyOnWriteArrayList<Cookie>();

	private List<Cookie> parseCookies(URI uri, List<String> cookieHeaders) {
		ArrayList<Cookie> cookies = new ArrayList<Cookie>();
		int port = (uri.getPort() < 0) ? 80 : uri.getPort();
		boolean secure = "https".equals(uri.getScheme());
		CookieOrigin origin = new CookieOrigin(uri.getHost(), port,
				uri.getPath(), secure);
		for (String cookieHeader : cookieHeaders) {
			BasicHeader header = new BasicHeader(SM.SET_COOKIE, cookieHeader);
			try {
				cookies.addAll(cookieSpec.parse(header, origin));
			} catch (MalformedCookieException e) {
				L.d(e);
			}
		}
		return cookies;
	}

	private Collection<Cookie> getCookies(URI uri) {
		HashMap<String, Cookie> map = new HashMap<String, Cookie>();
		for (Cookie cookie : getCookies()) {
			boolean suitable = uri.getHost().equals(cookie.getDomain())
					&& uri.getPath().startsWith(cookie.getPath());
			if (suitable) {
				boolean put = true;
				if (map.containsKey(cookie.getName())) {
					Cookie otherCookie = map.get(cookie.getName());
					boolean betterMatchingPath = cookie.getPath().length() > otherCookie
							.getPath().length();
					put = betterMatchingPath;
				}
				if (put) {
					map.put(cookie.getName(), cookie);
				}
			}
		}
		return map.values();
	}

	private void persistCookies() {
		Editor editor = prefs.edit();
		editor.clear();
		for (int i = 0; i < cookies.size(); i++) {
			editor.putString(String.valueOf(i), toString(cookies.get(i)));
		}
		editor.commit();
	}

	private void restoreCookies() {
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			String str = prefs.getString(String.valueOf(i), null);
			if (str == null) {
				return;
			} else {
				cookies.add(fromString(str));
			}
		}
	}

	private static String toString(Cookie cookie) {
		StringBuilder sb = new StringBuilder();
		sb.append(cookie.getName());
		sb.append(SEP);
		sb.append(cookie.getValue());
		sb.append(SEP);
		sb.append(cookie.getDomain());
		sb.append(SEP);
		sb.append(cookie.getPath());
		Date expiryDate = cookie.getExpiryDate();
		if (expiryDate != null) {
			sb.append(SEP);
			sb.append(expiryDate.getTime());
		}
		return sb.toString();
	}

	private static Cookie fromString(String str) {
		String[] parts = str.split(SEP);
		BasicClientCookie cookie = new BasicClientCookie(parts[0], parts[1]);
		cookie.setDomain(parts[2]);
		cookie.setPath(parts[3]);
		if (parts.length == 5) {
			cookie.setExpiryDate(new Date(Long.valueOf(parts[4])));
		}
		return cookie;
	}

	private static final String SEP = ";";

	private static boolean isEqual(Cookie first, Cookie second) {
		boolean equal = first.getName().equals(second.getName())
				&& first.getDomain().equals(second.getDomain())
				&& first.getPath().equals(second.getPath());
		return equal;
	}

}
