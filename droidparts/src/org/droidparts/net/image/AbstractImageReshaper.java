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
package org.droidparts.net.image;

import android.graphics.Bitmap.CompressFormat;
import android.util.Pair;

public abstract class AbstractImageReshaper implements ImageReshaper {

	// slow, supports transparency
	public static final Pair<CompressFormat, Integer> PNG = Pair.create(
			CompressFormat.PNG, 100);
	// fast
	public static final Pair<CompressFormat, Integer> JPEG = Pair.create(
			CompressFormat.JPEG, 80);

	@Override
	public Pair<CompressFormat, Integer> getCacheFormat(String contentType) {
		if ("image/png".equals(contentType)) {
			return PNG;
		} else {
			return JPEG;
		}
	}

}
