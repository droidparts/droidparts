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
package org.droidparts.inner;

import java.io.IOException;

import org.droidparts.util.L;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.util.Pair;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class BitmapFactoryUtils {

	public static Point calcDecodeSizeHint(ImageView imageView) {
		Point p = new Point();
		LayoutParams params = imageView.getLayoutParams();
		p.x = (params != null) ? params.width : imageView.getWidth();
		p.y = (params != null) ? params.height : imageView.getHeight();
		return p;
	}

	public static Pair<Bitmap, BitmapFactory.Options> decodeScaled(byte[] data,
			int reqWidth, int reqHeight, Bitmap.Config config, Bitmap inBitmap)
			throws Exception {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		boolean gotSizeHint = (reqWidth > 0) || (reqHeight > 0);
		boolean gotConfig = (config != null);
		if (gotSizeHint || gotConfig) {
			if (gotConfig) {
				opts.inPreferredConfig = config;
			}
			if (gotSizeHint) {
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeByteArray(data, 0, data.length, opts);
				opts.inSampleSize = calcInSampleSize(opts, reqWidth, reqHeight);
				opts.inJustDecodeBounds = false;
			}
			if (inBitmapSupported()) {
				opts.inBitmap = inBitmap;
			}
		}
		Bitmap bm = null;
		try {
			bm = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
		} catch (Throwable t) {
			System.gc();
			if (inBitmapSupported() && inBitmap != null) {
				opts.inBitmap = null;
				try {
					bm = BitmapFactory.decodeByteArray(data, 0, data.length,
							opts);
					L.w(t);
				} catch (Throwable th) {
					System.gc();
					throw new Exception(th);
				}
			} else {
				throw new Exception(t);
			}
		}
		if (bm == null) {
			throw new IOException("BitmapFactory returned null.");
		} else if (bm.getWidth() <= 0 || bm.getHeight() <= 0) {
			throw new IOException(String.format("Invalid Bitmap: w:%d, h:%d.",
					bm.getWidth(), bm.getHeight()));
		}
		return Pair.create(bm, opts);
	}

	private static boolean inBitmapSupported() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	private static int calcInSampleSize(BitmapFactory.Options opts,
			int reqWidth, int reqHeight) {
		int height = opts.outHeight;
		int width = opts.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			int heightRatio = 1;
			if (reqHeight > 0) {
				heightRatio = Math.round((float) height / (float) reqHeight);
			}
			int widthRatio = 1;
			if (reqWidth > 0) {
				widthRatio = Math.round((float) width / (float) reqWidth);
			}
			inSampleSize = (heightRatio < widthRatio) ? heightRatio
					: widthRatio;
		}
		return inSampleSize;
	}

}
