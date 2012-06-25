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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.droidparts.net.http.HTTPException;
import org.droidparts.net.http.RESTClient;
import org.droidparts.util.L;
import org.droidparts.util.ui.ViewUtils;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

public class ImageAttacher extends FileFetcher {

	private final ExecutorService exec;
	private final RESTClient client;
	private final FileCacher fileCacher;

	private final ConcurrentHashMap<ImageView, Pair<String, View>> map = new ConcurrentHashMap<ImageView, Pair<String, View>>();

	private int crossFadeAnimationDuration = 400;

	public ImageAttacher(FileCacher fileCacher) {
		this(fileCacher, 1);
	}

	public ImageAttacher(FileCacher fileCacher, int nThreads) {
		if (nThreads == 1) {
			exec = Executors.newSingleThreadExecutor();
		} else {
			exec = Executors.newFixedThreadPool(nThreads);
		}
		client = new RESTClient(null);
		this.fileCacher = fileCacher;
	}

	public void setCrossFadeDuration(int millisec) {
		this.crossFadeAnimationDuration = millisec;
	}

	public void attachImage(ImageView view, String imgUrl) {
		addAndExecute(view, new Pair<String, View>(imgUrl, null));
	}

	public void attachImageCrossFaded(ImageView view, String imgUrl,
			View placeholderView) {
		placeholderView.setVisibility(VISIBLE);
		view.setVisibility(INVISIBLE);
		addAndExecute(view, new Pair<String, View>(imgUrl, placeholderView));
	}

	private void addAndExecute(ImageView view, Pair<String, View> pair) {
		map.put(view, pair);
		exec.execute(fetchAndAttachRunnable);
	}

	public BitmapDrawable getCachedOrFetchAndCache(String fileUrl) {

		BitmapDrawable image = null;
		if (fileCacher != null) {
			image = fileCacher.readBitmapFromCache(fileUrl);
		}

		if (image == null) {
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(
						client.getInputStream(fileUrl).second);
				image = new BitmapDrawable(bis);
			} catch (HTTPException e) {
				L.e(e);
			} finally {
				silentlyClose(bis);
			}

			if (fileCacher != null && image != null) {
				fileCacher.saveBitmapToCache(fileUrl, image);
			}
		}
		return image;
	}

	protected BitmapDrawable processBeforeAttaching(String url, ImageView view,
			BitmapDrawable img) {
		// TODO scale by default
		return img;
	}

	private final Runnable fetchAndAttachRunnable = new Runnable() {

		@Override
		public void run() {
			for (ImageView view : map.keySet()) {
				Pair<String, View> pair = map.get(view);
				if (pair != null) {
					String fileUrl = pair.first;
					View placeholderView = pair.second;
					map.remove(view);
					BitmapDrawable img = getCachedOrFetchAndCache(fileUrl);
					img = processBeforeAttaching(fileUrl, view, img);
					if (img != null) {
						Activity activity = (Activity) view.getContext();
						activity.runOnUiThread(new AttachRunnable(view, img,
								placeholderView));
					}
				}
			}
		}
	};

	private class AttachRunnable implements Runnable {

		private final ImageView imageView;
		private final Drawable drawable;
		private final View placeholderView;

		public AttachRunnable(ImageView imageView, Drawable drawable,
				View placeholderView) {
			this.imageView = imageView;
			this.drawable = drawable;
			this.placeholderView = placeholderView;
		}

		@Override
		public void run() {
			imageView.setImageDrawable(drawable);
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