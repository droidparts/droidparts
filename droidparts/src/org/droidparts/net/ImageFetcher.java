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
package org.droidparts.net;

import static android.graphics.Color.TRANSPARENT;
import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.util.io.IOUtils.silentlyClose;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import org.droidparts.http.RESTClient;
import org.droidparts.net.cache.BitmapCache;
import org.droidparts.net.cache.BitmapDiskCache;
import org.droidparts.net.cache.BitmapMemoryCache;
import org.droidparts.net.concurrent.BackgroundExecutor;
import org.droidparts.util.L;

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

	private final RESTClient restClient;

	private final BitmapCache memoryCache;
	private final BitmapCache diskCache;

	final ThreadPoolExecutor cacheExecutor;
	final ThreadPoolExecutor fetchExecutor;

	final ConcurrentHashMap<ImageView, Long> wip = new ConcurrentHashMap<ImageView, Long>();
	private Handler handler;

	ImageFetchListener fetchListener;
	private ImageReshaper reshaper;
	int crossFadeMillis = 0;

	public ImageFetcher(Context ctx) {
		this(ctx, new BackgroundExecutor(2), new RESTClient(ctx),
				BitmapMemoryCache.getDefaultInstance(ctx), BitmapDiskCache
						.getDefaultInstance(ctx));
	}

	protected ImageFetcher(Context ctx, ThreadPoolExecutor fetchExecutor,
			RESTClient restClient, BitmapCache memoryCache,
			BitmapCache diskCache) {
		this.fetchExecutor = fetchExecutor;
		this.restClient = restClient;
		this.memoryCache = memoryCache;
		this.diskCache = diskCache;
		handler = new Handler(Looper.getMainLooper());
		cacheExecutor = new BackgroundExecutor(1);
	}

	public void setFetchListener(ImageFetchListener fetchListener) {
		wip.clear();
		this.fetchListener = fetchListener;
	}

	public void setReshaper(ImageReshaper reshaper) {
		wip.clear();
		this.reshaper = reshaper;
	}

	public void setCrossFadeDuration(int millisec) {
		wip.clear();
		this.crossFadeMillis = millisec;
	}

	//

	public void attachImage(ImageView imageView, String imgUrl) {
		if (fetchListener != null) {
			fetchListener.onTaskAdded(imageView);
		}
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
			bm = reshapeAndCache(imgUrl, bm);
		}
		return bm;
	}

	//

	public void clearCacheOlderThan(int hours) {
		if (diskCache != null && diskCache instanceof BitmapDiskCache) {
			final long timestamp = System.currentTimeMillis() - hours * 60 * 60
					* 1000;
			cacheExecutor.execute(new Runnable() {

				@Override
				public void run() {
					((BitmapDiskCache) diskCache)
							.purgeFilesAccessedBefore(timestamp);
				}
			});
		} else {
			L.w("Failed to clear null or incompatible disk cache.");
		}
	}

	//

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
				if (fetchListener != null) {
					final int kBReceived = bytesReadTotal / 1024;
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							fetchListener.onDownloadProgressChanged(imageView,
									kBTotal, kBReceived);
						}
					});
				}
			}
			byte[] data = baos.toByteArray();
			Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
			return bm;
		} catch (final Exception e) {
			L.w("Failed to fetch %s.", imgUrl);
			L.d(e);
			if (fetchListener != null) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						fetchListener.onDownloadFailed(imageView, e);
					}
				});
			}
			return null;
		} finally {
			silentlyClose(bis, baos);
		}
	}

	Bitmap getCached(String imgUrl) {
		String key = getCacheKey(imgUrl);
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
					if (memoryCache != null) {
						memoryCache.put(key, bm);
					}
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
					if (memoryCache != null) {
						memoryCache.put(key, bm);
					}
				}
			}
		}
		return bm;
	}

	Bitmap reshapeAndCache(String imgUrl, Bitmap bm) {
		if (diskCache != null) {
			diskCache.put(imgUrl, bm);
		}
		if (reshaper != null) {
			bm = reshaper.reshape(bm);
		}
		String key = getCacheKey(imgUrl);
		if (memoryCache != null) {
			memoryCache.put(key, bm);
		}
		if (diskCache != null && reshaper != null) {
			diskCache.put(key, bm);
		}
		return bm;
	}

	private String getCacheKey(String imgUrl) {
		return (reshaper == null) ? imgUrl : (imgUrl + reshaper.getId());
	}

	private void runOnUiThread(Runnable r) {
		boolean success = handler.post(r);
		// a hack
		while (!success) {
			handler = new Handler(Looper.getMainLooper());
			success = handler.post(r);
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
				bm = imageFetcher.reshapeAndCache(imgUrl, bm);
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
			if (imageFetcher.fetchListener != null) {
				imageFetcher.fetchListener.onTaskCompleted(imageView);
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