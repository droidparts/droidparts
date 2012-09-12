package org.droidparts.test.persist.json;

import org.droidparts.persist.json.JSONSerializer;
import org.droidparts.test.model.Album;

public class AlbumSerializer extends JSONSerializer<Album> {

	public AlbumSerializer() {
		super(Album.class);
	}

}
