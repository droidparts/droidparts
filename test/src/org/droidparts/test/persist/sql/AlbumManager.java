package org.droidparts.test.persist.sql;

import java.util.ArrayList;
import java.util.Collection;

import org.droidparts.persist.sql.EntityManager;
import org.droidparts.persist.sql.stmt.Is;
import org.droidparts.test.model.Album;
import org.droidparts.test.model.AlbumToTag;
import org.droidparts.test.model.Tag;
import org.droidparts.test.persist.DB;
import org.droidparts.util.DatabaseUtils2;

import android.content.Context;
import android.database.Cursor;

public class AlbumManager extends EntityManager<Album> implements DB {

	private final EntityManager<AlbumToTag> albumToTagManager;
	private final EntityManager<Tag> tagManager;

	public AlbumManager(Context ctx) {
		super(ctx, Album.class);
		albumToTagManager = EntityManager.getInstance(ctx, AlbumToTag.class);
		tagManager = EntityManager.getInstance(ctx, Tag.class);
	}

	public ArrayList<Tag> getTags(long albumId) {
		ArrayList<Tag> tags = new ArrayList<Tag>();
		Cursor c = tagManager.select()
				.where(Column.ID, Is.IN, getTagIds(albumId)).execute();
		tags = tagManager.readAllFromCursor(c);
		return tags;
	}

	public void addTags(long albumId, Collection<Tag> tags) {
		Album album = read(albumId);
		for (Tag tag : tags) {
			boolean success = tagManager.create(tag);
			if (!success) {
				Cursor c = tagManager.select()
						.where(Column.NAME, Is.EQUAL, tag.name).execute();
				tag = tagManager.readFirstFromCursor(c);
			}
			albumToTagManager.create(new AlbumToTag(album, tag));
		}
	}

	public long[] getTagIds(long albumId) {
		Cursor c = albumToTagManager.select().columns(Column.TAG_ID)
				.where(Column.ALBUM_ID, Is.EQUAL, albumId).execute();
		return DatabaseUtils2.readIds(c);
	}
}
