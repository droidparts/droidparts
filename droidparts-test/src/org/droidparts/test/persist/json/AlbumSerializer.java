package org.droidparts.test.persist.json;

import org.droidparts.persist.json.JSONSerializer;
import org.droidparts.test.model.Album;

import android.content.Context;

public class AlbumSerializer extends JSONSerializer<Album> {

	public AlbumSerializer(Context ctx) {
		super(Album.class, ctx);
	}

}
