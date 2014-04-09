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
import android.widget.ImageView;

public interface ImageFetchListener {

	void onFetchAdded(ImageView imageView, String imgUrl);

	void onFetchProgressChanged(ImageView imageView, String imgUrl,
			int kBTotal, int kBReceived);

	void onFetchFailed(ImageView imageView, String imgUrl, Exception e);

	void onFetchCompleted(ImageView imageView, String imgUrl, Bitmap bm);

}
