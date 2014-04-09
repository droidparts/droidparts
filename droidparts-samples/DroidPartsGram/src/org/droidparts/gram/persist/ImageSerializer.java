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
package org.droidparts.gram.persist;

import org.droidparts.gram.model.Filter;
import org.droidparts.gram.model.Image;
import org.droidparts.persist.json.JSONSerializer;
import org.droidparts.util.L;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class ImageSerializer extends JSONSerializer<Image> {

	public ImageSerializer(Context ctx) {
		super(Image.class, ctx);
	}

	@Override
	public Image deserialize(JSONObject obj) throws JSONException {
		Image img = super.deserialize(obj);
		img.filter = new Filter(obj.getString("filter"));
		L.i("Thumbnail witdh: " + img.thumbnailWidth);
		return img;
	}

}
