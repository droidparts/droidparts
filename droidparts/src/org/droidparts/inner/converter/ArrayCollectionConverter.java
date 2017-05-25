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
package org.droidparts.inner.converter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.droidparts.inner.ConverterRegistry;
import org.droidparts.inner.PersistUtils;
import org.droidparts.inner.TypeHelper;
import org.droidparts.model.Model;
import org.droidparts.util.Arrays2;
import org.droidparts.util.Strings;

import static org.droidparts.inner.ReflectionUtils.newInstance;
import static org.droidparts.inner.TypeHelper.isArray;
import static org.droidparts.inner.TypeHelper.isInterface;
import static org.droidparts.inner.TypeHelper.isModel;
import static org.droidparts.inner.TypeHelper.isSet;
import static org.droidparts.util.Strings.isNotEmpty;

public class ArrayCollectionConverter extends Converter<Object> {

	// ASCII RS (record separator), '|' for readability
	private static final String SEP = "|" + (char) 30;

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isArray(cls) || TypeHelper.isCollection(cls);
	}

	@Override
	public String getDBColumnType() {
		return BLOB;
	}

	@Override
	public <G1, G2> Object readFromJSON(Class<Object> valType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                    JSONObject obj, String key) throws Exception {
		Converter<JSONArray> conv = ConverterRegistry.getConverter(JSONArray.class);
		Wrapper w = new Wrapper(conv.readFromJSON(JSONArray.class, null, null, obj, key), null);
		return readFromWrapper(valType, genericArg1, w);
	}

	@Override
	public <G1, G2> Object readFromXML(Class<Object> valType, Class<G1> genericArg1, Class<G2> genericArg2, Node node,
	                                   String nodeListItemTagHint) throws Exception {
		NodeList nl = node.getChildNodes();
		ArrayList<Node> elementNodes = new ArrayList<Node>();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				if (isNotEmpty(nodeListItemTagHint)) {
					if (nodeListItemTagHint.equals(n.getNodeName())) {
						elementNodes.add(n);
					}
				} else {
					elementNodes.add(n);
				}
			}
		}
		Wrapper w = new Wrapper(null, elementNodes);
		return readFromWrapper(valType, genericArg1, w);
	}

	@Override
	public <G1, G2> void putToJSON(Class<Object> valType, Class<G1> genericArg1, Class<G2> genericArg2, JSONObject obj,
	                               String key, Object val) throws Exception {
		Converter<G1> converter = ConverterRegistry.getConverter(genericArg1);
		ArrayList<G1> list = arrOrCollToList(valType, genericArg1, val);
		JSONArray vals = new JSONArray();
		JSONObject tmpObj = new JSONObject();
		for (G1 value : list) {
			converter.putToJSON(genericArg1, null, null, tmpObj, "key", value);
			vals.put(tmpObj.get("key"));
		}
		obj.put(key, vals);
	}

	//
	@SuppressWarnings("unchecked")
	protected <V> Object readFromWrapper(Class<Object> valType, Class<V> genericArg1, Wrapper wrapper)
			throws Exception {
		boolean isArr = isArray(valType);
		boolean isModel = isModel(genericArg1);
		Collection<Object> items;
		if (isArr) {
			items = new ArrayList<Object>();
		} else {
			items = makeCollection(valType);
		}
		Converter<V> converter = ConverterRegistry.getConverter(genericArg1);
		for (int i = 0; i < wrapper.size(); i++) {
			Object item = wrapper.get(i);
			if (isModel) {
				item = wrapper.deserialize(item, converter, genericArg1);
			} else {
				item = wrapper.convert(item, converter, genericArg1);
			}
			items.add(item);
		}
		if (isArr) {
			Object[] arr = items.toArray();
			if (isModel) {
				Object modelArr = Array.newInstance(genericArg1, arr.length);
				for (int i = 0; i < arr.length; i++) {
					Array.set(modelArr, i, arr[i]);
				}
				return modelArr;
			} else {
				String[] arr2 = new String[arr.length];
				for (int i = 0; i < arr.length; i++) {
					arr2[i] = arr[i].toString();
				}
				return parseTypeArr(converter, genericArg1, arr2);
			}
		} else {
			return items;
		}
	}

	@Override
	public <G1, G2> void putToContentValues(Class<Object> valueType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                        ContentValues cv, String key, Object val) throws Exception {
		Converter<G1> converter = ConverterRegistry.getConverter(genericArg1);
		if (converter.getDBColumnType() == BLOB) {
			byte[] bytes = PersistUtils.toBytes(val);
			cv.put(key, bytes);
		} else {
			ArrayList<G1> list = arrOrCollToList(valueType, genericArg1, val);
			ArrayList<Object> vals = new ArrayList<Object>();
			ContentValues tmpCV = new ContentValues();
			for (G1 obj : list) {
				converter.putToContentValues(genericArg1, null, null, tmpCV, "key", obj);
				vals.add(tmpCV.get("key"));
			}
			String strVal = Strings.join(vals, SEP);
			cv.put(key, strVal);
		}
	}

	@Override
	public <G1, G2> Object readFromCursor(Class<Object> valType, Class<G1> genericArg1, Class<G2> genericArg2,
	                                      Cursor cursor, int columnIndex) throws Exception {
		Converter<G1> converter = ConverterRegistry.getConverter(genericArg1);
		if (converter.getDBColumnType() == BLOB) {
			byte[] arr = cursor.getBlob(columnIndex);
			return (arr != null) ? PersistUtils.fromBytes(arr) : null;
		} else {
			String str = cursor.getString(columnIndex);
			String[] parts = (str.length() > 0) ? str.split("\\" + SEP) : new String[0];
			if (isArray(valType)) {
				return parseTypeArr(converter, genericArg1, parts);
			} else {
				return parseTypeColl(converter, valType, genericArg1, parts);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> ArrayList<T> arrOrCollToList(Class<?> valType, Class<T> genericArg1, Object val) {
		ArrayList<T> list = new ArrayList<T>();
		if (isArray(valType)) {
			list.addAll((List<T>) Arrays.asList(Arrays2.toObjectArray(val)));
		} else {
			list.addAll((Collection<T>) val);
		}
		return list;
	}

	// say hello to arrays of primitives
	private final <T> Object parseTypeArr(Converter<T> converter, Class<T> valType, String[] arr) throws Exception {
		Object objArr = Array.newInstance(valType, arr.length);
		for (int i = 0; i < arr.length; i++) {
			T item = converter.parseFromString(valType, null, null, arr[i]);
			Array.set(objArr, i, item);
		}
		return objArr;
	}

	private final <T> Collection<T> parseTypeColl(Converter<T> converter, Class<Object> collType, Class<T> genericArg1,
	                                              String[] arr) throws Exception {
		Collection<T> coll = makeCollection(collType);
		for (int i = 0; i < arr.length; i++) {
			T item = converter.parseFromString(genericArg1, null, null, arr[i]);
			coll.add(item);
		}
		return coll;
	}

	@SuppressWarnings("unchecked")
	private static <T> Collection<T> makeCollection(Class<Object> collCls) {
		if (!isInterface(collCls)) {
			return (Collection<T>) newInstance(collCls);
		} else if (isSet(collCls)) {
			return new LinkedHashSet<T>();
		} else {
			return new ArrayList<T>();
		}
	}

	//

	private static class Wrapper {
		private final JSONArray arr;
		private final ArrayList<Node> nodes;

		private final JSONObject tmp = new JSONObject();

		Wrapper(JSONArray arr, ArrayList<Node> nodes) {
			this.arr = arr;
			this.nodes = nodes;
		}

		int size() {
			return isJSON() ? arr.length() : nodes.size();
		}

		<V> Object get(int i) throws Exception {
			if (isJSON()) {
				return arr.get(i);
			} else {
				return nodes.get(i);
			}
		}

		<V> Object convert(Object item, Converter<V> conv, Class<V> valType) throws Exception {
			if (isJSON()) {
				if (item.getClass() == valType) {
					return item;
				} else {
					tmp.put("key", item);
					return conv.readFromJSON(valType, null, null, tmp, "key");
				}
			} else {
				String txt = PersistUtils.getNodeText((Node) item);
				return conv.parseFromString(valType, null, null, txt);
			}
		}

		@SuppressWarnings("unchecked")
		<V, M extends Model> Object deserialize(Object item, Converter<V> conv, Class<V> valType) throws Exception {
			ModelConverter<M> mc = (ModelConverter<M>) conv;
			Class<M> cl = (Class<M>) valType;
			if (isJSON()) {
				JSONObject obj = (JSONObject) item;
				return mc.getJSONSerializer(cl, obj).deserialize(obj);
			} else {
				Node node = (Node) item;
				return mc.getXMLSerializer(cl, node).deserialize(node);
			}
		}

		private boolean isJSON() {
			return (arr != null);
		}

	}

}
