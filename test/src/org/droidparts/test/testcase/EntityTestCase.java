package org.droidparts.test.testcase;

import java.util.ArrayList;

import org.droidparts.contract.DB;
import org.droidparts.persist.sql.stmt.Is;
import org.droidparts.test.model.Album;
import org.droidparts.test.model.Track;
import org.droidparts.test.persist.sql.AlbumManager;
import org.droidparts.test.persist.sql.TrackManager;

import android.database.Cursor;
import android.test.AndroidTestCase;

public class EntityTestCase extends AndroidTestCase {

	private AlbumManager albumManager;
	private TrackManager trackManager;

	@Override
	protected void setUp() throws Exception {
		albumManager = new AlbumManager(getContext());
		trackManager = new TrackManager(getContext());
	}

	public void testCRUD() throws Exception {
		Album album1 = new Album();
		album1.name = "Diamond";
		album1.year = 4;
		albumManager.create(album1);
		assertFalse(album1.id == 0);
		Album album2 = albumManager.read(album1.id);
		assertEquals(album1.name, album2.name);
		album2.name = "Iris";
		albumManager.update(album2);
		Album album3 = albumManager.read(album2.id);
		assertEquals(album2.name, album3.name);
		assertEquals(album1.id, album3.id);
		albumManager.delete(album1.id);
		assertNull(albumManager.read(album1.id));
	}

	public void testUniqueAndNull() throws Exception {
		Album album1 = new Album();
		album1.name = "one1";
		album1.comment = "two1";
		boolean created = albumManager.create(album1);
		assertTrue(created);

		Album album2 = new Album();
		created = albumManager.create(album2);
		assertFalse(created);

		album2.name = album1.name;
		created = albumManager.create(album2);
		assertFalse(created);

		album2.name = album1.name + "x";
		created = albumManager.create(album2);
		assertTrue(created);

		int count = albumManager.select()
				.where(DB.Column.ID, Is.EQUAL, album1.id).count();
		assertEquals(1, count);

		Cursor cursor = albumManager.select().where("comment", Is.NOT_NULL)
				.execute();
		assertEquals(1, cursor.getCount());
		cursor.moveToFirst();
		Album album11 = albumManager.readFromCursor(cursor);
		assertEquals(album1.name, album11.name);
		cursor.close();

		cursor = albumManager.select().where("comment", Is.NULL).execute();
		assertEquals(1, cursor.getCount());
		cursor.moveToFirst();
		Album album21 = albumManager.readFromCursor(cursor);
		assertEquals(album2.name, album21.name);
		cursor.close();
		//
		deleteAllAlbums();
	}

	public void testInAndLike() throws Exception {
		ArrayList<Album> list = new ArrayList<Album>();
		for (String str : new String[] { "pc", "mac", "phone" }) {
			Album album = new Album();
			album.name = str;
			list.add(album);
		}
		boolean success = albumManager.create(list);
		assertTrue(success);
		//
		int count = albumManager.select().where(DB.Column.ID, Is.IN, 1, 2)
				.count();
		assertEquals(2, count);
		//
		int[] arr = new int[] { 1, 2 };
		count = albumManager.select().where(DB.Column.ID, Is.NOT_IN, arr)
				.count();
		assertEquals(1, count);
		//
		count = albumManager.select().where("name", Is.LIKE, "%%hon%%").count();
		assertEquals(1, count);
		//
		deleteAllAlbums();
	}

	private void deleteAllAlbums() {
		albumManager.delete().execute();
	}

	public void testForeignKeys() {
		Album album = new Album("Diamond", 2007);
		albumManager.create(album);
		String[] tracks = new String[] { "Diamond", "Beautiful", "Stay" };
		for (String name : tracks) {
			Track track = new Track();
			track.album = album;
			track.name = name;
			trackManager.create(track);
		}
		assertEquals(3,
				trackManager.select().where("album_id", Is.EQUAL, album.id)
						.count());
		albumManager.delete(album.id);
		assertEquals(0, trackManager.select().count());
	}
}
