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
package org.droidparts.test.testcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.droidparts.persist.sql.EntityManager;
import org.droidparts.persist.sql.stmt.Is;
import org.droidparts.test.model.Album;
import org.droidparts.test.model.AlbumToTag;
import org.droidparts.test.model.Primitives;
import org.droidparts.test.model.Primitives.En;
import org.droidparts.test.model.Tag;
import org.droidparts.test.model.Track;
import org.droidparts.test.persist.DB;
import org.droidparts.test.persist.sql.AlbumManager;
import org.droidparts.test.persist.sql.TrackManager;

import android.database.Cursor;
import android.test.AndroidTestCase;

public class EntityTestCase extends AndroidTestCase implements DB {

	private static final String[] ALBUMS = new String[] { "Diamond", "Iris" };
	private static final int[] YEARS = new int[] { 2007, 2009 };
	private static final String[] TRACKS = new String[] { "Diamond",
			"Beautiful", "Stay", "Secret Desire", "The Sun", "Buddha" };
	private static final String[] TAGS = new String[] { "downtempo", "lounge",
			"chillout" };

	private EntityManager<Primitives> primitivesManager;
	private EntityManager<AlbumToTag> albumToTagManager;
	private AlbumManager albumManager;
	private EntityManager<Tag> tagManager;
	private TrackManager trackManager;

	@Override
	protected void setUp() {
		if (primitivesManager == null) {
			primitivesManager = new EntityManager<Primitives>(Primitives.class,
					getContext());
			albumToTagManager = new EntityManager<AlbumToTag>(AlbumToTag.class,
					getContext());
			tagManager = new EntityManager<Tag>(Tag.class, getContext());
			albumManager = new AlbumManager(getContext());
			trackManager = new TrackManager(getContext());
		}
	}

	@Override
	protected void tearDown() {
		primitivesManager.delete().execute();
		tagManager.delete().execute();
		albumManager.delete().execute();
		trackManager.delete().execute();
	}

	public void testUniqueIndex() {
		Album album = new Album(ALBUMS[0], YEARS[0]);
		Tag tag = new Tag(TAGS[0]);
		AlbumToTag att = new AlbumToTag(album, tag);
		assertTrue(albumToTagManager.create(att));
		assertFalse(albumToTagManager.create(att));
		album.name = ALBUMS[1];
		album.id = 0;
		assertTrue(albumToTagManager.create(att));
		assertEquals(2, albumToTagManager.select().count());
	}

	public void testCRUD() {
		Album album1 = createAlbum();
		assertFalse(album1.id == 0);
		Album album2 = albumManager.read(album1.id);
		assertEquals(album1.name, album2.name);
		album2.name = ALBUMS[1];
		albumManager.update(album2);
		Album album3 = albumManager.read(album2.id);
		assertEquals(album2.name, album3.name);
		assertEquals(album1.id, album3.id);
		albumManager.delete(album1.id);
		assertNull(albumManager.read(album1.id));
	}

	public void testUniqueAndNull() {
		Album album1 = new Album(ALBUMS[0], YEARS[0]);
		album1.comment = TAGS[0];
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

		int count = albumManager.select().whereId(album1.id).count();
		assertEquals(1, count);

		Cursor cursor = albumManager.select()
				.where(Column.COMMENT, Is.NOT_NULL).execute();
		assertEquals(1, cursor.getCount());
		cursor.moveToFirst();
		Album album11 = albumManager.readRow(cursor);
		assertEquals(album1.name, album11.name);
		cursor.close();

		cursor = albumManager.select().where(Column.COMMENT, Is.NULL).execute();
		assertEquals(1, cursor.getCount());
		cursor.moveToFirst();
		Album album21 = albumManager.readRow(cursor);
		assertEquals(album2.name, album21.name);
		cursor.close();
	}

	public void testDate() {
		long now = System.currentTimeMillis();
		Primitives pri = new Primitives();
		pri.date = new Date(now);
		pri.dates.add(pri.date);
		assertTrue(primitivesManager.create(pri));
		pri = primitivesManager.read(pri.id);
		assertEquals(now, pri.date.getTime());
		assertEquals(1, pri.dates.size());
		assertEquals(now, pri.dates.get(0).getTime());

	}

	public void testEnum() {
		Primitives pri = new Primitives();
		pri.en = En.HI;
		pri.enArr = new En[] { En.HI, En.THERE };
		primitivesManager.create(pri);
		pri = primitivesManager.read(pri.id);
		assertEquals(En.HI, pri.en);
		assertEquals(En.THERE.id, pri.enArr[1].id);
	}

	public void testArraysAndCollections() {
		Primitives pri = new Primitives();
		pri.strArr = new String[] { "one", "two" };
		pri.intArr = new int[] { 10, 20, 30 };
		pri.strList.addAll(Arrays.asList(pri.strArr));
		pri.doubleSet.add(100.500);
		pri.doubleSet.add(12.5);
		assertTrue(primitivesManager.create(pri));
		pri = primitivesManager.read(pri.id);
		assertEquals(2, pri.strArr.length);
		assertEquals(30, pri.intArr[2]);
		assertTrue(pri.strList.contains("one"));
		assertTrue(pri.doubleSet.contains(12.5));
	}

	public void testWhereId() {
		createAlbums(10);
		//
		int count = albumManager.select().whereId(5, 8).count();
		assertEquals(2, count);
		count = albumManager.select().whereId(3).count();
		assertEquals(1, count);
		count = albumManager.select().whereId(15).count();
		assertEquals(0, count);
	}

	public void testBetween() {
		createAlbums(20);
		//
		int count = albumManager.select().where(Column.ID, Is.BETWEEN, 5, 10)
				.count();
		assertEquals(6, count);
		count = albumManager.select().where(Column.ID, Is.NOT_BETWEEN, 5, 10)
				.count();
		assertEquals(14, count);
	}

	public void testIn() {
		createAlbums(3);
		//
		int[] arr = new int[] { 1, 2 };
		int count = albumManager.select().where(Column.ID, Is.IN, arr).count();
		assertEquals(2, count);
		count = albumManager.select().where(Column.ID, Is.NOT_IN, arr).count();
		assertEquals(1, count);
	}

	public void testLike() {
		ArrayList<Album> list = new ArrayList<Album>();
		for (String str : TRACKS) {
			Album album = new Album();
			album.name = str;
			list.add(album);
		}
		albumManager.create(list);
		int count = albumManager.select()
				.where(Column.NAME, Is.LIKE, "%%udd%%").count();
		assertEquals(1, count);
		count = albumManager.select()
				.where(Column.NAME, Is.NOT_LIKE, "%%udd%%").count();
		assertEquals(TRACKS.length - 1, count);
	}

	public void testForeignKeys() {
		Album album = createAlbum();
		for (String name : TRACKS) {
			Track track = new Track();
			track.album = album;
			track.name = name;
			trackManager.create(track);
		}
		assertEquals(TRACKS.length,
				trackManager.select()
						.where(Column.ALBUM_ID, Is.EQUAL, album.id).count());
		albumManager.delete(album.id);
		assertEquals(0, trackManager.select().count());
	}

	public void testEagerForeignKeys() {
		Album album = createAlbum();
		Track track = new Track();
		track.name = TRACKS[0];
		track.album = album;
		track.nullableAlbum = album;
		trackManager.create(track);
		track = trackManager.read(track.id);
		assertNotNull(track.album.name);
		assertNull(track.nullableAlbum.name);
	}

	public void testUniqueAndNullable() {
		Album album = new Album();
		assertFalse(albumManager.create(album));
		album.name = "name";
		assertTrue(albumManager.create(album));
		assertFalse(albumManager.create(album));
		//
		Track track = new Track();
		track.name = "tr";
		assertFalse(trackManager.create(track));
		track.album = album;
		assertTrue(trackManager.create(track));
		//
		Album album2 = new Album();
		album2.name = "name2";
		track.nullableAlbum = album2;
		assertTrue(trackManager.update(track));
		assertFalse(track.nullableAlbum.id == 0);
		//
		album.name = null;
		assertFalse(albumManager.update(album));
		//
		track.nullableAlbum = null;
		assertTrue(trackManager.update(track));
		assertNull(trackManager.read(track.id).nullableAlbum);
	}

	public void testOffsetLimit() {
		int count = 100;
		int offset = 10;
		int limit = 20;
		createAlbums(count);
		assertEquals(count, albumManager.select().count());
		assertEquals(limit, albumManager.select().limit(limit).count());
		assertEquals(limit, albumManager.select().offset(offset).limit(limit)
				.count());
		assertEquals(count - offset, albumManager.select().offset(offset)
				.count());
	}

	public void testWhere() {
		Album album = createAlbum();
		assertEquals(1, albumManager.select().where("_id = ?", album.id)
				.count());
		assertEquals(1, albumManager.select().where("_id = " + album.id)
				.count());
	}

	public void testM2M() {
		Album album = createAlbum();
		ArrayList<Tag> tags = new ArrayList<Tag>();
		for (String name : TAGS) {
			tags.add(new Tag(name));
		}
		albumManager.addTags(album.id, tags);
		//
		assertEquals(TAGS.length, albumToTagManager.select().count());
		tags = albumManager.getTags(album.id);
		assertEquals(TAGS.length, tags.size());
		//
		tagManager.delete(tags);
		tags = albumManager.getTags(album.id);
		assertEquals(0, albumToTagManager.select().count());
		assertEquals(0, tags.size());
	}

	private Album createAlbum() {
		Album album = new Album(ALBUMS[0], YEARS[0]);
		albumManager.create(album);
		return album;
	}

	private int createAlbums(int count) {
		ArrayList<Album> albums = new ArrayList<Album>();
		for (int i = 0; i < count; i++) {
			albums.add(new Album("A " + i, i));
		}
		return albumManager.create(albums);
	}

}
