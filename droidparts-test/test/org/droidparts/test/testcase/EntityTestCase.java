/**
 * Copyright 2017 Alex Yanchenko
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

import android.database.Cursor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.droidparts.persist.sql.EntityManager;
import org.droidparts.persist.sql.stmt.Is;
import org.droidparts.persist.sql.stmt.Where;
import org.droidparts.test.model.Album;
import org.droidparts.test.model.AlbumToTag;
import org.droidparts.test.model.Nested;
import org.droidparts.test.model.Primitives;
import org.droidparts.test.model.Primitives.En;
import org.droidparts.test.model.Tag;
import org.droidparts.test.model.Track;
import org.droidparts.test.persist.DB;
import org.droidparts.test.persist.sql.AlbumManager;
import org.droidparts.test.persist.sql.TrackManager;
import org.droidparts.test.testcase.activity.ActivityTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EntityTestCase extends ActivityTestCase implements DB {

	private static final String[] ALBUMS = new String[]{"Diamond", "Iris"};
	private static final int[] YEARS = new int[]{2007, 2009};
	private static final String[] TRACKS = new String[]{"Diamond", "Beautiful", "Stay", "Secret Desire", "The Sun",
			"Buddha"};
	private static final String[] TAGS = new String[]{"downtempo", "lounge", "chillout"};

	private EntityManager<Primitives> primitivesManager;
	private EntityManager<AlbumToTag> albumToTagManager;
	private AlbumManager albumManager;
	private EntityManager<Tag> tagManager;
	private TrackManager trackManager;

	@Before
	public void setUp() {
		if (primitivesManager == null) {
			primitivesManager = new EntityManager<Primitives>(Primitives.class, getActivity());
			albumToTagManager = new EntityManager<AlbumToTag>(AlbumToTag.class, getActivity());
			tagManager = new EntityManager<Tag>(Tag.class, getActivity());
			albumManager = new AlbumManager(getActivity());
			trackManager = new TrackManager(getActivity());
		}
	}

	@After
	public void tearDown() {
		primitivesManager.delete().execute();
		tagManager.delete().execute();
		albumManager.delete().execute();
		trackManager.delete().execute();
	}

	@Test
	public void testUniqueIndex() {
		Album album = new Album(ALBUMS[0], YEARS[0]);
		Tag tag = new Tag(TAGS[0]);
		AlbumToTag att = new AlbumToTag(album, tag);
		assertTrue(albumToTagManager.create(att));
		assertFalse(albumToTagManager.create(att));
		album.name = ALBUMS[1];
		album._id = 0;
		assertTrue(albumToTagManager.create(att));
		assertEquals(2, albumToTagManager.select().count());
	}

	@Test
	public void testCRUD() {
		Album album1 = createFirstAlbum();
		assertFalse(album1._id == 0);
		Album album2 = albumManager.read(album1._id);
		assertEquals(album1.name, album2.name);
		album2.name = ALBUMS[1];
		albumManager.update(album2);
		Album album3 = albumManager.read(album2._id);
		assertEquals(album2.name, album3.name);
		assertEquals(album1._id, album3._id);
		albumManager.delete(album1._id);
		assertNull(albumManager.read(album1._id));
	}

	@Test
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

		int count = albumManager.select().whereId(album1._id).count();
		assertEquals(1, count);

		Cursor cursor = albumManager.select().where(Column.COMMENT, Is.NOT_NULL).execute();
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

	@Test
	public void testModel() {
		Album album = createFirstAlbum();
		album.nested.str = "str";
		for (String str : TRACKS) {
			Nested n = new Nested();
			n.str = str;
			album.nestedList.add(n);
		}
		album.nestedArr = album.nestedList.toArray(new Nested[album.nestedList.size()]);
		albumManager.update(album);
		album = albumManager.read(album._id);
		assertEquals("str", album.nested.str);
		assertEquals(TRACKS.length, album.nestedList.size());
		assertEquals(TRACKS.length, album.nestedArr.length);
	}

	@Test
	public void testDate() {
		long now = System.currentTimeMillis();
		Primitives pri = new Primitives();
		pri.date = new Date(now);
		pri.dates.add(pri.date);
		assertTrue(primitivesManager.create(pri));
		pri = primitivesManager.read(pri._id);
		assertEquals(now, pri.date.getTime());
		assertEquals(1, pri.dates.size());
		assertEquals(now, pri.dates.get(0).getTime());

	}

	@Test
	public void testEnum() {
		Primitives pri = new Primitives();
		pri.en = En.HI;
		pri.enArr = new En[]{En.HI, En.THERE};
		primitivesManager.create(pri);
		pri = primitivesManager.read(pri._id);
		assertEquals(En.HI, pri.en);
		assertEquals(En.THERE.id, pri.enArr[1].id);
	}

	@Test
	public void testArraysAndCollections() {
		Primitives pri = new Primitives();
		pri.strArr = new String[]{"one", "two"};
		pri.intArr = new int[]{10, 20, 30};
		pri.strList.addAll(Arrays.asList(pri.strArr));
		pri.doubleSet.add(100.500);
		pri.doubleSet.add(12.5);
		assertTrue(primitivesManager.create(pri));
		pri = primitivesManager.read(pri._id);
		assertEquals(2, pri.strArr.length);
		assertEquals(30, pri.intArr[2]);
		assertTrue(pri.strList.contains("one"));
		assertTrue(pri.doubleSet.contains(12.5));
	}

	@Test
	public void testWhereId() {
		createDummyAlbums(10);
		//
		int count = albumManager.select().whereId(5, 8).count();
		assertEquals(2, count);
		count = albumManager.select().whereId(3).count();
		assertEquals(1, count);
		count = albumManager.select().whereId(15).count();
		assertEquals(0, count);
	}

	@Test
	public void testBetween() {
		createDummyAlbums(20);
		//
		int count = albumManager.select().where(Column._ID, Is.BETWEEN, 5, 10).count();
		assertEquals(6, count);
		count = albumManager.select().where(Column._ID, Is.NOT_BETWEEN, 5, 10).count();
		assertEquals(14, count);
	}

	@Test
	public void testIn() {
		createDummyAlbums(3);
		//
		int[] arr = new int[]{1, 2};
		int count = albumManager.select().where(Column._ID, Is.IN, arr).count();
		assertEquals(2, count);
		count = albumManager.select().where(Column._ID, Is.NOT_IN, arr).count();
		assertEquals(1, count);
	}

	@Test
	public void testLike() {
		ArrayList<Album> list = new ArrayList<Album>();
		for (String str : TRACKS) {
			Album album = new Album();
			album.name = str;
			list.add(album);
		}
		albumManager.createAll(list);
		int count = albumManager.select().where(Column.NAME, Is.LIKE, "%%udd%%").count();
		assertEquals(1, count);
		count = albumManager.select().where(Column.NAME, Is.NOT_LIKE, "%%udd%%").count();
		assertEquals(TRACKS.length - 1, count);
	}

	@Test
	public void testForeignKeys() {
		Album album = createFirstAlbum();
		for (String name : TRACKS) {
			Track track = new Track();
			track.album = album;
			track.name = name;
			trackManager.create(track);
		}
		assertEquals(TRACKS.length, trackManager.select().where(Column.ALBUM_ID, Is.EQUAL, album._id).count());
		albumManager.delete(album._id);
		assertEquals(0, trackManager.select().count());
	}

	@Test
	public void testEagerForeignKeys() {
		Album album = createFirstAlbum();
		Track track = new Track();
		track.name = TRACKS[0];
		track.album = album;
		track.nullableAlbum = album;
		trackManager.create(track);
		track = trackManager.read(track._id);
		assertNotNull(track.album.name);
		assertNull(track.nullableAlbum.name);
	}

	@Test
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
		assertFalse(track.nullableAlbum._id == 0);
		//
		album.name = null;
		assertFalse(albumManager.update(album));
		//
		track.nullableAlbum = null;
		assertTrue(trackManager.update(track));
		assertNull(trackManager.read(track._id).nullableAlbum);
	}

	@Test
	public void testOffsetLimit() {
		int count = 100;
		int offset = 10;
		int limit = 20;
		createDummyAlbums(count);
		assertEquals(count, albumManager.select().count());
		assertEquals(limit, albumManager.select().limit(limit).count());
		assertEquals(limit, albumManager.select().offset(offset).limit(limit).count());
		assertEquals(count - offset, albumManager.select().offset(offset).count());
	}

	@Test
	public void testWhere() {
		Album album = createFirstAlbum();
		assertEquals(1, albumManager.select().where("_id = ?", album._id).count());
		assertEquals(1, albumManager.select().where("_id = " + album._id).count());
	}

	@Test
	public void testWhereCount() {
		createDummyAlbums(20);
		assertEquals(1, albumManager.select().where(Column._ID, Is.EQUAL, 10).count());
		assertEquals(0, albumManager.select().where(Column._ID, Is.EQUAL, 100).count());
	}

	@Test
	public void testMultipleWheres() {
		createDummyAlbums(20);
		Where where1 = new Where(Column.NAME, Is.IN, "A 10", "A 11", "A 12");
		Where where2 = new Where(Column.YEAR, Is.IN, 10, 11, 15);
		int count = albumManager.select().where(where1.and(where2)).count();
		assertEquals(2, count);
	}

	@Test
	public void testM2M() {
		Album album = createFirstAlbum();
		ArrayList<Tag> tags = new ArrayList<Tag>();
		for (String name : TAGS) {
			tags.add(new Tag(name));
		}
		albumManager.addTags(album._id, tags);
		//
		assertEquals(TAGS.length, albumToTagManager.select().count());
		tags = albumManager.getTags(album._id);
		assertEquals(TAGS.length, tags.size());
		//
		tagManager.deleteAll(tags);
		tags = albumManager.getTags(album._id);
		assertEquals(0, albumToTagManager.select().count());
		assertEquals(0, tags.size());
	}

	private Album createFirstAlbum() {
		Album album = new Album(ALBUMS[0], YEARS[0]);
		albumManager.create(album);
		return album;
	}

	private int createDummyAlbums(int count) {
		ArrayList<Album> albums = new ArrayList<Album>();
		for (int i = 0; i < count; i++) {
			albums.add(new Album("A " + i, i));
		}
		return albumManager.createAll(albums);
	}

}
