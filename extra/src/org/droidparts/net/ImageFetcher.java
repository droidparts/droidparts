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
package org.droidparts.net;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.graphics.Color.TRANSPARENT;
import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.util.io.IOUtils.silentlyClose;
import static org.droidparts.util.ui.BitmapUtils.getSize;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.droidparts.http.RESTClient;
import org.droidparts.net.cache.BitmapDiskCache;
import org.droidparts.net.cache.BitmapLruCache;
import org.droidparts.util.AppUtils;
import org.droidparts.util.L;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

public class ImageFetcher {

	public static int MEMORY_CACHE_DISABLED = 0;
	public static int MEMORY_CACHE_DEFAULT_PERCENT = 20;
	public static int MEMORY_CACHE_DEFAULT_MAX_ITEM_SIZE = 256 * 1024;

	private ThreadPoolExecutor cacheExecutor;
	private RESTClient restClient;

	private BitmapLruCache memoryCache;
	private BitmapDiskCache diskCache;

	final ConcurrentHashMap<ImageView, Long> currWIP = new ConcurrentHashMap<ImageView, Long>();
	ThreadPoolExecutor fetchExecutor;
	volatile Handler handler;

	private BitmapReshaper reshaper;
	int crossFadeMillis = 0;
	int maxMemoryCacheItemSize;

	public ImageFetcher(Context ctx) {
		handler = new Handler(Looper.getMainLooper());
		cacheExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
		setExecutor((ThreadPoolExecutor) Executors.newFixedThreadPool(1));
		setRESTClient(new RESTClient(ctx));
		//
		File cacheDir = new AppUtils(ctx).getExternalCacheDir();
		if (cacheDir != null) {
			File imgCacheDir = (cacheDir == null) ? null : new File(cacheDir,
					"img");
			setDiskCacheDir(imgCacheDir);
		} else {
			L.w("External cache dir null. Lacking 'android.permission.WRITE_EXTERNAL_STORAGE' permission?");
		}
		//
		setMemoryCachePercent(ctx, MEMORY_CACHE_DEFAULT_PERCENT);
		setMaxMemoryCacheItemSize(MEMORY_CACHE_DEFAULT_MAX_ITEM_SIZE);
	}

	public void setBitmapReshaper(BitmapReshaper reshaper) {
		this.reshaper = reshaper;
	}

	public void setCrossFadeDuration(int millisec) {
		this.crossFadeMillis = millisec;
	}

	public void setExecutor(ThreadPoolExecutor exec) {
		this.fetchExecutor = exec;
	}

	public void setRESTClient(RESTClient client) {
		this.restClient = client;
	}

	public void setDiskCacheDir(File dir) {
		diskCache = (dir == null) ? null : new BitmapDiskCache(dir);
	}

	public boolean setMemoryCachePercent(Context ctx, int percent) {
		int maxBytes = 0;
		if (percent != MEMORY_CACHE_DISABLED) {
			int maxAvailableMemory = ((ActivityManager) ctx
					.getSystemService(ACTIVITY_SERVICE)).getMemoryClass();
			maxBytes = (int) (maxAvailableMemory * ((float) percent / 100)) * 1024 * 1024;
		}
		try {
			memoryCache = (BitmapLruCache) Class
					.forName("org.droidparts.net.cache.StockBitmapLruCache")
					.getConstructor(int.class).newInstance(maxBytes);
			L.i("Using stock LruCache.");
			return true;
		} catch (Throwable t) {
			try {
				memoryCache = (BitmapLruCache) Class
						.forName(
								"org.droidparts.net.cache.SupportBitmapLruCache")
						.getConstructor(int.class).newInstance(maxBytes);
				L.i("Using Support Package LruCache.");
				return true;
			} catch (Throwable tr) {
				L.i("LruCache not available.");
				return false;
			}
		}
	}

	public void setMaxMemoryCacheItemSize(int bytes) {
		this.maxMemoryCacheItemSize = bytes;
	}

	public BitmapDiskCache getDiskCache() {
		return diskCache;
	}

	//

	public void attachImage(ImageView imageView, String imgUrl) {
		long submitted = System.nanoTime();
		currWIP.put(imageView, submitted);
		Runnable r = new ReadFromCacheRunnable(this, imageView, imgUrl,
				submitted);
		cacheExecutor.remove(r);
		fetchExecutor.remove(r);
		cacheExecutor.execute(r);
	}

	public Bitmap getImage(String imgUrl) {
		Bitmap bm = getCached(imgUrl);
		if (bm == null) {
			bm = fetch(null, imgUrl);
		}
		if (bm != null) {
			putToCache(imgUrl, bm);
		}
		return bm;
	}

	//

	protected void onFetchProgressChanged(View imageView, String imgUrl,
			int kBTotal, int kBReceived) {
		// L.d(String.format("Fetched %d of %d kB from %s.", kBReceived,
		// kBTotal,
		// imgUrl));
	}

	protected void onFetchFailed(View imageView, String imgUrl, Exception e) {
	}

	protected void onBitmapWillBeSet(ImageView imageView) {
	}

	//

	Bitmap fetch(ImageView imageView, String imgUrl) {
		int bytesReadTotal = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			Pair<Integer, BufferedInputStream> resp = restClient
					.getInputStream(imgUrl);
			int kBTotal = resp.first / 1024;
			bis = resp.second;
			int bytesRead;
			while ((bytesRead = bis.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
				bytesReadTotal += bytesRead;
				onFetchProgressChanged(imageView, imgUrl, kBTotal,
						bytesReadTotal / 1024);
			}
			byte[] data = baos.toByteArray();
			Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
			return bm;
		} catch (Exception e) {
			L.w("Failed to fetch " + imgUrl);
			L.d(e);
			onFetchFailed(imageView, imgUrl, e);
			return null;
		} finally {
			silentlyClose(bis, baos);
		}
	}

	void runOnUiThread(Runnable r) {
		boolean success = handler.post(r);
		// a hack
		while (!success) {
			handler = new Handler(Looper.getMainLooper());
			success = handler.post(r);
		}
	}

	Bitmap getCached(String imgUrl) {
		String key = getKey(imgUrl);
		Bitmap bm = null;
		if (reshaper != null) {
			if (memoryCache != null) {
				bm = memoryCache.get(key);
			}
			if (bm == null) {
				if (diskCache != null) {
					bm = diskCache.get(key);
				}
				if (bm != null) {
					cacheToMemory(key, bm);
				}
			}
		}
		if (bm == null) {
			if (memoryCache != null) {
				bm = memoryCache.get(key);
			}
			if (bm == null) {
				if (diskCache != null) {
					bm = diskCache.get(imgUrl);
				}
				if (bm != null) {
					if (reshaper != null) {
						bm = reshaper.reshape(bm);
					}
					cacheToMemory(key, bm);
				}
			}
		}
		return bm;
	}

	Bitmap putToCache(String imgUrl, Bitmap bm) {
		if (reshaper != null) {
			bm = reshaper.reshape(bm);
		}
		String key = getKey(imgUrl);
		cacheToMemory(key, bm);
		if (diskCache != null) {
			diskCache.put(key, bm);
		}
		return bm;
	}

	private String getKey(String imgUrl) {
		return (reshaper == null) ? imgUrl
				: (imgUrl + reshaper.getReshaperId());
	}

	private void cacheToMemory(String key, Bitmap bm) {
		if (memoryCache != null && getSize(bm) < maxMemoryCacheItemSize) {
			memoryCache.put(key, bm);
		}
	}

	//

	static abstract class ImageViewRunnable implements Runnable {

		protected final ImageFetcher imageFetcher;
		protected final ImageView imageView;

		public ImageViewRunnable(ImageFetcher imageFetcher, ImageView imageView) {
			this.imageFetcher = imageFetcher;
			this.imageView = imageView;
		}

		@Override
		public boolean equals(Object o) {
			boolean eq = false;
			if (this == o) {
				eq = true;
			} else if (o instanceof ImageViewRunnable) {
				eq = imageView.equals(((ImageViewRunnable) o).imageView);
			}
			return eq;
		}

		@Override
		public int hashCode() {
			return imageView.hashCode();
		}
	}

	static class ReadFromCacheRunnable extends ImageViewRunnable {

		protected final String imgUrl;
		protected final long submitted;

		public ReadFromCacheRunnable(ImageFetcher imageFetcher,
				ImageView imageView, String imgUrl, long submitted) {
			super(imageFetcher, imageView);
			this.imgUrl = imgUrl;
			this.submitted = submitted;
		}

		@Override
		public void run() {
			Bitmap bm = imageFetcher.getCached(imgUrl);
			if (bm == null) {
				FetchAndCacheRunnable r = new FetchAndCacheRunnable(
						imageFetcher, imageView, imgUrl, submitted);
				imageFetcher.fetchExecutor.execute(r);
			} else {
				imageFetcher.currWIP.remove(imageView);
				SetBitmapRunnable r = new SetBitmapRunnable(imageFetcher,
						imageView, bm, imageFetcher.crossFadeMillis);
				imageFetcher.runOnUiThread(r);
			}
		}
	}

	static class FetchAndCacheRunnable extends ReadFromCacheRunnable {

		public FetchAndCacheRunnable(ImageFetcher imageFetcher,
				ImageView imageView, String imgUrl, long submitted) {
			super(imageFetcher, imageView, imgUrl, submitted);
		}

		@Override
		public void run() {
			Bitmap bm = imageFetcher.fetch(imageView, imgUrl);
			if (bm != null) {
				imageFetcher.putToCache(imgUrl, bm);
				//
				Long timestamp = imageFetcher.currWIP.get(imageView);
				if (timestamp != null && timestamp == submitted) {
					imageFetcher.currWIP.remove(imageView);
					SetBitmapRunnable r = new SetBitmapRunnable(imageFetcher,
							imageView, bm, imageFetcher.crossFadeMillis);
					imageFetcher.runOnUiThread(r);
				}
			}
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + ": " + imgUrl;
		}

	}

	static class SetBitmapRunnable extends ImageViewRunnable {

		private final Bitmap bitmap;
		private final int crossFadeMillis;

		public SetBitmapRunnable(ImageFetcher imageFetcher,
				ImageView imageView, Bitmap bitmap, int crossFadeMillis) {
			super(imageFetcher, imageView);
			this.bitmap = bitmap;
			this.crossFadeMillis = crossFadeMillis;
		}

		@Override
		public void run() {
			imageFetcher.onBitmapWillBeSet(imageView);
			if (crossFadeMillis > 0) {
				Drawable prevDrawable = imageView.getDrawable();
				if (prevDrawable == null) {
					prevDrawable = new ColorDrawable(TRANSPARENT);
				}
				TransitionDrawable transitionDrawable = new TransitionDrawable(
						new Drawable[] { prevDrawable,
								new BitmapDrawable(bitmap) });
				imageView.setImageDrawable(transitionDrawable);
				transitionDrawable.startTransition(crossFadeMillis);
			} else {
				imageView.setImageBitmap(bitmap);
			}
		}

	}

}