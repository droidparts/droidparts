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
package org.droidparts.test.testcase.serialize;

import java.util.ArrayList;

import android.support.test.runner.AndroidJUnit4;
import android.test.AssertionFailedError;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.droidparts.model.Model;
import org.droidparts.persist.serializer.SerializerException;
import org.droidparts.persist.serializer.XMLSerializer;
import org.droidparts.test.R;
import org.droidparts.test.model.Album;
import org.droidparts.test.model.Album2;
import org.droidparts.test.model.AlbumFail;
import org.droidparts.test.model.Collections;
import org.droidparts.test.testcase.activity.ActivityTestCase;
import org.droidparts.util.IOUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class XMLTestCase extends ActivityTestCase {

	@Test
	public void testAlbums() throws Exception {
		Document albumsDoc = getXMLDocument(R.raw.albums_xml);
		NodeList nl = albumsDoc.getElementsByTagName("album");
		XMLSerializer<Album> serializer = makeSerializer(Album.class);
		ArrayList<Album> albums = serializer.deserializeAll(nl);
		assertEquals(2, albums.size());
		assertEquals("Diamond", albums.get(0).name);
		assertEquals(2009, albums.get(1).year);
	}

	@Test
	public void testAlbum2() throws Exception {
		Document albumDoc = getXMLDocument(R.raw.album2);
		XMLSerializer<Album2> serializer = makeSerializer(Album2.class);
		Album2 a2 = serializer.deserialize(albumDoc);
		assertEquals("Iris", a2.name);
		assertEquals(2009, a2.year);
		assertEquals(2, a2.albumArr.length);
		assertEquals(2, a2.albumList.size());
		assertEquals(3, a2.ints.length);
		assertEquals(3, a2.integers.size());
		assertEquals(5, a2.ints[1]);
		assertEquals(100500, (int) a2.integers.get(2));
		assertEquals(a2.integers.size(), a2.integersHinted.size());
		assertTrue(a2.integersHintedWrong.isEmpty());
	}

	@Test
	public void testFail() throws Exception {
		Document albumDoc = getXMLDocument(R.raw.album2);
		XMLSerializer<AlbumFail> serializer = makeSerializer(AlbumFail.class);
		try {
			serializer.deserialize(albumDoc);
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(e instanceof SerializerException);
		}
	}

	//

	@Test
	public void testCollectionsFail() throws Exception {
		try {
			Document doc = getXMLDocument(R.raw.albums_partial_xml);
			XMLSerializer<Collections> ser = makeSerializer(Collections.class);
			ser.deserialize(doc);
		} catch (Exception e) {
			assertTrue(e instanceof SerializerException);
			return;
		}
		throw new AssertionFailedError();
	}

	//

	private Document getXMLDocument(int resId) throws Exception {
		String xml = IOUtils.readToString(getActivity().getResources().openRawResource(resId));
		return XMLSerializer.parseDocument(xml);
	}

	protected final <T extends Model> XMLSerializer<T> makeSerializer(Class<T> cls) {
		return new XMLSerializer<T>(cls, getActivity());
	}

}
