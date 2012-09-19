package org.droidparts.test.model;

import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.model.Entity;
import org.droidparts.test.persist.DB;

@Table(name = DB.Table.TRACKS)
public class Track extends Entity {
	private static final long serialVersionUID = 1L;

	@Column(name = DB.Column.NAME)
	public String name;

	@Column(name = DB.Column.ALBUM_ID)
	public Album album;

	@Column(nullable = true)
	public Album nullableAlbum;

}
