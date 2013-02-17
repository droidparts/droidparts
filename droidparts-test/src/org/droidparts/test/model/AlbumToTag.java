package org.droidparts.test.model;

import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.model.Entity;
import org.droidparts.test.persist.DB;

@Table(name = DB.Table.ALBUM_TO_TAG)
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
