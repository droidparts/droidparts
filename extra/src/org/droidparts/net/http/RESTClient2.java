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
package org.droidparts.net.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RESTClient2 extends RESTClient {

	public RESTClient2(String userAgent) {
		super(userAgent);
	}

	public JSONObject getJSONObject(String uri) throws HTTPException {
		String resp = get(uri);
		try {
			return new JSONObject(resp);
		} catch (JSONException e) {
			throw new HTTPException(e);
		}
	}

	public JSONArray getJSONArray(String uri) throws HTTPException {
		String resp = get(uri);
		try {
			return new JSONArray(resp);
		} catch (JSONException e) {
			throw new HTTPException(e);
		}
	}

	public String put(String uri, String data) throws HTTPException {
		return put(uri, "text/plain", data);
	}

	public String put(String uri, JSONObject data) throws HTTPException {
		return put(uri, "application/json", data.toString());
	}

	public String put(String uri, JSONArray data) throws HTTPException {
		return put(uri, "application/json", data.toString());
	}

	public String post(String uri, String data) throws HTTPException {
		return post(uri, "text/plain", data);
	}

	public String post(String uri, JSONObject data) throws HTTPException {
		return post(uri, "application/json", data.toString());
	}

	public String post(String uri, JSONArray data) throws HTTPException {
		return post(uri, "application/json", data.toString());
	}

}
