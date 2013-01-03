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
package org.droidparts.net.cache;

import static android.content.Context.ACTIVITY_SERVICE;

import org.droidparts.util.L;

import android.app.ActivityManager;
import android.content.Context;

public final class BitmapMemoryCache {

	protected static final int CACHE_DISABLED = 0;

	public static BitmapLruCache getInstance(Context ctx, int percent) {
		BitmapLruCache cache = null;
		if (percent > CACHE_DISABLED) {
			int maxBytes = 0;
			int maxAvailableMemory = ((ActivityManager) ctx
					.getSystemService(ACTIVITY_SERVICE)).getMemoryClass();
			maxBytes = (int) (maxAvailableMemory * ((float) percent / 100)) * 1024 * 1024;
			try {
				cache = (BitmapLruCache) Class
						.forName("org.droidparts.net.cache.StockBitmapLruCache")
						.getConstructor(int.class).newInstance(maxBytes);
				L.i("Using stock LruCache.");
			} catch (Throwable t) {
				try {
					cache = (BitmapLruCache) Class
							.forName(
									"org.droidparts.net.cache.SupportBitmapLruCache")
							.getConstructor(int.class).newInstance(maxBytes);
					L.i("Using Support Package LruCache.");
				} catch (Throwable tr) {
					L.i("LruCache not available.");
				}
			}
		}
		return cache;
	}

}
