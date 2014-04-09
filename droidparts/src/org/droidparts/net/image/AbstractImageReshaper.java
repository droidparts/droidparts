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
package org.droidparts.net.image;

import android.graphics.Bitmap;
import android.util.Pair;

public abstract class AbstractImageReshaper implements ImageReshaper {

	// slow, supports transparency
	public static final Pair<Bitmap.CompressFormat, Integer> PNG = Pair.create(
			Bitmap.CompressFormat.PNG, 100);
	// fast
	public static final Pair<Bitmap.CompressFormat, Integer> JPEG = Pair
			.create(Bitmap.CompressFormat.JPEG, 80);

	@Override
	public Pair<Bitmap.CompressFormat, Integer> getCacheFormat(String mimeType) {
		if ("image/png".equals(mimeType)) {
			return PNG;
		} else {
			return JPEG;
		}
	}

	@Override
	public Bitmap.Config getBitmapConfig() {
		return Bitmap.Config.ARGB_8888;
	}

}
