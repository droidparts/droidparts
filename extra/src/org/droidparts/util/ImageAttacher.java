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

import static org.droidparts.util.io.IOUtils.silentlyClose;
import static org.droidparts.util.ui.ViewUtils.crossFade;
import static org.droidparts.util.ui.ViewUtils.setInvisible;

import java.io.BufferedInputStream;
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

	public Bitmap getCachedOrFetchAndCache(String fileUrl) throws Exception {

		Bitmap bm = null;
		if (bitmapCacher != null) {
			bm = bitmapCacher.readFromCache(fileUrl);
		}

		if (bm == null) {
			BufferedInputStream bis = null;
			try {
				bis = restClient.getInputStream(fileUrl).second;
				bm = BitmapFactory.decodeStream(bis);
			} finally {
				silentlyClose(bis);
			}

			if (bitmapCacher != null && bm != null) {
				bitmapCacher.saveToCache(fileUrl, bm);
			}
		}
		return bm;
	}

	protected Bitmap onSuccess(ImageView imageView, String url, Bitmap bm) {
		return bm;
	}

	protected void onFailure(ImageView imageView, String url, Exception e) {
		L.w(e);
	}

	protected static BitmapCacher getBitmapCacher(Context ctx) {
		File externalCacheDir = new AppUtils(ctx).getExternalCacheDir();
		return (externalCacheDir != null) ? new BitmapCacher(new File(
				externalCacheDir, "img")) : null;
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
		private final String fileUrl;
		private final long submitted;

		public FetchAndCacheBitmapRunnable(ImageAttacher imageAttacher,
				View placeholderView, ImageView imageView, String fileUrl,
				long submitted) {
			super(placeholderView, imageView);
			this.ia = imageAttacher;
			this.fileUrl = fileUrl;
			this.submitted = submitted;

		}

		@Override
		public void run() {
			Bitmap bm = null;
			try {
				bm = ia.getCachedOrFetchAndCache(fileUrl);
			} catch (Exception e) {
				ia.onFailure(imageView, fileUrl, e);
			}
			if (bm != null && (ia.currWIP.get(imageView) == submitted)) {
				ia.currWIP.remove(imageView);
				bm = ia.onSuccess(imageView, fileUrl, bm);
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
			return getClass().getSimpleName() + ": " + fileUrl;
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