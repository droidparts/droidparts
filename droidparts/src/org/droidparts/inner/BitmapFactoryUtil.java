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
package org.droidparts.inner;

import java.io.IOException;
import java.io.InputStream;

import org.droidparts.util.L;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class BitmapFactoryUtil {

	public static Point calcDecodeSizeHint(ImageView imageView) {
		Point p = new Point();
		LayoutParams params = imageView.getLayoutParams();
		if (params.width != LayoutParams.WRAP_CONTENT) {
			p.x = imageView.getWidth();
		}
		if (params.height != LayoutParams.WRAP_CONTENT) {
			p.y = imageView.getHeight();
		}
		if (p.x == 0 || p.y == 0) {
			DisplayMetrics metrics = imageView.getContext().getResources()
					.getDisplayMetrics();
			if (p.x == 0) {
				p.x = metrics.widthPixels;
			}
			if (p.y == 0) {
				p.y = metrics.heightPixels;
			}
		}
		return p;
	}

	public static Pair<Bitmap, BitmapFactory.Options> decodeScaled(
			InputStream is, int reqWidth, int reqHeight, Bitmap.Config config)
			throws IOException {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		boolean gotSizeHint = (reqWidth > 0) || (reqHeight > 0);
		boolean gotConfig = (config != null);
		if (gotSizeHint || gotConfig) {
			if (gotConfig) {
				opts.inPreferredConfig = config;
			}
			if (gotSizeHint) {
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(is, null, opts);
				opts.inSampleSize = calcInSampleSize(opts, reqWidth, reqHeight);
				opts.inJustDecodeBounds = false;
				if (is.markSupported()) {
					try {
						is.reset();
					} catch (IOException e) {
						L.d(e);
					}
				}
			}
		}
		Bitmap bm = null;
		try {
			bm = BitmapFactory.decodeStream(is, null, opts);
		} catch (Throwable t) {
			throw new IOException(t);
		}
		if (bm == null) {
			throw new IOException("BitmapFactory returned null.");
		}
		return Pair.create(bm, opts);
	}

	private static int calcInSampleSize(BitmapFactory.Options opts,
			int reqWidth, int reqHeight) {
		int height = opts.outHeight;
		int width = opts.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			int heightRatio = Math.round((float) height / (float) reqHeight);
			int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = (heightRatio < widthRatio) ? heightRatio
					: widthRatio;
		}
		return inSampleSize;
	}

}
