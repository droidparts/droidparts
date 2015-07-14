/**
 * Copyright 2015 Alex Yanchenko
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
package org.droidparts.net.http.worker.wrapper;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.droidparts.util.IOUtils;

public class HttpMimeWrapper {

	public static HttpEntity buildMultipartEntity(String name, String contentType, String fileName, InputStream is)
			throws IOException {
		byte[] data = IOUtils.readToByteArray(is);
		ContentBody contentBody;
		if (contentType != null) {
			contentBody = new ByteArrayBody(data, contentType, fileName);
		} else {
			contentBody = new ByteArrayBody(data, fileName);
		}
		MultipartEntity entity = new MultipartEntity();
		entity.addPart(name, contentBody);
		return entity;
	}

}
