package org.droidparts.test.testcase;

import static org.droidparts.persist.json.JSONSerializer.__;
import static org.droidparts.util.Strings.join;

import java.util.ArrayList;

import org.droidparts.persist.json.JSONSerializer;
import org.droidparts.test.R;
import org.droidparts.test.model.Album;
import org.droidparts.test.model.Nested;
import org.droidparts.test.model.Primitives;
import org.droidparts.test.persist.json.AlbumSerializer;
import org.droidparts.util.AppUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.test.AndroidTestCase;

public class JSONTestCase extends AndroidTestCase {

	public void testPrimitives() throws Exception {
		JSONSerializer<Primitives> serializer = new JSONSerializer<Primitives>(
				getContext(), Primitives.class);
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
		assertEquals("obj->key", join(new String[] { "obj", "key" }, __, null));
		JSONSerializer<Nested> serializer = new JSONSerializer<Nested>(
				getContext(), Nested.class);
		Nested model = serializer.deserialize(getNested());
		assertEquals("str", model.str);
		JSONObject obj = serializer.serialize(model);
		assertEquals("str", obj.getJSONObject("sub_obj").getString("str"));
	}

	//
	private JSONObject getPrimitives() throws Exception {
		String str = new AppUtils(getContext())
				.readStringResource(R.raw.primitives);
		return new JSONObject(str);
	}

	private JSONObject getNested() throws Exception {
		String str = new AppUtils(getContext())
				.readStringResource(R.raw.nested);
		return new JSONObject(str);
	}

	private JSONArray getAlbums() throws Exception {
		String str = new AppUtils(getContext())
				.readStringResource(R.raw.albums);
		return new JSONArray(str);
	}

}
