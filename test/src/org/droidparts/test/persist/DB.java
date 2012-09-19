package org.droidparts.test.persist;

public interface DB extends org.droidparts.contract.DB {

	public interface Table extends org.droidparts.contract.DB.Table {

		String ALBUMS = "albums";
		String TRACKS = "tracks";
		String TAGS = "tags";

		String ALBUM_TO_TAG = "album_to_tag";

	}

	public interface Column extends org.droidparts.contract.DB.Column {

		String YEAR = "year";
		String NAME = "name";
		String COMMENT = "comment";

		String ALBUM_ID = "album_id";
		String TAG_ID = "tag_id";

	}

}
