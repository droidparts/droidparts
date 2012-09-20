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
import static org.droidparts.util.ui.ViewUtils.setInvisible;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.droidparts.http.RESTClient;
import org.droidparts.util.io.BitmapCacher;
import org.droidparts.util.ui.ViewUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

public class ImageAttacher {

	private final BitmapCacher bitmapCacher;
	private final ExecutorService executorService;
	private final RESTClient restClient;
	private Handler handler;

	private final ConcurrentHashMap<ImageView, Pair<String, View>> data = new ConcurrentHashMap<ImageView, Pair<String, View>>();

	private int crossFadeAnimationDuration = 400;

	public ImageAttacher(Context ctx) {
		this(ctx, Executors.newSingleThreadExecutor(),
				new RESTClient(ctx, null));
	}

	public ImageAttacher(Context ctx, ExecutorService executorService,
			RESTClient restClient) {
		this(
				new AppUtils(ctx).getExternalCacheDir() != null ? new BitmapCacher(
						new File(new AppUtils(ctx).getExternalCacheDir(), "img"))
						: null, executorService, restClient);
	}

	public ImageAttacher(BitmapCacher bitmapCacher,
			ExecutorService executorService, RESTClient restClient) {
		this.bitmapCacher = bitmapCacher;
		this.executorService = executorService;
		this.restClient = restClient;
		handler = new Handler(Looper.getMainLooper());
	}

	public void setCrossFadeDuration(int millisec) {
		this.crossFadeAnimationDuration = millisec;
	}

	public void attachImage(ImageView imageView, String imgUrl) {
		addAndExecute(imageView, new Pair<String, View>(imgUrl, null));
	}

	public void attachImageCrossFaded(View placeholderView,
			ImageView imageView, String imgUrl) {
		setInvisible(placeholderView, false);
		setInvisible(imageView, true);
		addAndExecute(imageView,
				new Pair<String, View>(imgUrl, placeholderView));
	}

	protected Bitmap onSuccess(ImageView imageView, String url, Bitmap bm) {
		return bm;
	}

	protected void onFailure(ImageView imageView, String url, Exception e) {
		L.e(e);
	}

	private void addAndExecute(ImageView view, Pair<String, View> pair) {
		data.put(view, pair);
		executorService.execute(fetchAndAttachRunnable);
	}

	public Bitmap getCachedOrFetchAndCache(String fileUrl) throws Exception {

		Bitmap bm = null;
		if (bitmapCacher != null) {
			bm = bitmapCacher.readFromCache(fileUrl);
		}

		if (bm == null) {
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(
						restClient.getInputStream(fileUrl).second);
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

	private final Runnable fetchAndAttachRunnable = new Runnable() {

		@Override
		public void run() {
			for (ImageView imageView : data.keySet()) {
				Pair<String, View> pair = data.get(imageView);
				data.remove(imageView);
				if (pair != null) {
					String fileUrl = pair.first;
					View placeholderView = pair.second;
					Bitmap bm = null;
					try {
						bm = getCachedOrFetchAndCache(fileUrl);
					} catch (Exception e) {
						onFailure(imageView, fileUrl, e);
					}
					if (bm != null && !data.containsKey(imageView)) {
						bm = onSuccess(imageView, fileUrl, bm);
						AttachRunnable r = new AttachRunnable(placeholderView,
								imageView, bm);
						boolean success = handler.post(r);
						if (!success) {
							handler = new Handler(Looper.getMainLooper());
							success = handler.post(r);
						}
					}
				}
			}
		}
	};

	private class AttachRunnable implements Runnable {

		private final ImageView imageView;
		private final Bitmap bitmap;
		private final View placeholderView;

		public AttachRunnable(View placeholderView, ImageView imageView,
				Bitmap bitmap) {
			this.placeholderView = placeholderView;
			this.imageView = imageView;
			this.bitmap = bitmap;
		}

		@Override
		public void run() {
			imageView.setImageBitmap(bitmap);
			if (placeholderView != null) {
				if (crossFadeAnimationDuration > 0) {
					ViewUtils.crossFade(placeholderView, imageView,
							crossFadeAnimationDuration, null);
				} else {
					setInvisible(imageView, false);
					setInvisible(placeholderView, true);
				}
			}
		}

	}

}