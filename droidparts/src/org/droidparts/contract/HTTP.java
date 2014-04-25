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
package org.droidparts.contract;

import org.apache.http.HttpStatus;

public interface HTTP {

	public interface Method {

		String GET = "GET";
		String PUT = "PUT";
		String POST = "POST";
		String DELETE = "DELETE";

	}

	public interface ResponseCode extends HttpStatus {
	}

	public interface ContentType {

		String APPLICATION_FORM_DATA = "application/x-www-form-urlencoded";
		String APPLICATION_JSON = "application/json";
		String MULTIPART = "multipart/form-data";
		String TEXT_PLAIN = "text/plain";

	}

	public interface Header {

		// Request
		String USER_AGENT = "User-Agent";
		String ACCEPT_ENCODING = "Accept-Encoding";
		String ACCEPT_CHARSET = "Accept-Charset";
		String CACHE_CONTROL = "Cache-Control";
		String CONNECTION = "Connection";
		String IF_MODIFIED_SINCE = "If-Modified-Since";
		String IF_NONE_MATCH = "If-None-Match";

		// Response
		String CONTENT_LENGTH = "Content-Length";
		String CONTENT_TYPE = "Content-Type";
		String CONTENT_ENCODING = "Content-Encoding";
		String ETAG = "ETag";

		String DATE = "Date";
		String LAST_MODIFIED = "Last-Modified";
		String ESPIRES = "Expires";

		// Value
		String KEEP_ALIVE = "keep-alive";
		String NO_CACHE = "no-cache";

	}

}