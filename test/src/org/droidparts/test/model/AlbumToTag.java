package org.droidparts.test.model;

import org.droidparts.annotation.sql.Column;
import org.droidparts.model.Entity;
import org.droidparts.test.persist.DB;

public class AlbumToTag extends Entity {
	private static final long serialVersionUID = 1L;

	@Column(name = DB.Column.ALBUM_ID)
	public Album album;

	@Column(name = DB.Column.TAG_ID)
	public Tag tag;

	public AlbumToTag() {
	}

	public AlbumToTag(Album album, Tag tag) {
		this.album = album;
		this.tag = tag;
	}

}
