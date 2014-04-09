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
