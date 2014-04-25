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
package org.droidparts.test.persist.sql;

import java.util.ArrayList;
import java.util.Collection;

import org.droidparts.persist.sql.EntityManager;
import org.droidparts.persist.sql.stmt.Is;
import org.droidparts.persist.sql.stmt.Select;
import org.droidparts.test.model.Album;
import org.droidparts.test.model.AlbumToTag;
import org.droidparts.test.model.Tag;
import org.droidparts.test.persist.DB;

import android.content.Context;

public class AlbumManager extends EntityManager<Album> implements DB {

	private final EntityManager<AlbumToTag> albumToTagManager;
	private final EntityManager<Tag> tagManager;

	public AlbumManager(Context ctx) {
		super(Album.class, ctx);
		albumToTagManager = new EntityManager<AlbumToTag>(AlbumToTag.class, ctx);
		tagManager = new EntityManager<Tag>(Tag.class, ctx);
	}

	public ArrayList<Tag> getTags(long albumId) {
		ArrayList<Tag> tags = new ArrayList<Tag>();
		Select<Tag> select = tagManager.select().where(Column.ID, Is.IN,
				getTagIds(albumId));
		tags = tagManager.readAll(select);
		return tags;
	}

	public void addTags(long albumId, Collection<Tag> tags) {
		Album album = read(albumId);
		for (Tag tag : tags) {
			boolean success = tagManager.create(tag);
			if (!success) {
				Select<Tag> select = tagManager.select().where(Column.NAME,
						Is.EQUAL, tag.name);
				tag = tagManager.readFirst(select);
			}
			albumToTagManager.create(new AlbumToTag(album, tag));
		}
	}

	public long[] getTagIds(long albumId) {
		Select<AlbumToTag> select = albumToTagManager.select()
				.columns(Column.TAG_ID)
				.where(Column.ALBUM_ID, Is.EQUAL, albumId);
		return albumToTagManager.readIds(select);
	}
}
