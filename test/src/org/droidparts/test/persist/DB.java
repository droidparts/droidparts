package org.droidparts.test.persist;

public interface DB extends org.droidparts.contract.DB {

	public interface Table extends org.droidparts.contract.DB.Table {

		String ALBUMS = "_albums_";
		String TRACKS = "_tracks_";
		String TAGS = "_tags_";

		String ALBUM_TO_TAG = "_album_to_tag_";

	}

	public interface Column extends org.droidparts.contract.DB.Column {

		String YEAR = "_year_";
		String NAME = "_name_";
		String COMMENT = "_comment_";

		String ALBUM_ID = "_album_";
		String TAG_ID = "_tag_";

	}

}
