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
package org.droidparts.net.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import org.droidparts.contract.HTTP.ContentType;
import org.droidparts.net.http.worker.HTTPWorker;

public class RESTClient2 extends RESTClient {

	public RESTClient2(Context ctx) {
		super(ctx);
	}

	public RESTClient2(Context ctx, String userAgent) {
		super(ctx, userAgent);
	}

	public RESTClient2(Context ctx, HTTPWorker worker) {
		super(ctx, worker);
	}

	@Override
	public HTTPResponse2 get(String uri) throws HTTPException {
		return new HTTPResponse2(super.get(uri));
	}

	@Override
	public HTTPResponse2 get(String uri, long ifModifiedSince, String etag, boolean body) throws HTTPException {
		return new HTTPResponse2(super.get(uri, ifModifiedSince, etag, body));
	}

	@Override
	public HTTPResponse2 post(String uri, String contentType, String data) throws HTTPException {
		return new HTTPResponse2(super.post(uri, contentType, data));
	}

	@Override
	public HTTPResponse2 postMultipart(String uri, String name, String contentType, String fileName, InputStream is)
			throws HTTPException {
		return new HTTPResponse2(super.postMultipart(uri, name, contentType, fileName, is));
	}

	@Override
	public HTTPResponse2 put(String uri, String contentType, String data) throws HTTPException {
		return new HTTPResponse2(super.put(uri, contentType, data));
	}

	@Override
	public HTTPResponse2 delete(String uri) throws HTTPException {
		return new HTTPResponse2(super.delete(uri));
	}

	public HTTPResponse2 put(String uri, String data) throws HTTPException {
		return put(uri, ContentType.TEXT_PLAIN, data);
	}

	public HTTPResponse2 put(String uri, JSONObject data) throws HTTPException {
		return put(uri, ContentType.APPLICATION_JSON, data.toString());
	}

	public HTTPResponse2 put(String uri, JSONArray data) throws HTTPException {
		return put(uri, ContentType.APPLICATION_JSON, data.toString());
	}

	public HTTPResponse2 post(String uri, String data) throws HTTPException {
		return post(uri, ContentType.TEXT_PLAIN, data);
	}

	public HTTPResponse2 post(String uri, JSONObject data) throws HTTPException {
		return post(uri, ContentType.APPLICATION_JSON, data.toString());
	}

	public HTTPResponse2 post(String uri, JSONArray data) throws HTTPException {
		return post(uri, ContentType.APPLICATION_JSON, data.toString());
	}

	public HTTPResponse2 post(String uri, Map<String, String> formData) throws HTTPException {
		Uri.Builder builder = new Uri.Builder();
		for (String key : formData.keySet()) {
			String val = formData.get(key);
			builder.appendQueryParameter(key, (val != null) ? val : "");
		}
		String query = builder.build().getQuery();
		return post(uri, ContentType.APPLICATION_FORM_DATA, query);
	}

	public HTTPResponse2 postMultipart(String uri, String name, File file) throws HTTPException {
		return postMultipart(uri, name, null, file);
	}

	public HTTPResponse2 postMultipart(String uri, String name, String contentType, File file) throws HTTPException {
		try {
			return postMultipart(uri, name, contentType, file.getName(), new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new HTTPException(e);
		}
	}

	public HTTPResponse2 postMultipart(String uri, String name, String fileName, byte[] fileBytes)
			throws HTTPException {
		return postMultipart(uri, name, null, fileName, fileBytes);
	}

	public HTTPResponse2 postMultipart(String uri, String name, String contentType, String fileName, byte[] fileBytes)
			throws HTTPException {
		return postMultipart(uri, name, contentType, fileName, new ByteArrayInputStream(fileBytes));
	}

}
