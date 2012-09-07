package org.droidparts.gram.model;

import static org.droidparts.persist.json.JSONSerializer.__;

import org.droidparts.annotation.json.Key;
import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.gram.contract.DB;
import org.droidparts.model.Entity;

@Table(DB.Table.IMAGES)
public class Image extends Entity {
	private static final long serialVersionUID = 1L;

	@Key(name = "id")
	@Column(name = DB.Column.REMOTE_ID)
	public String remoteId;

	@Key(name = "caption" + __ + "text", optional = true)
	@Column(name = DB.Column.CAPTION)
	public String captionText = "";

	@Key(name = "images" + __ + "thumbnail" + __ + "width")
	public int thumbnailWidth;

	@Key(name = "images" + __ + "thumbnail" + __ + "url")
	@Column(name = DB.Column.THUMBNAIL_URL)
	public String thumbnailUrl = "";

	@Key(name = "images" + __ + "standard_resolution" + __ + "url")
	@Column(name = DB.Column.IMAGE_URL)
	public String imageUrl = "";

	@Key(name = "tags")
	@Column(name = DB.Column.TAGS)
	public String[] tags;

	@Column(name = DB.Column.FILTER, eager = true)
	public Filter filter;

}
