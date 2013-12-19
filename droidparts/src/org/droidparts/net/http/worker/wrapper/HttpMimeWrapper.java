/**
 * Copyright 2013 Alex Yanchenko
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

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;

import java.io.File;

import static org.droidparts.contract.Constants.UTF8;

public class HttpMimeWrapper {

	public static HttpEntity buildMultipartEntity(String name, String contentType, File file) {
		MultipartEntity entity = new MultipartEntity();
		ContentBody contentBody;
		if (contentType != null) {
			contentBody = new FileBody(file, contentType, UTF8);
		} else {
			contentBody = new FileBody(file);
		}
		entity.addPart(name, contentBody);
		return entity;
	}
}
