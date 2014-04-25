/**
 * Copyright 2014 Alex Yanchenko
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.droidparts.gram.model;

import org.droidparts.annotation.json.Key;
import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.gram.contract.DB;
import org.droidparts.model.Entity;

@Table(name = DB.Table.IMAGES)
public class Image extends Entity {
	private static final long serialVersionUID = 1L;

	@Key(name = "id")
	@Column(name = DB.Column.REMOTE_ID)
	public String remoteId;

	@Key(name = "caption" + Key.SUB + "text", optional = true)
	@Column(name = DB.Column.CAPTION)
	public String captionText = "";

	@Key(name = "images" + Key.SUB + "thumbnail" + Key.SUB + "width")
	public int thumbnailWidth;

	@Key(name = "images" + Key.SUB + "thumbnail" + Key.SUB + "url")
	@Column(name = DB.Column.THUMBNAIL_URL)
	public String thumbnailUrl = "";

	@Key(name = "images" + Key.SUB + "standard_resolution" + Key.SUB + "url")
	@Column(name = DB.Column.IMAGE_URL)
	public String imageUrl = "";

	@Key(name = "tags")
	@Column(name = DB.Column.TAGS)
	public String[] tags;

	@Column(name = DB.Column.FILTER, eager = true)
	public Filter filter;

}
