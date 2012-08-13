package org.droidparts.test.testcase;

import java.util.ArrayList;

import org.droidparts.R;
import org.droidparts.serializer.json.JSONSerializer;
import org.droidparts.test.model.Phone;
import org.droidparts.test.model.Primitives;
import org.droidparts.test.serializer.PhoneSerializer;
import org.droidparts.util.AppUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.test.AndroidTestCase;

public class JSONTestCase extends AndroidTestCase {

	public void testPrimitives() throws Exception {
		JSONSerializer<Primitives> serializer = new JSONSerializer<Primitives>(
				Primitives.class);
		Primitives primitives = serializer.deserialize(getPrimitives());
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
	}

	public void testPhones() throws Exception {
		PhoneSerializer serializer = new PhoneSerializer();
		ArrayList<Phone> phones = serializer.deserializeList(getPhones());
		assertEquals(2, phones.size());
		assertEquals("Galaxy Nexus", phones.get(0).name);
		assertEquals(5.1f, phones.get(1).version);
	}

	public void testNestedKeys() throws Exception {
		assertEquals(0, JSONSerializer.buildNestedKey().length());
		assertEquals("obj->key", JSONSerializer.buildNestedKey("obj", "key"));
	}

	//

	private JSONObject getPrimitives() throws Exception {
		String str = new AppUtils(getContext())
				.readStringResource(R.raw.primitives);
		return new JSONObject(str);
	}

	private JSONArray getPhones() throws Exception {
		String str = new AppUtils(getContext())
				.readStringResource(R.raw.phones);
		return new JSONArray(str);
	}

}
