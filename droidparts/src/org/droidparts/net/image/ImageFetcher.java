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
import static org.droidparts.util.Strings.isNotEmpty;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import org.droidparts.contract.HTTP.Header;
import org.droidparts.inner.BitmapFactoryUtil;
import org.droidparts.net.concurrent.BackgroundExecutor;
import org.droidparts.net.http.HTTPResponse;
import org.droidparts.net.http.RESTClient;
import org.droidparts.net.http.worker.HTTPWorker;
import org.droidparts.net.image.cache.BitmapDiskCache;
import org.droidparts.net.image.cache.BitmapMemoryCache;
import org.droidparts.util.L;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Point;
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

	private final ThreadPoolExecutor cacheExecutor;
	private final ThreadPoolExecutor fetchExecutor;

	private final LinkedHashMap<ImageView, String> todo = new LinkedHashMap<ImageView, String>();
	private final ConcurrentHashMap<ImageView, Long> wip = new ConcurrentHashMap<ImageView, Long>();
	private Handler handler;

	private ImageFetchListener fetchListener;
	private ImageReshaper reshaper;

	private int crossFadeMillis = 0;

	private volatile boolean paused;

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
		this.fetchListener = fetchListener;
	}

	public void setReshaper(ImageReshaper reshaper) {
		this.reshaper = reshaper;
	}

	public void setCrossFadeDuration(int millisec) {
		this.crossFadeMillis = millisec;
	}

	public void pause() {
		paused = true;
	}

	public void resume(boolean executePendingTasks) {
		paused = false;
		if (executePendingTasks) {
			for (ImageView iv : todo.keySet()) {
				attachImage(iv, todo.get(iv));
			}
		}
		todo.clear();
	}

	//

	public void attachImage(ImageView imageView, String imgUrl) {
		long submitted = System.nanoTime();
		wip.put(imageView, submitted);
		if (paused) {
			todo.remove(imageView);
			todo.put(imageView, imgUrl);
		} else {
			if (fetchListener != null) {
				fetchListener.onTaskAdded(imageView, imgUrl);
			}
			Runnable r = new ReadFromCacheRunnable(this, imageView, imgUrl,
					submitted);
			cacheExecutor.remove(r);
			fetchExecutor.remove(r);
			if (isNotEmpty(imgUrl)) {
				cacheExecutor.execute(r);
			} else {
				if (fetchListener != null) {
					fetchListener.onTaskCompleted(imageView, imgUrl);
				}
			}
		}
	}

	public Bitmap getImage(String imgUrl, ImageView imageViewHint) {
		Bitmap bm = readCached(imgUrl, imageViewHint);
		if (bm == null) {
			Pair<byte[], Pair<Bitmap, BitmapFactory.Options>> bmData = fetchAndDecode(
					imageViewHint, imgUrl);
			if (bmData != null) {
				cacheRawImage(imgUrl, bmData.first);
				bm = reshapeAndCache(imgUrl, imageViewHint, bmData.second);
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

	protected Pair<byte[], Pair<Bitmap, BitmapFactory.Options>> fetchAndDecode(
			final ImageView imageView, final String imgUrl) {
		Pair<byte[], Pair<Bitmap, BitmapFactory.Options>> bmData = null;
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
									imgUrl, kBTotal, kBReceived);
						}
					});
				}
			}
			byte[] data = baos.toByteArray();
			Point p = getSizeHint(imageView);
			Pair<Bitmap, BitmapFactory.Options> bm = BitmapFactoryUtil
					.decodeScaled(new ByteArrayInputStream(data),
							getConfigHint(), p.x, p.y);
			if (bm != null) {
				bmData = Pair.create(data, bm);
			}
		} catch (final Exception e) {
			HTTPWorker.throwIfNetworkOnMainThreadException(e);
			L.w("Failed to fetch %s.", imgUrl);
			L.d(e);
			if (fetchListener != null) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						fetchListener.onDownloadFailed(imageView, imgUrl, e);
					}
				});
			}
		} finally {
			silentlyClose(bis, baos);
		}
		return bmData;
	}

	protected Bitmap readCached(String imgUrl, ImageView imageView) {
		Bitmap bm = null;
		Point p = getSizeHint(imageView);
		String key = getCacheKey(imgUrl, p.x, p.y);
		if (memoryCache != null) {
			bm = memoryCache.get(key);
		}
		if (bm == null) {
			Pair<Bitmap, BitmapFactory.Options> bmData = diskCache.get(key,
					getConfigHint(), p.x, p.y);
			if (bmData != null) {
				bm = bmData.first;
				if (memoryCache != null) {
					memoryCache.put(key, bm);
				}
			} else {
				bmData = diskCache.get(imgUrl, getConfigHint(), p.x, p.y);
				if (bmData != null) {
					bm = reshapeAndCache(imgUrl, imageView, bmData);
				}
			}
		}
		return bm;
	}

	private void cacheRawImage(String imgUrl, byte[] data) {
		if (diskCache != null) {
			diskCache.put(imgUrl, data);
		}
	}

	private Bitmap reshapeAndCache(String imgUrl, ImageView imageView,
			Pair<Bitmap, BitmapFactory.Options> bmData) {
		Bitmap bm = bmData.first;
		if (reshaper != null) {
			Bitmap reshapedBm = reshaper.reshape(bm);
			if (bm != reshapedBm) {
				bm.recycle();
			}
			bm = reshapedBm;
		}
		Point p = getSizeHint(imageView);
		String key = getCacheKey(imgUrl, p.x, p.y);
		if (memoryCache != null) {
			memoryCache.put(key, bm);
		}
		if (diskCache != null && reshaper != null) {
			Pair<CompressFormat, Integer> cacheFormat = reshaper
					.getCacheFormat(bmData.second.outMimeType);
			if (cacheFormat != null) {
				diskCache.put(key, bm, cacheFormat);
			}
		}
		return bm;
	}

	private String getCacheKey(String imgUrl, int widthHint, int heightHint) {
		StringBuilder sb = new StringBuilder(5);
		sb.append(imgUrl);
		if (getConfigHint() != null) {
			sb.append(getConfigHint());
		}
		if (widthHint > 0 || heightHint > 0) {
			sb.append(widthHint);
			sb.append(heightHint);
		}
		if (reshaper != null) {
			sb.append(reshaper.getCacheId());
		}
		return sb.toString();
	}

	private Bitmap.Config getConfigHint() {
		return (reshaper != null) ? reshaper.getConfigHint() : null;
	}

	private Point getSizeHint(ImageView imageView) {
		Point p = new Point();
		if (reshaper != null) {
			p.x = reshaper.getWidthHint();
			p.y = reshaper.getHeightHint();
		}
		if (p.x <= 0 && p.y <= 0) {
			p = BitmapFactoryUtil.calcDecodeSizeHint(imageView);
		}
		return p;
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

	static abstract class ImageFetcherRunnable implements Runnable {

		protected final ImageFetcher imageFetcher;
		protected final ImageView imageView;
		protected final String imgUrl;
		protected final long submitted;

		public ImageFetcherRunnable(ImageFetcher imageFetcher,
				ImageView imageView, String imgUrl, long submitted) {
			this.imageFetcher = imageFetcher;
			this.imageView = imageView;
			this.imgUrl = imgUrl;
			this.submitted = submitted;
		}

		protected final void attachIfMostRecent(Bitmap bitmap) {
			Long mostRecent = imageFetcher.wip.get(imageView);
			if (mostRecent != null && submitted == mostRecent) {
				imageFetcher.wip.remove(imageView);
				SetBitmapRunnable r = new SetBitmapRunnable(imageFetcher,
						imageView, imgUrl, submitted, bitmap);
				imageFetcher.runOnUiThread(r);
			}
		}

		@Override
		public boolean equals(Object o) {
			boolean eq = false;
			if (this == o) {
				eq = true;
			} else if (o instanceof ImageFetcherRunnable) {
				eq = imageView.equals(((ImageFetcherRunnable) o).imageView);
			}
			return eq;
		}

		@Override
		public int hashCode() {
			return imageView.hashCode();
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + ": " + imgUrl;
		}
	}

	static class ReadFromCacheRunnable extends ImageFetcherRunnable {

		public ReadFromCacheRunnable(ImageFetcher imageFetcher,
				ImageView imageView, String imgUrl, long submitted) {
			super(imageFetcher, imageView, imgUrl, submitted);
		}

		@Override
		public void run() {
			Bitmap bm = imageFetcher.readCached(imgUrl, imageView);
			if (bm == null) {
				FetchAndCacheRunnable r = new FetchAndCacheRunnable(
						imageFetcher, imageView, imgUrl, submitted);
				imageFetcher.fetchExecutor.execute(r);
			} else {
				attachIfMostRecent(bm);
			}
		}

	}

	static class FetchAndCacheRunnable extends ImageFetcherRunnable {

		public FetchAndCacheRunnable(ImageFetcher imageFetcher,
				ImageView imageView, String imgUrl, long submitted) {
			super(imageFetcher, imageView, imgUrl, submitted);
		}

		@Override
		public void run() {
			Pair<byte[], Pair<Bitmap, BitmapFactory.Options>> bmData = imageFetcher
					.fetchAndDecode(imageView, imgUrl);
			if (bmData != null) {
				imageFetcher.cacheRawImage(imgUrl, bmData.first);
				Bitmap bm = imageFetcher.reshapeAndCache(imgUrl, imageView,
						bmData.second);
				attachIfMostRecent(bm);
			}
		}

	}

	static class SetBitmapRunnable extends ImageFetcherRunnable {

		private final Bitmap bitmap;

		public SetBitmapRunnable(ImageFetcher imageFetcher,
				ImageView imageView, String imgUrl, long submitted,
				Bitmap bitmap) {
			super(imageFetcher, imageView, imgUrl, submitted);
			this.bitmap = bitmap;
		}

		@Override
		public void run() {
			if (imageFetcher.crossFadeMillis > 0) {
				Drawable prevDrawable = imageView.getDrawable();
				if (prevDrawable == null) {
					prevDrawable = new ColorDrawable(TRANSPARENT);
				}
				Drawable nextDrawable = new BitmapDrawable(
						imageView.getResources(), bitmap);
				TransitionDrawable transitionDrawable = new TransitionDrawable(
						new Drawable[] { prevDrawable, nextDrawable });
				imageView.setImageDrawable(transitionDrawable);
				transitionDrawable
						.startTransition(imageFetcher.crossFadeMillis);
			} else {
				imageView.setImageBitmap(bitmap);
			}
			if (imageFetcher.fetchListener != null) {
				imageFetcher.fetchListener.onTaskCompleted(imageView, imgUrl);
			}
		}

	}

}
