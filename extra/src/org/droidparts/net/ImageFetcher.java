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
import android.widget.ImageView;

public class ImageFetcher {

	protected static final int MEMORY_CACHE_DISABLED = 0;
	protected static final int MEMORY_CACHE_DEFAULT_PERCENT = 20;
	protected static final int MEMORY_CACHE_DEFAULT_MAX_ITEM_SIZE = 256 * 1024;

	protected static File getDefaultFileCacheDir(Context ctx) {
		File cacheDir = new AppUtils(ctx).getExternalCacheDir();
		File imgCacheDir = null;
		if (cacheDir != null) {
			imgCacheDir = (cacheDir == null) ? null : new File(cacheDir, "img");
		} else {
			L.w("External cache dir null. Lacking 'android.permission.WRITE_EXTERNAL_STORAGE' permission?");
		}
		return imgCacheDir;
	}

	protected boolean setMemoryCachePercent(Context ctx, int percent) {
		if (percent != MEMORY_CACHE_DISABLED) {
			int maxBytes = 0;
			int maxAvailableMemory = ((ActivityManager) ctx
					.getSystemService(ACTIVITY_SERVICE)).getMemoryClass();
			maxBytes = (int) (maxAvailableMemory * ((float) percent / 100)) * 1024 * 1024;
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
				}
			}
		}
		return false;
	}

	final ThreadPoolExecutor cacheExecutor;
	final ThreadPoolExecutor fetchExecutor;
	private final RESTClient restClient;

	private BitmapLruCache memoryCache;
	private final BitmapDiskCache diskCache;
	final int maxMemoryCacheItemSize;

	int crossFadeMillis = 0;
	private BitmapReshaper reshaper;
	ProgressListener listener;

	final ConcurrentHashMap<ImageView, Long> wip = new ConcurrentHashMap<ImageView, Long>();
	private Handler handler;

	public ImageFetcher(Context ctx) {
		this(ctx, (ThreadPoolExecutor) Executors.newFixedThreadPool(1),
				new RESTClient(ctx), getDefaultFileCacheDir(ctx),
				MEMORY_CACHE_DEFAULT_PERCENT,
				MEMORY_CACHE_DEFAULT_MAX_ITEM_SIZE);
	}

	protected ImageFetcher(Context ctx, ThreadPoolExecutor fetchExecutor,
			RESTClient restClient, File fileCacheDir, int memoryCachePercent,
			int maxMemoryCacheItemSize) {
		handler = new Handler(Looper.getMainLooper());
		cacheExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
		this.fetchExecutor = fetchExecutor;
		this.restClient = restClient;
		this.diskCache = (fileCacheDir != null) ? new BitmapDiskCache(
				fileCacheDir) : null;
		setMemoryCachePercent(ctx, memoryCachePercent);
		this.maxMemoryCacheItemSize = maxMemoryCacheItemSize;
	}

	public void setCrossFadeDuration(int millisec) {
		wip.clear();
		this.crossFadeMillis = millisec;
	}

	public void setBitmapReshaper(BitmapReshaper reshaper) {
		wip.clear();
		this.reshaper = reshaper;
	}

	public void setProgressListener(ProgressListener listener) {
		wip.clear();
		this.listener = listener;
	}

	public BitmapDiskCache getDiskCache() {
		return diskCache;
	}

	//

	public void attachImage(ImageView imageView, String imgUrl) {
		long submitted = System.nanoTime();
		wip.put(imageView, submitted);
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

	private void runOnUiThread(Runnable r) {
		boolean success = handler.post(r);
		// a hack
		while (!success) {
			handler = new Handler(Looper.getMainLooper());
			success = handler.post(r);
		}
	}

	Bitmap fetch(final ImageView imageView, final String imgUrl) {
		int bytesReadTotal = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			Pair<Integer, BufferedInputStream> resp = restClient
					.getInputStream(imgUrl);
			final int kBTotal = resp.first / 1024;
			bis = resp.second;
			int bytesRead;
			while ((bytesRead = bis.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
				bytesReadTotal += bytesRead;
				if (listener != null) {
					final int kBReceived = bytesReadTotal / 1024;
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							listener.onFetchProgressChanged(imageView, imgUrl,
									kBTotal, kBReceived);
						}
					});
				}
			}
			byte[] data = baos.toByteArray();
			Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
			return bm;
		} catch (final Exception e) {
			L.w("Failed to fetch " + imgUrl);
			L.d(e);
			if (listener != null) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						listener.onFetchFailed(imageView, imgUrl, e);
					}
				});
			}
			return null;
		} finally {
			silentlyClose(bis, baos);
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
				imageFetcher.wip.remove(imageView);
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
				Long timestamp = imageFetcher.wip.get(imageView);
				if (timestamp != null && timestamp == submitted) {
					imageFetcher.wip.remove(imageView);
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
			if (imageFetcher.listener != null) {
				imageFetcher.listener.onImageWillBeSet(imageView);
			}
			if (crossFadeMillis > 0) {
				Drawable prevDrawable = imageView.getDrawable();
				if (prevDrawable == null) {
					prevDrawable = new ColorDrawable(TRANSPARENT);
				}
				Drawable nextDrawable = new BitmapDrawable(
						imageView.getResources(), bitmap);
				TransitionDrawable transitionDrawable = new TransitionDrawable(
						new Drawable[] { prevDrawable, nextDrawable });
				imageView.setImageDrawable(transitionDrawable);
				transitionDrawable.startTransition(crossFadeMillis);
			} else {
				imageView.setImageBitmap(bitmap);
			}
		}

	}

}