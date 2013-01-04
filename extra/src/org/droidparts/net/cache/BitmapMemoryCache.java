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
package org.droidparts.net.cache;

import static android.content.Context.ACTIVITY_SERVICE;
import static org.droidparts.util.ui.BitmapUtils.getSize;

import org.droidparts.util.L;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;

public final class BitmapMemoryCache {

	public static final int CACHE_DISABLED = 0;
	public static final int MEMORY_CACHE_DEFAULT_PERCENT = 20;
	public static final int MEMORY_CACHE_DEFAULT_MAX_ITEM_SIZE = 256 * 1024;

	private static BitmapMemoryCache instance;

	public static BitmapMemoryCache getDefaultInstance(Context ctx) {
		if (instance == null) {
			instance = new BitmapMemoryCache(ctx, MEMORY_CACHE_DEFAULT_PERCENT,
					MEMORY_CACHE_DEFAULT_MAX_ITEM_SIZE);
		}
		return instance;
	}

	private BitmapLruCache cache;
	private final int memoryCacheMaxItemSize;

	public BitmapMemoryCache(Context ctx, int percent,
			int memoryCacheMaxItemSize) {
		this.memoryCacheMaxItemSize = memoryCacheMaxItemSize;
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
	}

	public boolean put(String key, Bitmap bm) {
		boolean put = false;
		if (cache != null && getSize(bm) < memoryCacheMaxItemSize) {
			cache.put(key, bm);
			put = true;
		}
		return put;
	}

	public Bitmap get(String key) {
		Bitmap bm = null;
		if (cache != null) {
			bm = cache.get(key);
		}
		return bm;
	}

}
