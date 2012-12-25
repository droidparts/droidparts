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
package org.droidparts.util;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.graphics.Color.TRANSPARENT;
import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.util.io.IOUtils.silentlyClose;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.droidparts.http.RESTClient;
import org.droidparts.util.io.BitmapCache;

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

public class ImageAttacher {

	public static int MEMORY_CACHE_DISABLED = 0;
	public static int MEMORY_CACHE_DEFAULT_PERCENT = 20;

	public interface Reshaper {

		String getId();

		Bitmap reshape(Bitmap bm);

	}

	private final ThreadPoolExecutor cacheExecutor;
	private final RESTClient restClient;
	private final BitmapCache bitmapCache;

	final ConcurrentHashMap<ImageView, Long> currWIP = new ConcurrentHashMap<ImageView, Long>();
	final ThreadPoolExecutor fetchExecutor;
	volatile Handler handler;

	private Reshaper reshaper;
	int crossFadeMillis = 0;

	public ImageAttacher(Context ctx) {
		this(ctx, MEMORY_CACHE_DEFAULT_PERCENT);
	}

	public ImageAttacher(Context ctx, int memoryCachePercent) {
		this((ThreadPoolExecutor) Executors.newFixedThreadPool(1),
				new RESTClient(ctx), getDefaultBitmapCache(ctx,
						memoryCachePercent));
	}

	public ImageAttacher(ThreadPoolExecutor executor, RESTClient restClient,
			BitmapCache bitmapCache) {
		this.fetchExecutor = executor;
		this.restClient = restClient;
		this.bitmapCache = bitmapCache;
		cacheExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
		handler = new Handler(Looper.getMainLooper());
	}

	public void setCrossFadeDuration(int millisec) {
		this.crossFadeMillis = millisec;
	}

	public void setReshaper(Reshaper reshaper) {
		this.reshaper = reshaper;
	}

	public BitmapCache getBitmapCache() {
		return bitmapCache;
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
		L.w("Failed to fetch " + imgUrl);
	}

	//

	protected static BitmapCache getDefaultBitmapCache(Context ctx,
			int memoryCachePercent) {
		//
		File cacheDir = new AppUtils(ctx).getExternalCacheDir();
		File imgCacheDir = null;
		if (cacheDir != null) {
			imgCacheDir = (cacheDir == null) ? null : new File(cacheDir, "img");
		} else {
			L.w("External cache dir null. Lacking 'android.permission.WRITE_EXTERNAL_STORAGE' permission?");
		}
		//
		int maxMemoryBytes = 0;
		if (memoryCachePercent != MEMORY_CACHE_DISABLED) {
			int maxAvailableMemory = ((ActivityManager) ctx
					.getSystemService(ACTIVITY_SERVICE)).getMemoryClass();
			maxMemoryBytes = (int) (maxAvailableMemory * ((float) memoryCachePercent / 100)) * 1024 * 1024;
		}
		//
		return new BitmapCache(imgCacheDir, maxMemoryBytes);
	}

	Bitmap getCached(String imgUrl) {
		Bitmap bm = null;
		if (reshaper != null) {
			bm = bitmapCache.get(imgUrl + reshaper.getId());
		}
		if (bm == null) {
			bm = bitmapCache.get(imgUrl);
			if (bm != null && reshaper != null) {
				bm = reshaper.reshape(bm);
			}
		}
		return bm;
	}

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
			L.d(e);
			onFetchFailed(imageView, imgUrl, e);
			return null;
		} finally {
			silentlyClose(bis, baos);
		}
	}

	boolean putToCache(String imgUrl, Bitmap bm) {
		if (reshaper != null) {
			imgUrl += reshaper.getId();
			bm = reshaper.reshape(bm);
		}
		return bitmapCache.put(imgUrl, bm);
	}

	void runOnUiThread(Runnable r) {
		boolean success = handler.post(r);
		// a hack
		while (!success) {
			handler = new Handler(Looper.getMainLooper());
			success = handler.post(r);
		}
	}

	//

	static abstract class ImageViewRunnable implements Runnable {

		protected final ImageView imageView;

		public ImageViewRunnable(ImageView imageView) {
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

		protected final ImageAttacher ia;
		protected final String imgUrl;
		protected final long submitted;

		public ReadFromCacheRunnable(ImageAttacher imageAttacher,
				ImageView imageView, String imgUrl, long submitted) {
			super(imageView);
			this.ia = imageAttacher;
			this.imgUrl = imgUrl;
			this.submitted = submitted;
		}

		@Override
		public void run() {
			Bitmap bm = ia.getCached(imgUrl);
			if (bm == null) {
				FetchAndCacheRunnable r = new FetchAndCacheRunnable(ia,
						imageView, imgUrl, submitted);
				ia.fetchExecutor.execute(r);
			} else {
				ia.currWIP.remove(imageView);
				SetBitmapRunnable r = new SetBitmapRunnable(imageView, bm,
						ia.crossFadeMillis);
				ia.runOnUiThread(r);
			}
		}
	}

	static class FetchAndCacheRunnable extends ReadFromCacheRunnable {

		public FetchAndCacheRunnable(ImageAttacher imageAttacher,
				ImageView imageView, String imgUrl, long submitted) {
			super(imageAttacher, imageView, imgUrl, submitted);
		}

		@Override
		public void run() {
			Bitmap bm = ia.fetch(imageView, imgUrl);
			if (bm != null) {
				ia.putToCache(imgUrl, bm);
				//
				Long timestamp = ia.currWIP.get(imageView);
				if (timestamp != null && timestamp == submitted) {
					ia.currWIP.remove(imageView);
					SetBitmapRunnable r = new SetBitmapRunnable(imageView, bm,
							ia.crossFadeMillis);
					ia.runOnUiThread(r);
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

		public SetBitmapRunnable(ImageView imageView, Bitmap bitmap,
				int crossFadeMillis) {
			super(imageView);
			this.bitmap = bitmap;
			this.crossFadeMillis = crossFadeMillis;
		}

		@Override
		public void run() {
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