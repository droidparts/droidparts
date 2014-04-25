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
package org.droidparts.net.http.worker.wrapper;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.net.http.HttpResponseCache;

//ICS+
public class HttpResponseCacheWrapper {

	private static long DEFAULT_SIZE = 10 * 1024 * 1024; // 10 MiB

	public static void install(Context ctx) throws IOException {
		install(ctx, DEFAULT_SIZE);
	}

	public static void install(Context ctx, long maxSize) throws IOException {
		File cacheDir = new File(ctx.getCacheDir(), "http");
		HttpResponseCache.install(cacheDir, maxSize);
	}

	public static void delete(Context ctx) throws IOException {
		HttpResponseCache instance = HttpResponseCache.getInstalled();
		if (instance != null) {
			instance.delete();
		}
	}

}
