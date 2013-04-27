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
	final ThreadPoolExecutor fetchExecutor;

	private final LinkedHashMap<ImageView, Bean> todo = new LinkedHashMap<ImageView, Bean>();
	private final ConcurrentHashMap<ImageView, Long> wip = new ConcurrentHashMap<ImageView, Long>();
	private Handler handler;

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

	public void pause() {
		paused = true;
	}

	public void resume(boolean executePendingTasks) {
		paused = false;
		if (executePendingTasks) {
			for (ImageView iv : todo.keySet()) {
				Bean ib = todo.get(iv);
				attachImage(iv, ib.imgUrl, ib.options, ib.listener);
			}
		}
		todo.clear();
	}

	//

	public void attachImage(ImageView imageView, String imgUrl) {
		attachImage(imageView, imgUrl, null);
	}

	public void attachImage(ImageView imageView, String imgUrl,
			ImageFetchOptions options) {
		attachImage(imageView, imgUrl, options, null);
	}

	public void attachImage(ImageView imageView, String imgUrl,
			ImageFetchOptions options, ImageFetchListener listener) {
		long submitted = System.nanoTime();
		wip.put(imageView, submitted);
		Bean bean = new Bean(imageView, imgUrl, options, listener);
		if (paused) {
			todo.remove(imageView);
			todo.put(imageView, bean);
		} else {
			if (listener != null) {
				listener.onTaskAdded(imageView, imgUrl);
			}
			Runnable r = new ReadFromCacheRunnable(this, bean, submitted);
			cacheExecutor.remove(r);
			fetchExecutor.remove(r);
			if (isNotEmpty(imgUrl)) {
				cacheExecutor.execute(r);
			} else {
				if (listener != null) {
					listener.onTaskCompleted(imageView, imgUrl, null);
				}
			}
		}
	}

	public Bitmap getImage(ImageView imageView, String imgUrl,
			ImageFetchOptions options, ImageFetchListener listener) {
		Bean bean = new Bean(imageView, imgUrl, options, listener);
		Bitmap bm = readCached(bean);
		if (bm == null) {
			Pair<byte[], Pair<Bitmap, BitmapFactory.Options>> bmData = fetchAndDecode(bean);
			if (bmData != null) {
				cacheRawImage(imgUrl, bmData.first);
				bm = reshapeAndCache(bean, bmData.second);
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
			final Bean bean) {
		Pair<byte[], Pair<Bitmap, BitmapFactory.Options>> bmData = null;
		int bytesReadTotal = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			HTTPResponse resp = restClient.getInputStream(bean.imgUrl);
			final int kBTotal = resp.getHeaderInt(Header.CONTENT_LENGTH) / 1024;
			bis = resp.inputStream;
			int bytesRead;
			while ((bytesRead = bis.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
				bytesReadTotal += bytesRead;
				if (bean.listener != null) {
					final int kBReceived = bytesReadTotal / 1024;
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							bean.listener.onDownloadProgressChanged(
									bean.imgView, bean.imgUrl, kBTotal,
									kBReceived);
						}
					});
				}
			}
			byte[] data = baos.toByteArray();
			Point p = bean.getSizeHint();
			Pair<Bitmap, BitmapFactory.Options> bm = BitmapFactoryUtil
					.decodeScaled(new ByteArrayInputStream(data),
							bean.getConfigHint(), p.x, p.y);
			if (bm != null) {
				bmData = Pair.create(data, bm);
			}
		} catch (final Exception e) {
			HTTPWorker.throwIfNetworkOnMainThreadException(e);
			L.w("Failed to fetch %s.", bean.imgUrl);
			L.d(e);
			if (bean.listener != null) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						bean.listener.onDownloadFailed(bean.imgView,
								bean.imgUrl, e);
					}
				});
			}
		} finally {
			silentlyClose(bis, baos);
		}
		return bmData;
	}

	protected Bitmap readCached(Bean bean) {
		Bitmap bm = null;
		Point p = bean.getSizeHint();
		String key = bean.getCacheKey();
		if (memoryCache != null) {
			bm = memoryCache.get(key);
		}
		if (bm == null) {
			Pair<Bitmap, BitmapFactory.Options> bmData = diskCache.get(key,
					bean.getConfigHint(), p.x, p.y);
			if (bmData != null) {
				bm = bmData.first;
				if (memoryCache != null) {
					memoryCache.put(key, bm);
				}
			} else {
				bmData = diskCache.get(bean.imgUrl, bean.getConfigHint(), p.x,
						p.y);
				if (bmData != null) {
					bm = reshapeAndCache(bean, bmData);
				}
			}
		}
		return bm;
	}

	void cacheRawImage(String imgUrl, byte[] data) {
		if (diskCache != null) {
			diskCache.put(imgUrl, data);
		}
	}

	Bitmap reshapeAndCache(Bean bean, Pair<Bitmap, BitmapFactory.Options> bmData) {
		Bitmap bm = bmData.first;
		if (bean.options != null && bean.options.getCacheId() != null) {
			Bitmap reshapedBm = bean.options.reshape(bm);
			if (bm != reshapedBm) {
				bm.recycle();
			}
			bm = reshapedBm;
		}
		String key = bean.getCacheKey();
		if (memoryCache != null) {
			memoryCache.put(key, bm);
		}
		if (diskCache != null && bean.options != null) {
			Pair<CompressFormat, Integer> cacheFormat = bean.options
					.getCacheFormat(bmData.second.outMimeType);
			if (cacheFormat != null) {
				diskCache.put(key, bm, cacheFormat);
			}
		}
		return bm;
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

	static class Bean {

		public final ImageView imgView;
		public final String imgUrl;
		public final ImageFetchOptions options;
		public final ImageFetchListener listener;

		public Bean(ImageView imgView, String imgUrl,
				ImageFetchOptions options, ImageFetchListener listener) {
			this.imgView = imgView;
			this.imgUrl = imgUrl;
			this.options = options;
			this.listener = listener;
		}

		public String getCacheKey() {
			Point p = getSizeHint();
			StringBuilder sb = new StringBuilder(5);
			sb.append(imgUrl);
			Bitmap.Config configHint = getConfigHint();
			if (configHint != null) {
				sb.append(configHint);
			}
			if (p.x > 0 || p.y > 0) {
				sb.append(p.x);
				sb.append(p.y);
			}
			if (options != null) {
				String cacheId = options.getCacheId();
				if (cacheId != null) {
					sb.append(cacheId);
				}
			}
			return sb.toString();
		}

		public Bitmap.Config getConfigHint() {
			return (options != null) ? options.getConfigHint() : null;
		}

		public Point getSizeHint() {
			Point p = new Point();
			if (options != null) {
				p.x = options.getWidthHint();
				p.y = options.getHeightHint();
			}
			if (p.x <= 0 && p.y <= 0) {
				p = BitmapFactoryUtil.calcDecodeSizeHint(imgView);
			}
			return p;
		}
	}

	abstract static class ImageFetcherRunnable implements Runnable {

		protected final ImageFetcher imageFetcher;
		protected final Bean bean;
		protected final long submitted;
		protected final ImageView imageView;

		public ImageFetcherRunnable(ImageFetcher imageFetcher, Bean bean,
				long submitted) {
			this.imageFetcher = imageFetcher;
			this.bean = bean;
			this.submitted = submitted;
			imageView = bean.imgView;
		}

		protected final void attachIfMostRecent(Bitmap bitmap) {

			Long mostRecent = imageFetcher.wip.get(imageView);
			if (mostRecent != null && submitted == mostRecent) {
				imageFetcher.wip.remove(imageView);
				SetBitmapRunnable r = new SetBitmapRunnable(imageFetcher, bean,
						submitted, bitmap);
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
			return getClass().getSimpleName() + ": " + bean.imgUrl;
		}
	}

	static class ReadFromCacheRunnable extends ImageFetcherRunnable {

		public ReadFromCacheRunnable(ImageFetcher imageFetcher, Bean bean,
				long submitted) {
			super(imageFetcher, bean, submitted);
		}

		@Override
		public void run() {
			Bitmap bm = imageFetcher.readCached(bean);
			if (bm == null) {
				FetchAndCacheRunnable r = new FetchAndCacheRunnable(
						imageFetcher, bean, submitted);
				imageFetcher.fetchExecutor.execute(r);
			} else {
				attachIfMostRecent(bm);
			}
		}

	}

	static class FetchAndCacheRunnable extends ImageFetcherRunnable {

		public FetchAndCacheRunnable(ImageFetcher imageFetcher, Bean bean,
				long submitted) {
			super(imageFetcher, bean, submitted);
		}

		@Override
		public void run() {
			Pair<byte[], Pair<Bitmap, BitmapFactory.Options>> bmData = imageFetcher
					.fetchAndDecode(bean);
			if (bmData != null) {
				imageFetcher.cacheRawImage(bean.imgUrl, bmData.first);
				Bitmap bm = imageFetcher.reshapeAndCache(bean, bmData.second);
				attachIfMostRecent(bm);
			}
		}

	}

	static class SetBitmapRunnable extends ImageFetcherRunnable {

		private final Bitmap bitmap;

		public SetBitmapRunnable(ImageFetcher imageFetcher, Bean bean,
				long submitted, Bitmap bitmap) {
			super(imageFetcher, bean, submitted);
			this.bitmap = bitmap;
		}

		@Override
		public void run() {
			int crossFadeMillis = (bean.options != null) ? bean.options
					.getCrossFadeMillis() : 0;
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
			if (bean.listener != null) {
				bean.listener.onTaskCompleted(imageView, bean.imgUrl, bitmap);
			}
		}

	}

}
