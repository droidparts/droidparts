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

import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.util.IOUtils.readToString;
import static org.droidparts.util.IOUtils.silentlyClose;
import static org.droidparts.util.Strings.isNotEmpty;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.droidparts.net.http.HTTPException;
import org.droidparts.util.L;

public class HTTPInputStream extends BufferedInputStream {

	public static HTTPInputStream getInstance(HttpURLConnection conn,
			boolean useErrorStream) throws HTTPException {
		try {
			InputStream is = useErrorStream ? conn.getErrorStream() : conn
					.getInputStream();
			is = getUnpackedInputStream(conn.getContentEncoding(), is);
			return new HTTPInputStream(is, conn, null);
		} catch (Exception e) {
			throw new HTTPException(e);
		}
	}

	public static HTTPInputStream getInstance(HttpResponse resp)
			throws HTTPException {
		HttpEntity entity = resp.getEntity();
		try {
			InputStream is = entity.getContent();
			Header ce = entity.getContentEncoding();
			is = getUnpackedInputStream(ce != null ? ce.getValue() : null, is);
			return new HTTPInputStream(is, null, entity);
		} catch (Exception e) {
			throw new HTTPException(e);
		}
	}

	private static InputStream getUnpackedInputStream(String contentEncoding,
			InputStream is) throws IOException {
		L.d("Content-Encoding: %s.", contentEncoding);
		if (isNotEmpty(contentEncoding)) {
			contentEncoding = contentEncoding.toLowerCase();
			if (contentEncoding.contains("gzip")) {
				return new GZIPInputStream(is);
			} else if (contentEncoding.contains("deflate")) {
				return new InflaterInputStream(is);
			}
		}
		return is;
	}

	private final HttpURLConnection conn;
	private final HttpEntity entity;

	private HTTPInputStream(InputStream is, HttpURLConnection conn,
			HttpEntity entity) throws HTTPException {
		super(is, BUFFER_SIZE);
		this.conn = conn;
		this.entity = entity;
	}

	public String readAndClose() throws HTTPException {
		try {
			return readToString(this);
		} catch (Exception e) {
			throw new HTTPException(e);
		} finally {
			silentlyClose(this);
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		if (conn != null) {
			conn.disconnect();
		} else if (entity != null) {
			entity.consumeContent();
		}
	}

}