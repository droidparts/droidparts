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
package org.droidparts.net.image.cache;

import static android.content.Context.ACTIVITY_SERVICE;
import static org.droidparts.util.ui.BitmapUtils.getSize;

import org.droidparts.util.L;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;

public class BitmapMemoryCache {

	public interface BitmapLruCache {
		Bitmap put(String key, Bitmap bm);

		Bitmap get(String key);
	}

	private static final int DEFAULT_APP_MEMORY_PERCENT = 20;
	private static final int DEFAULT_MAX_ITEM_SIZE = 256 * 1024;

	private static BitmapMemoryCache instance;

	public static BitmapMemoryCache getDefaultInstance(Context ctx) {
		if (instance == null) {
			instance = new BitmapMemoryCache(ctx, DEFAULT_APP_MEMORY_PERCENT,
					DEFAULT_MAX_ITEM_SIZE);
		}
		return instance;
	}

	private BitmapLruCache cache;
	private final int maxItemSize;

	public BitmapMemoryCache(Context ctx, int appMemoryPercent, int maxItemSize) {
		this.maxItemSize = maxItemSize;
		int maxAvailableMemory = ((ActivityManager) ctx
				.getSystemService(ACTIVITY_SERVICE)).getMemoryClass();
		int maxBytes = (int) (maxAvailableMemory * ((float) appMemoryPercent / 100)) * 1024 * 1024;
		try {
			cache = new StockBitmapLruCache(maxBytes);
			L.i("Using stock LruCache.");
		} catch (Throwable t) {
			try {
				cache = new SupportBitmapLruCache(maxBytes);
				L.i("Using Support Package LruCache.");
			} catch (Throwable tr) {
				L.i("LruCache not available.");
			}
		}
	}

	public boolean isAvailable() {
		return (cache != null);
	}

	public boolean put(String key, Bitmap bm) {
		boolean put = false;
		if (isAvailable() && getSize(bm) <= maxItemSize) {
			cache.put(key, bm);
			put = true;
		}
		return put;
	}

	public Bitmap get(String key) {
		Bitmap bm = null;
		if (isAvailable()) {
			bm = cache.get(key);
		}
		L.v("MemoryCache " + ((bm == null) ? "miss" : "hit") + " for '%s'.",
				key);
		return bm;
	}

}
