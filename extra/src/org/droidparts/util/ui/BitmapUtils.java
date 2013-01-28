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

import android.graphics.Bitmap;

public final class BitmapUtils {

	public static int getSize(Bitmap bm) {
		return bm.getRowBytes() * bm.getHeight();
	}

	public static Bitmap scaleBitmap(Bitmap bm, int sidePx, boolean max) {
		float w = bm.getWidth();
		float h = bm.getHeight();
		float wRatio = sidePx / w;
		float hRatio = sidePx / h;
		float ratio = max ? Math.min(wRatio, hRatio) : Math.max(wRatio, hRatio);
		w = ratio * w;
		h = ratio * h;
		return Bitmap.createScaledBitmap(bm, (int) w, (int) h, true);
	}

}
