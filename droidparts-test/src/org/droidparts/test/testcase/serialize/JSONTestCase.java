/**
 * Copyright 2015 Alex Yanchenko
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

import static org.droidparts.util.Strings.join;

import java.util.ArrayList;

import org.droidparts.annotation.serialize.JSON;
import org.droidparts.persist.serializer.JSONSerializer;
import org.droidparts.persist.serializer.SerializerException;
import org.droidparts.test.R;
import org.droidparts.test.model.Album;
import org.droidparts.test.model.Collections;
import org.droidparts.test.model.Nested;
import org.droidparts.test.model.Primitives;
import org.json.JSONArray;
import org.json.JSONObject;

import android.test.AssertionFailedError;

public class JSONTestCase extends AbstractJSONTestCase {

	public void testPrimitives() throws Exception {
		JSONSerializer<Primitives> serializer = makeSerializer(Primitives.class);
		Primitives primitives = serializer.deserialize(getPrimitives());
		assertNotNull(primitives.strArr);
		assertEquals(9000, (long) primitives.longList.get(0));
		//
		JSONObject obj = serializer.serialize(primitives);
		//
		assertEquals(1, obj.getInt("int1"));
		assertEquals(2, obj.getInt("int2"));
		assertEquals(0.5, obj.getDouble("float1"));
		assertEquals(2.5, obj.getDouble("float2"));
		assertEquals(true, obj.getBoolean("boolean1"));
		assertEquals(true, obj.getBoolean("boolean2"));
		assertEquals(true, obj.getBoolean("boolean3"));
		assertEquals(false, obj.getBoolean("boolean4"));
		assertEquals("str", obj.getString("string1"));
		assertEquals(2, obj.getJSONArray("string_array").length());
		assertEquals("two", obj.getJSONArray("string_array").getString(1));
	}

	public void testAlbums() throws Exception {
		JSONSerializer<Album> serializer = makeSerializer(Album.class);
		ArrayList<Album> albums = serializer.deserializeAll(getAlbums());
		assertEquals(2, albums.size());
		assertEquals("Diamond", albums.get(0).name);
		assertEquals(2009, albums.get(1).year);
	}

	public void testNestedKeys() throws Exception {
		assertEquals("obj->key", join(new String[] { "obj", "key" }, JSON.SUB));
		JSONSerializer<Nested> serializer = makeSerializer(Nested.class);
		Nested model = serializer.deserialize(getNested());
		assertEquals("str", model.str);
		JSONObject obj = serializer.serialize(model);
		assertEquals("str", obj.getJSONObject("sub_obj").getString("str"));
	}

	//

	public void testCollections() throws Exception {
		JSONSerializer<Collections> ser = makeSerializer(Collections.class);
		Collections coll = ser.deserialize(getJSONObject(R.raw.collections_json));
		assertEquals(2, coll.albumsArr.length);
		assertEquals(2, coll.albumsColl.size());
		//
		assertEquals(2, coll.map.size());
		assertTrue(coll.map.containsKey(2009));
		assertEquals("Iris", coll.map.get(2009));
		//
		JSONObject obj = ser.serialize(coll);
		assertTrue(obj.has("map"));
	}

	public void testCollectionsFail() throws Exception {
		try {
			JSONSerializer<Collections> ser = makeSerializer(Collections.class);
			ser.deserialize(getJSONObject(R.raw.albums_partial_json));
		} catch (Exception e) {
			assertTrue(e instanceof SerializerException);
			return;
		}
		throw new AssertionFailedError();
	}

	//

	private JSONObject getPrimitives() throws Exception {
		return getJSONObject(R.raw.primitives);
	}

	private JSONObject getNested() throws Exception {
		return getJSONObject(R.raw.nested);
	}

	private JSONArray getAlbums() throws Exception {
		return getJSONArray(R.raw.albums_json);
	}

}
