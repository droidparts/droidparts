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
package org.droidparts.http;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;

public class RESTClient2 extends RESTClient {

	private static final String TEXT_PLAIN = "text/plain";
	private static final String APPLICATION_JSON = "application/json";
	private static final String APPLICATION_FORM_DATA = "application/x-www-form-urlencoded";

	public RESTClient2(Context ctx, String userAgent) {
		super(ctx, userAgent);
	}

	public JSONObject getJSONObject(String uri) throws HTTPException {
		String resp = get(uri).body;
		try {
			return new JSONObject(resp);
		} catch (JSONException e) {
			throw new HTTPException(e);
		}
	}

	public JSONArray getJSONArray(String uri) throws HTTPException {
		String resp = get(uri).body;
		try {
			return new JSONArray(resp);
		} catch (JSONException e) {
			throw new HTTPException(e);
		}
	}

	public HTTPResponse put(String uri, String data) throws HTTPException {
		return put(uri, TEXT_PLAIN, data);
	}

	public HTTPResponse put(String uri, JSONObject data) throws HTTPException {
		return put(uri, APPLICATION_JSON, data.toString());
	}

	public HTTPResponse put(String uri, JSONArray data) throws HTTPException {
		return put(uri, APPLICATION_JSON, data.toString());
	}

	public HTTPResponse post(String uri, String data) throws HTTPException {
		return post(uri, TEXT_PLAIN, data);
	}

	public HTTPResponse post(String uri, JSONObject data) throws HTTPException {
		return post(uri, APPLICATION_JSON, data.toString());
	}

	public HTTPResponse post(String uri, JSONArray data) throws HTTPException {
		return post(uri, APPLICATION_JSON, data.toString());
	}

	public HTTPResponse post(String uri, Map<String, String> formData)
			throws HTTPException {
		Uri.Builder builder = new Uri.Builder();
		for (String key : formData.keySet()) {
			String val = formData.get(key);
			builder.appendQueryParameter(key, (val != null) ? val : "");
		}
		String query = builder.build().getQuery();
		return post(uri, APPLICATION_FORM_DATA, query);
	}

}
