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
package org.droidparts.net.image;

import static android.graphics.Color.TRANSPARENT;
import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.util.IOUtils.silentlyClose;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import org.droidparts.contract.HTTP.Header;
import org.droidparts.net.concurrent.BackgroundExecutor;
import org.droidparts.net.http.HTTPResponse;
import org.droidparts.net.http.RESTClient;
import org.droidparts.net.http.worker.HTTPWorker;
import org.droidparts.net.image.cache.BitmapDiskCache;
import org.droidparts.net.image.cache.BitmapMemoryCache;
import org.droidparts.util.L;
import org.droidparts.util.ui.BitmapUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
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

	private final BitmapMemoryCache memoryCache;
	private final BitmapDiskCache diskCache;

	final ThreadPoolExecutor cacheExecutor;
	final ThreadPoolExecutor fetchExecutor;

	final ConcurrentHashMap<ImageView, Long> wip = new ConcurrentHashMap<ImageView, Long>();
	private Handler handler;

	ImageFetchListener fetchListener;
	private ImageReshaper reshaper;

	private int reqWidth, reqHeight;
	int crossFadeMillis = 0;

	public ImageFetcher(Context ctx) {
		this(ctx, new BackgroundExecutor(2), new RESTClient(ctx),
				BitmapMemoryCache.getDefaultInstance(ctx), BitmapDiskCache
						.getDefaultInstance(ctx));
	}

	protected ImageFetcher(Context ctx, ThreadPoolExecutor fetchExecutor,
			RESTClient restClient, BitmapMemoryCache memoryCache,
			BitmapDiskCache diskCache) {
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
		if (reshaper != null) {
			reqWidth = reshaper.getWidthHint();
			reqHeight = reshaper.getHeightHint();
		} else {
			reqWidth = reqHeight = 0;
		}
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
		Bitmap bm = getCachedReshaped(imgUrl);
		if (bm == null) {
			Pair<Bitmap, Pair<String, byte[]>> bmData = fetch(null, imgUrl);
			if (bmData != null) {
				bm = reshapeAndCache(imgUrl, bmData);
			}
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

	Pair<Bitmap, Pair<String, byte[]>> fetch(final ImageView imageView,
			final String imgUrl) {
		Pair<Bitmap, Pair<String, byte[]>> bmData = null;
		int bytesReadTotal = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			HTTPResponse resp = restClient.getInputStream(imgUrl);
			final int kBTotal = resp.getHeaderInt(Header.CONTENT_LENGTH) / 1024;
			bis = resp.inputStream;
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
			Bitmap bm = BitmapUtils.decodeScaled(
					new ByteArrayInputStream(data), reqWidth, reqHeight);
			if (bm != null) {
				String contentType = resp.getHeaderString(Header.CONTENT_TYPE);
				bmData = Pair.create(bm, Pair.create(contentType, data));
			}
		} catch (final Exception e) {
			HTTPWorker.throwIfNetworkOnMainThreadException(e);
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
		} finally {
			silentlyClose(bis, baos);
		}
		return bmData;
	}

	protected Bitmap getCachedReshaped(String imgUrl) {
		String key = getCacheKey(imgUrl);
		Bitmap bm = null;
		if (reshaper != null) {
			if (memoryCache != null) {
				bm = memoryCache.get(key);
			}
			if (bm == null) {
				if (diskCache != null) {
					bm = diskCache.get(key, reqWidth, reqHeight);
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
					bm = diskCache.get(imgUrl, reqWidth, reqHeight);
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

	Bitmap reshapeAndCache(String imgUrl,
			Pair<Bitmap, Pair<String, byte[]>> bmData) {
		Bitmap bm = bmData.first;
		if (diskCache != null) {
			diskCache.put(imgUrl, bmData.second.second);
		}
		if (reshaper != null) {
			bm = reshaper.reshape(bm);
		}
		String key = getCacheKey(imgUrl);
		if (memoryCache != null) {
			memoryCache.put(key, bm);
		}
		if (diskCache != null && reshaper != null) {
			Pair<CompressFormat, Integer> cacheFormat = reshaper
					.getCacheFormat(bmData.second.first);
			if (cacheFormat != null) {
				diskCache.put(key, bm, cacheFormat);
			}
		}
		return bm;
	}

	private String getCacheKey(String imgUrl) {
		return (reshaper == null) ? imgUrl : (imgUrl + reshaper.getCacheId());
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
			Bitmap bm = imageFetcher.getCachedReshaped(imgUrl);
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
			Pair<Bitmap, Pair<String, byte[]>> bmData = imageFetcher.fetch(
					imageView, imgUrl);
			if (bmData != null) {
				Bitmap bm = bmData.first;
				bm = imageFetcher.reshapeAndCache(imgUrl, bmData);
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
