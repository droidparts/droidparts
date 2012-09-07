package org.droidparts.gram.persist;

import org.droidparts.gram.model.Filter;
import org.droidparts.gram.model.Image;
import org.droidparts.persist.json.JSONSerializer;
import org.droidparts.util.L;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageSerializer extends JSONSerializer<Image> {

	public ImageSerializer() {
		super(Image.class);
	}

	@Override
	public Image deserialize(JSONObject obj) throws JSONException {
		Image img = super.deserialize(obj);
		img.filter = new Filter(obj.getString("filter"));
		L.i("Thumbnail witdh: " + img.thumbnailWidth);
		return img;
	}

}
