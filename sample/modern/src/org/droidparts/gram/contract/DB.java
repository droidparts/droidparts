package org.droidparts.gram.contract;

public interface DB extends org.droidparts.contract.DB {

	int VERSION = 1;
	String FILE = "dpg.sqlite";

	public interface Table extends org.droidparts.contract.DB.Table {

		String IMAGES = "images";
		String FILTERS = "filters";

	}

	public interface Column extends org.droidparts.contract.DB.Column {

		String NAME = "name";

		String REMOTE_ID = "remote_id";
		String CAPTION = "caption";
		String THUMBNAIL_URL = "thumbnail_url";
		String IMAGE_URL = "image_url";
		String TAGS = "tags";
		String FILTER = "filter";

	}

}
