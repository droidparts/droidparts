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

import static org.droidparts.util.Strings.join;

import java.util.ArrayList;

import org.droidparts.annotation.json.Key;
import org.droidparts.persist.json.JSONSerializer;
import org.droidparts.test.R;
import org.droidparts.test.model.Album;
import org.droidparts.test.model.Nested;
import org.droidparts.test.model.Primitives;
import org.droidparts.test.persist.json.AlbumSerializer;
import org.droidparts.util.ResourceUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.test.AndroidTestCase;

public class JSONTestCase extends AndroidTestCase {

	public void testPrimitives() throws Exception {
		JSONSerializer<Primitives> serializer = new JSONSerializer<Primitives>(
				Primitives.class, getContext());
		Primitives primitives = serializer.deserialize(getPrimitives());
		assertNotNull(primitives.strArr);
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
		AlbumSerializer serializer = new AlbumSerializer(getContext());
		ArrayList<Album> albums = serializer.deserialize(getAlbums());
		assertEquals(2, albums.size());
		assertEquals("Diamond", albums.get(0).name);
		assertEquals(2009, albums.get(1).year);
	}

	public void testNestedKeys() throws Exception {
		assertEquals("obj->key", join(new String[] { "obj", "key" }, Key.SUB));
		JSONSerializer<Nested> serializer = new JSONSerializer<Nested>(
				Nested.class, getContext());
		Nested model = serializer.deserialize(getNested());
		assertEquals("str", model.str);
		JSONObject obj = serializer.serialize(model);
		assertEquals("str", obj.getJSONObject("sub_obj").getString("str"));
	}

	//
	private JSONObject getPrimitives() throws Exception {
		String str = ResourceUtils.readRawResource(getContext(),
				R.raw.primitives);
		return new JSONObject(str);
	}

	private JSONObject getNested() throws Exception {
		String str = ResourceUtils.readRawResource(getContext(), R.raw.nested);
		return new JSONObject(str);
	}

	private JSONArray getAlbums() throws Exception {
		String str = ResourceUtils.readRawResource(getContext(), R.raw.albums);
		return new JSONArray(str);
	}

}
