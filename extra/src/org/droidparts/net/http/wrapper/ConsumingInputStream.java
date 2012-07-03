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

import static org.droidparts.contract.Constants.BUFFER_SIZE;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;

public class ConsumingInputStream extends BufferedInputStream {

	private final HttpURLConnection conn;
	private final HttpEntity entity;

	public ConsumingInputStream(InputStream is, HttpURLConnection conn) {
		this(is, null, conn);
	}

	public ConsumingInputStream(InputStream is, HttpEntity entity) {
		this(is, entity, null);
	}

	private ConsumingInputStream(InputStream is, HttpEntity entity,
			HttpURLConnection conn) {
		super(is, BUFFER_SIZE);
		this.entity = entity;
		this.conn = conn;
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