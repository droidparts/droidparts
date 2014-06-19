/**
 * Copyright 2014 Alex Yanchenko
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
import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import org.droidparts.concurrent.thread.BackgroundThreadExecutor;
import org.droidparts.contract.HTTP.Header;
import org.droidparts.inner.BitmapFactoryUtils;
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

	protected final ThreadPoolExecutor cacheExecutor;
	protected final ThreadPoolExecutor fetchExecutor;

	private final ImageView mockImageView;

	private final LinkedHashSet<ImageViewSpec> pending = new LinkedHashSet<ImageViewSpec>();
	private final ConcurrentHashMap<ImageViewSpec, Long> wip = new ConcurrentHashMap<ImageViewSpec, Long>();
	private Handler handler;

	private volatile boolean paused;

	public ImageFetcher(Context ctx) {
		this(ctx, BitmapMemoryCache.getDefaultInstance(ctx), BitmapDiskCache
				.getDefaultInstance(ctx));
	}

	public ImageFetcher(Context ctx, BitmapMemoryCache memoryCache,
			BitmapDiskCache diskCache) {
		this(ctx, new BackgroundThreadExecutor(2, "ImageFetcher-Fetch"),
				new RESTClient(ctx), memoryCache, diskCache);
	}

	protected ImageFetcher(Context ctx, ThreadPoolExecutor fetchExecutor,
			RESTClient restClient, BitmapMemoryCache memoryCache,
			BitmapDiskCache diskCache) {
		this.fetchExecutor = fetchExecutor;
		this.restClient = restClient;
		this.memoryCache = memoryCache;
		this.diskCache = diskCache;
		handler = new Handler(Looper.getMainLooper());
		cacheExecutor = new BackgroundThreadExecutor(1, "ImageFetcher-Cache");
		mockImageView = new ImageView(ctx.getApplicationContext());
	}

	public void pause() {
		paused = true;
	}

	public void resume(boolean executePendingTasks) {
		paused = false;
		if (executePendingTasks) {
			for (ImageViewSpec spec : pending) {
				ImageView imgView = spec.imgViewRef.get();
				if (imgView != null) {
					attachImage(spec.imgUrl, imgView, spec.reshaper,
							spec.crossFadeMillis, spec.listener,
							spec.inBitmapRef.get());
				}
			}
		}
		pending.clear();
	}

	//

	public void attachImage(String imgUrl, ImageView imageView) {
		attachImage(imgUrl, imageView, 0);
	}

	public void attachImage(String imgUrl, ImageView imageView,
			int crossFadeMillis) {
		attachImage(imgUrl, imageView, null, crossFadeMillis);
	}

	public void attachImage(String imgUrl, ImageView imageView,
			ImageReshaper reshaper, int crossFadeMillis) {
		attachImage(imgUrl, imageView, reshaper, crossFadeMillis, null);
	}

	public void attachImage(String imgUrl, ImageView imageView,
			ImageReshaper reshaper, int crossFadeMillis,
			ImageFetchListener listener) {
		attachImage(imgUrl, imageView, reshaper, crossFadeMillis, listener,
				null);
	}

	public void attachImage(String imgUrl, ImageView imageView,
			ImageReshaper reshaper, int crossFadeMillis,
			ImageFetchListener listener, Bitmap inBitmap) {
		ImageViewSpec spec = new ImageViewSpec(imageView, imgUrl, inBitmap,
				crossFadeMillis, reshaper, listener);
		long submitted = System.nanoTime();
		wip.put(spec, submitted);
		if (paused) {
			pending.remove(spec);
			pending.add(spec);
		} else {
			if (listener != null) {
				listener.onFetchAdded(imageView, imgUrl);
			}
			Runnable r = new ReadFromCacheRunnable(spec, submitted);
			cacheExecutor.remove(r);
			fetchExecutor.remove(r);
			if (isNotEmpty(imgUrl)) {
				cacheExecutor.execute(r);
			} else {
				if (listener != null) {
					listener.onFetchCompleted(imageView, imgUrl, null);
				}
			}
		}
	}

	public Bitmap getImage(String imgUrl) throws Exception {
		return getImage(imgUrl, mockImageView, null);
	}

	public Bitmap getImage(String imgUrl, ImageView hintImageView,
			ImageReshaper reshaper) throws Exception {
		ImageViewSpec spec = new ImageViewSpec(hintImageView, imgUrl, null, 0,
				reshaper, null);
		Bitmap bm = readCached(spec);
		if (bm == null) {
			Pair<byte[], Pair<Bitmap, BitmapFactory.Options>> bmData = fetchAndDecode(spec);
			cacheRawImage(imgUrl, bmData.first);
			bm = reshapeAndCache(spec, bmData.second);
		}
		return bm;
	}

	//

	public void clearCacheOlderThan(int hours) {
		if (diskCache != null) {
			final long timestamp = System.currentTimeMillis() - hours * 60 * 60
					* 1000;
			cacheExecutor.execute(new Runnable() {

				@Override
				public void run() {
					diskCache.purgeFilesAccessedBefore(timestamp);
				}
			});
		} else {
			L.w("Disk cache not set.");
		}
	}

	//

	Pair<byte[], Pair<Bitmap, BitmapFactory.Options>> fetchAndDecode(
			final ImageViewSpec spec) throws Exception {
		final ImageView imgView = spec.imgViewRef.get();
		if (imgView == null) {
			throw new IllegalStateException("ImageView is null.");
		}
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
							spec.listener.onFetchProgressChanged(imgView,
									spec.imgUrl, kBTotal, kBReceived);
						}
					});
				}
			}
			byte[] data = baos.toByteArray();
			Pair<Bitmap, BitmapFactory.Options> bm = BitmapFactoryUtils
					.decodeScaled(data, spec.widthHint, spec.heightHint,
							spec.configHint, spec.inBitmapRef.get());
			return Pair.create(data, bm);
		} finally {
			silentlyClose(bis, baos);
		}
	}

	Bitmap readCached(ImageViewSpec spec) {
		Bitmap bm = null;
		if (memoryCache != null) {
			bm = memoryCache.get(spec.cacheKey);
		}
		if (bm == null && diskCache != null) {
			Pair<Bitmap, BitmapFactory.Options> bmData = diskCache.get(
					spec.cacheKey, spec.widthHint, spec.heightHint,
					spec.configHint, spec.inBitmapRef.get());
			if (bmData != null) {
				bm = bmData.first;
				if (memoryCache != null) {
					memoryCache.put(spec.cacheKey, bm);
				}
			} else {
				bmData = diskCache.get(spec.imgUrl, spec.widthHint,
						spec.heightHint, spec.configHint,
						spec.inBitmapRef.get());
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

	Bitmap reshapeAndCache(ImageViewSpec spec,
			Pair<Bitmap, BitmapFactory.Options> bmData) {
		Bitmap bm = bmData.first;
		if (spec.reshaper != null) {
			Bitmap reshapedBm = spec.reshaper.reshape(bm);
			if (bm != reshapedBm) {
				bm.recycle();
			}
			bm = reshapedBm;
		}
		if (memoryCache != null) {
			memoryCache.put(spec.cacheKey, bm);
		}
		if (diskCache != null && spec.reshaper != null) {
			Pair<CompressFormat, Integer> cacheFormat = spec.reshaper
					.getCacheFormat(bmData.second.outMimeType);
			diskCache.put(spec.cacheKey, bm, cacheFormat);
		}
		return bm;
	}

	void attachIfMostRecent(ImageViewSpec spec, long submitted, Bitmap bitmap) {
		Long mostRecent = wip.get(spec);
		if (mostRecent != null && submitted == mostRecent) {
			wip.remove(spec);
			if (!paused || !pending.contains(spec)) {
				SetBitmapRunnable r = new SetBitmapRunnable(spec, bitmap);
				runOnUiThread(r);
			}
		}
	}

	void runOnUiThread(Runnable r) {
		boolean success = handler.post(r);
		// a hack
		while (!success) {
			handler = new Handler(Looper.getMainLooper());
			runOnUiThread(r);
		}
	}

	//

	static class ImageViewSpec {

		final WeakReference<ImageView> imgViewRef;
		final String imgUrl;
		final WeakReference<Bitmap> inBitmapRef;
		final int crossFadeMillis;
		final ImageReshaper reshaper;
		final ImageFetchListener listener;

		final String cacheKey;
		final Bitmap.Config configHint;
		final int widthHint;
		final int heightHint;

		private final int imgViewHash;

		public ImageViewSpec(ImageView imgView, String imgUrl, Bitmap inBitmap,
				int crossFadeMillis, ImageReshaper reshaper,
				ImageFetchListener listener) {
			imgViewRef = new WeakReference<ImageView>(imgView);
			this.imgUrl = imgUrl;
			inBitmapRef = new WeakReference<Bitmap>(inBitmap);
			this.crossFadeMillis = crossFadeMillis;
			this.reshaper = reshaper;
			this.listener = listener;
			cacheKey = getCacheKey();
			configHint = getConfigHint();
			Point p = getSizeHint();
			widthHint = p.x;
			heightHint = p.y;
			imgViewHash = imgView.hashCode();
		}

		private String getCacheKey() {
			StringBuilder sb = new StringBuilder();
			sb.append(imgUrl);
			if (reshaper != null) {
				sb.append("-");
				sb.append(reshaper.getCacheId());
			}
			Point p = getSizeHint();
			if (p.x > 0 || p.y > 0) {
				sb.append("-");
				sb.append(p.x);
				sb.append("x");
				sb.append(p.y);
			}
			return sb.toString();
		}

		private Bitmap.Config getConfigHint() {
			return (reshaper != null) ? reshaper.getBitmapConfig() : null;
		}

		private Point getSizeHint() {
			Point p = new Point();
			if (reshaper != null) {
				p.x = reshaper.getImageWidthHint();
				p.y = reshaper.getImageHeightHint();
			}
			if (p.x <= 0 && p.y <= 0) {
				p = BitmapFactoryUtils.calcDecodeSizeHint(imgViewRef.get());
			}
			return p;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (o instanceof ImageViewSpec) {
				return hashCode() == o.hashCode();
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return imgViewHash;
		}

	}

	abstract class ImageViewSpecRunnable implements Runnable {

		final ImageViewSpec spec;
		final long submitted;

		public ImageViewSpecRunnable(ImageViewSpec spec, long submitted) {
			this.spec = spec;
			this.submitted = submitted;
		}

		@Override
		public boolean equals(Object o) {
			boolean eq = false;
			if (this == o) {
				eq = true;
			} else if (o instanceof ImageViewSpecRunnable) {
				eq = spec.equals(((ImageViewSpecRunnable) o).spec);
			}
			return eq;
		}

		@Override
		public int hashCode() {
			return spec.hashCode();
		}

	}

	class ReadFromCacheRunnable extends ImageViewSpecRunnable {

		public ReadFromCacheRunnable(ImageViewSpec spec, long submitted) {
			super(spec, submitted);
		}

		@Override
		public void run() {
			Bitmap bm = readCached(spec);
			if (bm == null) {
				FetchAndCacheRunnable r = new FetchAndCacheRunnable(spec,
						submitted);
				fetchExecutor.execute(r);
			} else {
				attachIfMostRecent(spec, submitted, bm);
			}
		}

	}

	class FetchAndCacheRunnable extends ImageViewSpecRunnable {

		public FetchAndCacheRunnable(ImageViewSpec spec, long submitted) {
			super(spec, submitted);
		}

		@Override
		public void run() {
			try {
				Pair<byte[], Pair<Bitmap, BitmapFactory.Options>> bmData = fetchAndDecode(spec);
				cacheRawImage(spec.imgUrl, bmData.first);
				Bitmap bm = reshapeAndCache(spec, bmData.second);
				attachIfMostRecent(spec, submitted, bm);
			} catch (final Exception e) {
				HTTPWorker.throwIfNetworkOnMainThreadException(e);
				L.w("Failed to fetch '%s'.", spec.imgUrl);
				L.d(e);
				final ImageView imgView = spec.imgViewRef.get();
				if (spec.listener != null && imgView != null) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							spec.listener
									.onFetchFailed(imgView, spec.imgUrl, e);
						}
					});
				}
			}
		}

	}

	class SetBitmapRunnable extends ImageViewSpecRunnable {

		final Bitmap bitmap;

		public SetBitmapRunnable(ImageViewSpec spec, Bitmap bitmap) {
			super(spec, -1);
			this.bitmap = bitmap;
		}

		@Override
		public void run() {
			ImageView imgView = spec.imgViewRef.get();
			if (imgView == null) {
				L.i("ImageView became null (no strong references => GCed).");
			} else {
				if (spec.crossFadeMillis > 0) {
					Drawable prevDrawable = imgView.getDrawable();
					if (prevDrawable == null) {
						prevDrawable = new ColorDrawable(TRANSPARENT);
					}
					Drawable nextDrawable = new BitmapDrawable(
							imgView.getResources(), bitmap);
					TransitionDrawable transitionDrawable = new TransitionDrawable(
							new Drawable[] { prevDrawable, nextDrawable });
					imgView.setImageDrawable(transitionDrawable);
					transitionDrawable.startTransition(spec.crossFadeMillis);
				} else {
					imgView.setImageBitmap(bitmap);
				}
				if (spec.listener != null) {
					spec.listener
							.onFetchCompleted(imgView, spec.imgUrl, bitmap);
				}
			}
		}

	}

}
