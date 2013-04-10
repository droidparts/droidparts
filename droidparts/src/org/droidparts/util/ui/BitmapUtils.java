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
package org.droidparts.util.ui;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public final class BitmapUtils {

	public static int getSize(Bitmap bm) {
		return bm.getRowBytes() * bm.getHeight();
	}

	public static Bitmap getScaled(Bitmap bm, int sidePx, boolean max) {
		float w = bm.getWidth();
		float h = bm.getHeight();
		float wRatio = sidePx / w;
		float hRatio = sidePx / h;
		float ratio = max ? Math.min(wRatio, hRatio) : Math.max(wRatio, hRatio);
		w = ratio * w;
		h = ratio * h;
		return Bitmap.createScaledBitmap(bm, (int) w, (int) h, true);
	}

	public static Bitmap getSquare(Bitmap bm) {
		int w = bm.getWidth();
		int h = bm.getHeight();
		int side = (w > h) ? h : w;
		return Bitmap.createBitmap(bm, 0, 0, side, side);
	}

	public static Bitmap getRounded(Bitmap bm, int cornerRadiusPx) {
		int w = bm.getWidth();
		int h = bm.getHeight();

		Bitmap bmOut = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmOut);

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(0xff424242);

		Rect rect = new Rect(0, 0, w, h);
		RectF rectF = new RectF(rect);

		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawRoundRect(rectF, cornerRadiusPx, cornerRadiusPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bm, rect, rect, paint);

		return bmOut;
	}

	public static Bitmap decodeScaled(InputStream is, int reqWidth,
			int reqHeight) {
		BitmapFactory.Options opts = null;
		if (reqWidth > 0 && reqHeight > 0) {
			opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(is, null, opts);
			opts.inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight);
			opts.inJustDecodeBounds = false;
		}
		return BitmapFactory.decodeStream(is, null, opts);
	}

	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		int height = options.outHeight;
		int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			int heightRatio = Math.round((float) height / (float) reqHeight);
			int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

}
