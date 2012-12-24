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
import org.droidparts.util.io.BitmapCacher;

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

	public interface Reshaper {

		String getId();

		Bitmap reshape(Bitmap bm);

	}

	private final ThreadPoolExecutor executor;
	private final RESTClient restClient;
	private final BitmapCacher bitmapCacher;

	private Reshaper reshaper;
	int crossFadeMillis = 0;

	final ConcurrentHashMap<ImageView, Long> currWIP = new ConcurrentHashMap<ImageView, Long>();
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
		this.crossFadeMillis = millisec;
	}

	public void setReshaper(Reshaper reshaper) {
		this.reshaper = reshaper;
	}

	//

	public void attachImage(ImageView imageView, String imgUrl) {
		long time = System.nanoTime();
		currWIP.put(imageView, time);
		Runnable r = new FetchAndCacheBitmapRunnable(this, imageView, imgUrl,
				time);
		executor.remove(r);
		executor.execute(r);
	}

	public Bitmap getImage(String imgUrl) {
		return getCachedOrFetchAndCache(null, imgUrl);
	}

	//

	protected void onFetchProgressChanged(View imageView, String imgUrl,
			int kBTotal, int kBReceived) {
		L.d(String.format("Fetched %d of %d kB from %s.", kBReceived, kBTotal,
				imgUrl));
	}

	protected void onFetchFailed(View imageView, String imgUrl, Exception e) {
		L.w("Failed to fetch " + imgUrl);
	}

	//

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

	private Bitmap getCachedOrFetchAndCache(ImageView imageView, String imgUrl) {
		Bitmap bm = null;
		boolean saveToCache = false;
		boolean reshape = true;
		if (bitmapCacher != null) {
			if (reshaper != null) {
				bm = bitmapCacher.readFromCache(imgUrl + reshaper.getId());
			}
			if (bm != null) {
				reshape = false;
			} else {
				bm = bitmapCacher.readFromCache(imgUrl);
			}
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
					onFetchProgressChanged(imageView, imgUrl, kBTotal,
							bytesReadTotal / 1024);
				}
				byte[] data = baos.toByteArray();
				bm = BitmapFactory.decodeByteArray(data, 0, data.length);
			} catch (Exception e) {
				onFetchFailed(imageView, imgUrl, e);
				L.d(e);
			} finally {
				silentlyClose(bis, baos);
			}
		}

		if (bm != null) {
			if (bitmapCacher != null && saveToCache) {
				if (reshaper != null && reshape) {
					imgUrl += reshaper.getId();
					bm = reshaper.reshape(bm);
				}
				bitmapCacher.saveToCache(imgUrl, bm);
			}
		}

		return bm;
	}

	//

	static abstract class ImageViewWorkerRunnable implements Runnable {

		protected final ImageView imageView;

		public ImageViewWorkerRunnable(ImageView imageView) {
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
				ImageView imageView, String imgUrl, long submitted) {
			super(imageView);
			this.ia = imageAttacher;
			this.imgUrl = imgUrl;
			this.submitted = submitted;

		}

		@Override
		public void run() {
			Bitmap bm = ia.getCachedOrFetchAndCache(imageView, imgUrl);
			if (bm != null && (ia.currWIP.get(imageView) == submitted)) {
				ia.currWIP.remove(imageView);
				AttachBitmapRunnable r = new AttachBitmapRunnable(imageView,
						bm, ia.crossFadeMillis);
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
		private final int crossFadeMillis;

		public AttachBitmapRunnable(ImageView imageView, Bitmap bitmap,
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