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

import static org.droidparts.util.io.IOUtils.silentlyClose;

import java.io.BufferedInputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.droidparts.net.http.HTTPException;
import org.droidparts.net.http.RESTClient;
import org.droidparts.util.L;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAttacher extends FileFetcher {

	private final ExecutorService exec;
	private final RESTClient client;
	private final FileCacher fileCacher;

	private final ConcurrentHashMap<View, Pair<String, Drawable>> viewsToUrlsAndDefaultImages = new ConcurrentHashMap<View, Pair<String, Drawable>>();

	public ImageAttacher(FileCacher fileCacher) {
		exec = Executors.newSingleThreadExecutor();
		client = new RESTClient(null);
		this.fileCacher = fileCacher;
	}

	public void setCrossfadeDuration(int millisec) {
		// TODO
	}

	public void attachImage(String imgUrlFrom, View viewTo) {
		setImage(viewTo, imgUrlFrom, null);
	}

	@Deprecated
	public void setImage(View view, String fileUrl) {
		setImage(view, fileUrl, null);
	}

	@Deprecated
	public void setImage(View view, String fileUrl, Drawable defaultImg) {
		viewsToUrlsAndDefaultImages.put(view, new Pair<String, Drawable>(
				fileUrl, defaultImg));
		exec.execute(fetchAndAttachRunnable);
	}

	public Drawable getCachedOrFetchAndCache(String fileUrl) {

		BitmapDrawable image = null;
		if (fileCacher != null) {
			image = fileCacher.readFromCache(fileUrl);
		}

		if (image == null) {
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(
						client.getInputStream(fileUrl).second);
				image = new BitmapDrawable(bis);
				image = processFetchedImage(fileUrl, image);
			} catch (HTTPException e) {
				L.e(e);
			} finally {
				silentlyClose(bis);
			}

			if (fileCacher != null && image != null) {
				fileCacher.saveToCache(fileUrl, image);
			}
		}
		return image;
	}

	protected BitmapDrawable processFetchedImage(String url, BitmapDrawable img) {
		return img;
	}

	private final Runnable fetchAndAttachRunnable = new Runnable() {

		@Override
		public void run() {
			for (View view : viewsToUrlsAndDefaultImages.keySet()) {
				Pair<String, Drawable> tuple = viewsToUrlsAndDefaultImages
						.get(view);
				if (tuple != null) {
					viewsToUrlsAndDefaultImages.remove(view);
					String fileUrl = tuple.first;
					Drawable defaultImg = tuple.second;
					Drawable image = getCachedOrFetchAndCache(fileUrl);
					if (image == null) {
						image = defaultImg;
					}
					if (image != null) {
						view.post(new AttachRunnable(view, image));
					}
				}
			}
		}
	};

	private static class AttachRunnable implements Runnable {

		private final View view;
		private final Drawable drawable;

		public AttachRunnable(View view, Drawable drawable) {
			this.view = view;
			this.drawable = drawable;
		}

		@Override
		public void run() {
			if (view instanceof ImageView) {
				ImageView imageView = (ImageView) view;
				imageView.setImageDrawable(drawable);
			} else if (view instanceof TextView) {
				TextView textView = (TextView) view;
				textView.setCompoundDrawablesWithIntrinsicBounds(null,
						drawable, null, null);
			} else {
				L.e("Unsupported type: " + view.getClass());
			}
		}

	}

}