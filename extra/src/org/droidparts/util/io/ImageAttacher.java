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
package org.droidparts.util.io;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.droidparts.util.io.IOUtils.silentlyClose;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.droidparts.net.http.HTTPException;
import org.droidparts.net.http.RESTClient;
import org.droidparts.util.AppUtils;
import org.droidparts.util.L;
import org.droidparts.util.ui.ViewUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
		placeholderView.setVisibility(VISIBLE);
		imageView.setVisibility(INVISIBLE);
		addAndExecute(imageView,
				new Pair<String, View>(imgUrl, placeholderView));
	}

	private void addAndExecute(ImageView view, Pair<String, View> pair) {
		data.put(view, pair);
		executorService.execute(fetchAndAttachRunnable);
	}

	public Bitmap getCachedOrFetchAndCache(String fileUrl) {

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
			} catch (HTTPException e) {
				L.e(e);
			} finally {
				silentlyClose(bis);
			}

			if (bitmapCacher != null && bm != null) {
				bitmapCacher.saveToCache(fileUrl, bm);
			}
		}
		return bm;
	}

	protected Bitmap processBitmapBeforeAttaching(ImageView imageView,
			String url, Bitmap bm) {
		return bm;
	}

	private final Runnable fetchAndAttachRunnable = new Runnable() {

		@Override
		public void run() {
			for (ImageView view : data.keySet()) {
				Pair<String, View> pair = data.get(view);
				if (pair != null) {
					String fileUrl = pair.first;
					View placeholderView = pair.second;
					data.remove(view);
					Bitmap bm = getCachedOrFetchAndCache(fileUrl);
					if (bm != null) {
						bm = processBitmapBeforeAttaching(view, fileUrl, bm);
						AttachRunnable r = new AttachRunnable(placeholderView,
								view, bm);
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
				ViewUtils.crossFade(placeholderView, imageView,
						crossFadeAnimationDuration);
			}
		}

	}

	//

	@Deprecated
	public void setImage(View view, String fileUrl) {
		attachImage((ImageView) view, fileUrl);
	}

	@Deprecated
	public void setImage(View view, String fileUrl, Drawable defaultImg) {
		attachImage((ImageView) view, fileUrl);
	}

}