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

	private final LinkedHashMap<ImageView, Spec> todo = new LinkedHashMap<ImageView, Spec>();
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
				Spec ib = todo.get(iv);
				attachImage(iv, ib.imgUrl, ib.crossFadeMillis, ib.reshaper,
						ib.listener);
			}
		}
		todo.clear();
	}

	//

	public void attachImage(ImageView imageView, String imgUrl) {
		attachImage(imageView, imgUrl, 0);
	}

	public void attachImage(ImageView imageView, String imgUrl,
			int crossFadeMillis) {
		attachImage(imageView, imgUrl, crossFadeMillis, null);
	}

	public void attachImage(ImageView imageView, String imgUrl,
			int crossFadeMillis, ImageReshaper reshaper) {
		attachImage(imageView, imgUrl, crossFadeMillis, reshaper, null);
	}

	public void attachImage(ImageView imageView, String imgUrl,
			int crossFadeMillis, ImageReshaper reshaper,
			ImageFetchListener listener) {
		long submitted = System.nanoTime();
		wip.put(imageView, submitted);
		Spec spec = new Spec(imageView, imgUrl, crossFadeMillis, reshaper,
				listener);
		if (paused) {
			todo.remove(imageView);
			todo.put(imageView, spec);
		} else {
			if (listener != null) {
				listener.onTaskAdded(imageView, imgUrl);
			}
			Runnable r = new ReadFromCacheRunnable(this, spec, submitted);
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
			ImageReshaper config, ImageFetchListener listener) {
		Spec spec = new Spec(imageView, imgUrl, 0, config, listener);
		Bitmap bm = readCached(spec);
		if (bm == null) {
			Pair<byte[], Pair<Bitmap, BitmapFactory.Options>> bmData = fetchAndDecode(spec);
			if (bmData != null) {
				cacheRawImage(imgUrl, bmData.first);
				bm = reshapeAndCache(spec, bmData.second);
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
			final Spec spec) {
		Pair<byte[], Pair<Bitmap, BitmapFactory.Options>> bmData = null;
		int bytesReadTotal = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			HTTPResponse resp = restClient.getInputStream(spec.imgUrl);
			final int kBTotal = resp.getHeaderInt(Header.CONTENT_LENGTH) / 1024;
			bis = resp.inputStream;
			int bytesRead;
			while ((bytesRead = bis.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
				bytesReadTotal += bytesRead;
				if (spec.listener != null) {
					final int kBReceived = bytesReadTotal / 1024;
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							spec.listener.onDownloadProgressChanged(
									spec.imgView, spec.imgUrl, kBTotal,
									kBReceived);
						}
					});
				}
			}
			byte[] data = baos.toByteArray();
			Point p = spec.getSizeHint();
			Pair<Bitmap, BitmapFactory.Options> bm = BitmapUtils.decodeScaled(
					new ByteArrayInputStream(data), p.x, p.y,
					spec.getConfigHint());
			bmData = Pair.create(data, bm);
		} catch (final Exception e) {
			HTTPWorker.throwIfNetworkOnMainThreadException(e);
			L.w("Failed to fetch %s.", spec.imgUrl);
			L.d(e);
			if (spec.listener != null) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						spec.listener.onDownloadFailed(spec.imgView,
								spec.imgUrl, e);
					}
				});
			}
		} finally {
			silentlyClose(bis, baos);
		}
		return bmData;
	}

	protected Bitmap readCached(Spec spec) {
		Bitmap bm = null;
		Point p = spec.getSizeHint();
		String key = spec.getCacheKey();
		if (memoryCache != null) {
			bm = memoryCache.get(key);
		}
		if (bm == null) {
			Pair<Bitmap, BitmapFactory.Options> bmData = diskCache.get(key,
					p.x, p.y, spec.getConfigHint());
			if (bmData != null) {
				bm = bmData.first;
				if (memoryCache != null) {
					memoryCache.put(key, bm);
				}
			} else {
				bmData = diskCache.get(spec.imgUrl, p.x, p.y,
						spec.getConfigHint());
				if (bmData != null) {
					bm = reshapeAndCache(spec, bmData);
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

	Bitmap reshapeAndCache(Spec spec, Pair<Bitmap, BitmapFactory.Options> bmData) {
		Bitmap bm = bmData.first;
		if (spec.reshaper != null) {
			Bitmap reshapedBm = spec.reshaper.reshape(bm);
			if (bm != reshapedBm) {
				bm.recycle();
			}
			bm = reshapedBm;
		}
		String key = spec.getCacheKey();
		if (memoryCache != null) {
			memoryCache.put(key, bm);
		}
		if (diskCache != null && spec.reshaper != null) {
			Pair<CompressFormat, Integer> cacheFormat = spec.reshaper
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

	static class Spec {

		public final ImageView imgView;
		public final String imgUrl;
		public final int crossFadeMillis;
		public final ImageReshaper reshaper;
		public final ImageFetchListener listener;

		public Spec(ImageView imgView, String imgUrl, int crossFadeMillis,
				ImageReshaper reshaper, ImageFetchListener listener) {
			this.imgView = imgView;
			this.imgUrl = imgUrl;
			this.crossFadeMillis = crossFadeMillis;
			this.reshaper = reshaper;
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
			if (reshaper != null) {
				String cacheId = reshaper.getCacheId();
				if (cacheId != null) {
					sb.append(cacheId);
				}
			}
			return sb.toString();
		}

		public Bitmap.Config getConfigHint() {
			return (reshaper != null) ? reshaper.getBitmapConfig() : null;
		}

		public Point getSizeHint() {
			Point p = new Point();
			if (reshaper != null) {
				p.x = reshaper.getBitmapWidthHint();
				p.y = reshaper.getBitmapHeightHint();
			}
			if (p.x <= 0 && p.y <= 0) {
				p = BitmapUtils.calcDecodeSizeHint(imgView);
			}
			return p;
		}
	}

	abstract static class ImageFetcherRunnable implements Runnable {

		protected final ImageFetcher imageFetcher;
		protected final Spec spec;
		protected final long submitted;

		public ImageFetcherRunnable(ImageFetcher imageFetcher, Spec spec,
				long submitted) {
			this.imageFetcher = imageFetcher;
			this.spec = spec;
			this.submitted = submitted;
		}

		protected final void attachIfMostRecent(Bitmap bitmap) {

			Long mostRecent = imageFetcher.wip.get(spec.imgView);
			if (mostRecent != null && submitted == mostRecent) {
				imageFetcher.wip.remove(spec.imgView);
				SetBitmapRunnable r = new SetBitmapRunnable(imageFetcher, spec,
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
				eq = spec.imgView
						.equals(((ImageFetcherRunnable) o).spec.imgView);
			}
			return eq;
		}

		@Override
		public int hashCode() {
			return spec.imgView.hashCode();
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + ": " + spec.imgUrl;
		}
	}

	static class ReadFromCacheRunnable extends ImageFetcherRunnable {

		public ReadFromCacheRunnable(ImageFetcher imageFetcher, Spec spec,
				long submitted) {
			super(imageFetcher, spec, submitted);
		}

		@Override
		public void run() {
			Bitmap bm = imageFetcher.readCached(spec);
			if (bm == null) {
				FetchAndCacheRunnable r = new FetchAndCacheRunnable(
						imageFetcher, spec, submitted);
				imageFetcher.fetchExecutor.execute(r);
			} else {
				attachIfMostRecent(bm);
			}
		}

	}

	static class FetchAndCacheRunnable extends ImageFetcherRunnable {

		public FetchAndCacheRunnable(ImageFetcher imageFetcher, Spec spec,
				long submitted) {
			super(imageFetcher, spec, submitted);
		}

		@Override
		public void run() {
			Pair<byte[], Pair<Bitmap, BitmapFactory.Options>> bmData = imageFetcher
					.fetchAndDecode(spec);
			if (bmData != null) {
				imageFetcher.cacheRawImage(spec.imgUrl, bmData.first);
				Bitmap bm = imageFetcher.reshapeAndCache(spec, bmData.second);
				attachIfMostRecent(bm);
			}
		}

	}

	static class SetBitmapRunnable extends ImageFetcherRunnable {

		private final Bitmap bitmap;

		public SetBitmapRunnable(ImageFetcher imageFetcher, Spec spec,
				long submitted, Bitmap bitmap) {
			super(imageFetcher, spec, submitted);
			this.bitmap = bitmap;
		}

		@Override
		public void run() {
			if (spec.crossFadeMillis > 0) {
				Drawable prevDrawable = spec.imgView.getDrawable();
				if (prevDrawable == null) {
					prevDrawable = new ColorDrawable(TRANSPARENT);
				}
				Drawable nextDrawable = new BitmapDrawable(
						spec.imgView.getResources(), bitmap);
				TransitionDrawable transitionDrawable = new TransitionDrawable(
						new Drawable[] { prevDrawable, nextDrawable });
				spec.imgView.setImageDrawable(transitionDrawable);
				transitionDrawable.startTransition(spec.crossFadeMillis);
			} else {
				spec.imgView.setImageBitmap(bitmap);
			}
			if (spec.listener != null) {
				spec.listener
						.onTaskCompleted(spec.imgView, spec.imgUrl, bitmap);
			}
		}

	}

}
