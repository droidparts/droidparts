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
package org.droidparts.contract;

import org.apache.http.HttpStatus;

public interface HTTP {

	public interface Method {

		public static final String GET = "GET";
		public static final String PUT = "PUT";
		public static final String POST = "POST";
		public static final String DELETE = "DELETE";

	}

	public interface ResponseCode extends HttpStatus {
	}

	public interface ContentType {

		public static final String APPLICATION_FORM_DATA = "application/x-www-form-urlencoded";
		public static final String APPLICATION_JSON = "application/json";
		public static final String TEXT_PLAIN = "text/plain";

	}

	public interface Header {

		// Request
		public static final String ACCEPT_ENCODING = "Accept-Encoding";
		public static final String ACCEPT_CHARSET = "Accept-Charset";
		public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
		public static final String IF_NONE_MATCH = "If-None-Match";

		// Response
		public static final String CONTENT_LENGTH = "Content-Length";
		public static final String CONTENT_TYPE = "Content-Type";
		public static final String CONTENT_ENCODING = "Content-Encoding";
		public static final String ETAG = "ETag";

		public static final String DATE = "Date";
		public static final String LAST_MODIFIED = "Last-Modified";
		public static final String ESPIRES = "Expires";

	}

}
