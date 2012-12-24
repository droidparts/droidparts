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

import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.util.io.IOUtils.silentlyClose;
import static org.droidparts.util.ui.ViewUtils.crossFade;
import static org.droidparts.util.ui.ViewUtils.setInvisible;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.droidparts.http.RESTClient;
import org.droidparts.util.io.BitmapCacher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

public class ImageAttacher {

	private final ThreadPoolExecutor executor;
	private final RESTClient restClient;
	private final BitmapCacher bitmapCacher;

	final ConcurrentHashMap<ImageView, Long> currWIP = new ConcurrentHashMap<ImageView, Long>();
	int crossFadeAnimationDuration = 400;
	volatile Handler handler;

	public ImageAttacher(Context ctx) {
		this(ctx, (ThreadPoolExecutor) Executors.newFixedThreadPool(1),
				new RESTClient(ctx));
	}

	public ImageAttacher(Context ctx, ThreadPoolExecutor executor,
			RESTClient restClient) {
		this(executor, restClient, getBitmapCacher(ctx));
	}

	public ImageAttacher(ThreadPoolExecutor executor, RESTClient restClient,
			BitmapCacher bitmapCacher) {
		this.executor = executor;
		this.restClient = restClient;
		this.bitmapCacher = bitmapCacher;
		handler = new Handler(Looper.getMainLooper());
	}

	public void setCrossFadeDuration(int millisec) {
		this.crossFadeAnimationDuration = millisec;
	}

	public void attachImage(ImageView imageView, String imgUrl) {
		addAndExecute(null, imageView, imgUrl);
	}

	public void attachImageCrossFaded(View placeholderView,
			ImageView imageView, String imgUrl) {
		setInvisible(false, placeholderView);
		setInvisible(true, imageView);
		addAndExecute(placeholderView, imageView, imgUrl);
	}

	public Bitmap getCachedOrFetchAndCache(View placeholderView,
			ImageView imageView, String imgUrl) {
		Bitmap bm = null;
		boolean saveToCache = false;

		if (bitmapCacher != null) {
			bm = bitmapCacher.readFromCache(imgUrl);
		}

		if (bm == null) {
			saveToCache = true;
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
					onFetchProgressChanged(placeholderView, imgUrl, kBTotal,
							bytesReadTotal / 1024);
				}
				byte[] data = baos.toByteArray();
				bm = BitmapFactory.decodeByteArray(data, 0, data.length);
			} catch (Exception e) {
				L.d(e);
				onFailure(imageView, imgUrl, e);
			} finally {
				silentlyClose(bis, baos);
			}
		}

		if (bm != null) {
			if (bitmapCacher != null && saveToCache) {
				bitmapCacher.saveToCache(imgUrl, bm);
			}
			bm = onSuccess(imageView, imgUrl, bm);
		}

		return bm;
	}

	protected void onFetchProgressChanged(View backgroundView, String url,
			int kBTotal, int kBReceived) {
		L.d(String.format("Fetched %d of %d kB for %s.", kBReceived, kBTotal,
				url));
	}

	protected void onFailure(ImageView imageView, String url, Exception e) {
		L.w(e);
	}

	protected Bitmap onSuccess(ImageView imageView, String url, Bitmap bm) {
		return bm;
	}

	protected static BitmapCacher getBitmapCacher(Context ctx) {
		File externalCacheDir = new AppUtils(ctx).getExternalCacheDir();
		BitmapCacher bc = null;
		if (externalCacheDir != null) {
			bc = new BitmapCacher(new File(externalCacheDir, "img"));
		} else {
			L.w("External cache dir null. Forgot 'android.permission.WRITE_EXTERNAL_STORAGE' permission?");
		}
		return bc;
	}

	private void addAndExecute(View placeholderView, ImageView view, String url) {
		long time = System.nanoTime();
		currWIP.put(view, time);
		Runnable r = new FetchAndCacheBitmapRunnable(this, placeholderView,
				view, url, time);
		executor.remove(r);
		executor.execute(r);
	}

	//

	static abstract class ImageViewWorkerRunnable implements Runnable {

		protected final View placeholderView;
		protected final ImageView imageView;

		public ImageViewWorkerRunnable(View placeholderView, ImageView imageView) {
			this.placeholderView = placeholderView;
			this.imageView = imageView;
		}

		@Override
		public boolean equals(Object o) {
			boolean eq = false;
			if (this == o) {
				eq = true;
			} else if (o instanceof ImageViewWorkerRunnable) {
				eq = imageView.equals(((ImageViewWorkerRunnable) o).imageView);
			}
			return eq;
		}

		@Override
		public int hashCode() {
			return imageView.hashCode();
		}
	}

	static class FetchAndCacheBitmapRunnable extends ImageViewWorkerRunnable {

		private final ImageAttacher ia;
		private final String imgUrl;
		private final long submitted;

		public FetchAndCacheBitmapRunnable(ImageAttacher imageAttacher,
				View placeholderView, ImageView imageView, String imgUrl,
				long submitted) {
			super(placeholderView, imageView);
			this.ia = imageAttacher;
			this.imgUrl = imgUrl;
			this.submitted = submitted;

		}

		@Override
		public void run() {
			Bitmap bm = ia.getCachedOrFetchAndCache(placeholderView, imageView,
					imgUrl);
			if (bm != null && (ia.currWIP.get(imageView) == submitted)) {
				ia.currWIP.remove(imageView);
				AttachBitmapRunnable r = new AttachBitmapRunnable(
						placeholderView, imageView, bm,
						ia.crossFadeAnimationDuration);
				boolean success = ia.handler.post(r);
				// a hack
				while (!success) {
					ia.handler = new Handler(Looper.getMainLooper());
					success = ia.handler.post(r);
				}
			}
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + ": " + imgUrl;
		}

	}

	static class AttachBitmapRunnable extends ImageViewWorkerRunnable {

		private final Bitmap bitmap;
		private final int crossFadeAnimationDuration;

		public AttachBitmapRunnable(View placeholderView, ImageView imageView,
				Bitmap bitmap, int crossFadeAnimationDuration) {
			super(placeholderView, imageView);
			this.bitmap = bitmap;
			this.crossFadeAnimationDuration = crossFadeAnimationDuration;
		}

		@Override
		public void run() {
			imageView.setImageBitmap(bitmap);
			if (placeholderView != null) {
				if (crossFadeAnimationDuration > 0) {
					crossFade(placeholderView, imageView,
							crossFadeAnimationDuration, null);
				} else {
					setInvisible(false, imageView);
					setInvisible(true, placeholderView);
				}
			}
		}

	}

}