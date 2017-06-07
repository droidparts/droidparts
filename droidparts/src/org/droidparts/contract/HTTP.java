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
package org.droidparts.contract;

public interface HTTP {

	interface Method {

		String GET = "GET";
		String PUT = "PUT";
		String POST = "POST";
		String DELETE = "DELETE";

	}

	interface StatusCode {
		int CONTINUE = 100;
		int SWITCHING_PROTOCOLS = 101;
		int PROCESSING = 102;
		int OK = 200;
		int CREATED = 201;
		int ACCEPTED = 202;
		int NON_AUTHORITATIVE_INFORMATION = 203;
		int NO_CONTENT = 204;
		int RESET_CONTENT = 205;
		int PARTIAL_CONTENT = 206;
		int MULTI_STATUS = 207;
		int MULTIPLE_CHOICES = 300;
		int MOVED_PERMANENTLY = 301;
		int MOVED_TEMPORARILY = 302;
		int SEE_OTHER = 303;
		int NOT_MODIFIED = 304;
		int USE_PROXY = 305;
		int TEMPORARY_REDIRECT = 307;
		int BAD_REQUEST = 400;
		int UNAUTHORIZED = 401;
		int PAYMENT_REQUIRED = 402;
		int FORBIDDEN = 403;
		int NOT_FOUND = 404;
		int METHOD_NOT_ALLOWED = 405;
		int NOT_ACCEPTABLE = 406;
		int PROXY_AUTHENTICATION_REQUIRED = 407;
		int REQUEST_TIMEOUT = 408;
		int CONFLICT = 409;
		int GONE = 410;
		int LENGTH_REQUIRED = 411;
		int PRECONDITION_FAILED = 412;
		int REQUEST_TOO_LONG = 413;
		int REQUEST_URI_TOO_LONG = 414;
		int UNSUPPORTED_MEDIA_TYPE = 415;
		int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
		int EXPECTATION_FAILED = 417;
		int INSUFFICIENT_SPACE_ON_RESOURCE = 419;
		int METHOD_FAILURE = 420;
		int UNPROCESSABLE_ENTITY = 422;
		int LOCKED = 423;
		int FAILED_DEPENDENCY = 424;
		int INTERNAL_SERVER_ERROR = 500;
		int NOT_IMPLEMENTED = 501;
		int BAD_GATEWAY = 502;
		int SERVICE_UNAVAILABLE = 503;
		int GATEWAY_TIMEOUT = 504;
		int HTTP_VERSION_NOT_SUPPORTED = 505;
		int INSUFFICIENT_STORAGE = 507;
	}

	interface ContentType {

		String APPLICATION_FORM_DATA = "application/x-www-form-urlencoded";
		String APPLICATION_JSON = "application/json";
		String MULTIPART = "multipart/form-data";
		String TEXT_PLAIN = "text/plain";

	}

	interface Header {

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