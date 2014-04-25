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
package org.droidparts.inner;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.droidparts.model.Entity;
import org.droidparts.model.Model;
import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.Preference;
import android.view.View;

public final class TypeHelper {

	public static boolean isBoolean(Class<?> cls, boolean orWrapper) {
		return (cls == boolean.class) ? true
				: orWrapper ? (cls == Boolean.class) : false;
	}

	public static boolean isInteger(Class<?> cls, boolean orWrapper) {
		return (cls == int.class) ? true : orWrapper ? (cls == Integer.class)
				: false;
	}

	public static boolean isLong(Class<?> cls, boolean orWrapper) {
		return (cls == long.class) ? true : orWrapper ? (cls == Long.class)
				: false;
	}

	public static boolean isFloat(Class<?> cls, boolean orWrapper) {
		return (cls == float.class) ? true : orWrapper ? (cls == Float.class)
				: false;
	}

	public static boolean isDouble(Class<?> cls, boolean orWrapper) {
		return (cls == double.class) ? true : orWrapper ? (cls == Double.class)
				: false;
	}

	public static boolean isByte(Class<?> cls, boolean orWrapper) {
		return (cls == byte.class) ? true : orWrapper ? (cls == Byte.class)
				: false;
	}

	public static boolean isShort(Class<?> cls, boolean orWrapper) {
		return (cls == short.class) ? true : orWrapper ? (cls == Short.class)
				: false;
	}

	public static boolean isCharacter(Class<?> cls, boolean orWrapper) {
		return (cls == char.class) ? true
				: orWrapper ? (cls == Character.class) : false;
	}

	//

	public static boolean isString(Class<?> cls) {
		return cls == String.class;
	}

	public static boolean isEnum(Class<?> cls) {
		return cls.isEnum();
	}

	public static boolean isUUID(Class<?> cls) {
		return UUID.class.isAssignableFrom(cls);
	}

	public static boolean isUri(Class<?> cls) {
		return Uri.class.isAssignableFrom(cls);
	}

	public static boolean isDate(Class<?> cls) {
		return Date.class.isAssignableFrom(cls);
	}

	//

	public static boolean isByteArray(Class<?> cls) {
		return cls == byte[].class;
	}

	public static boolean isArray(Class<?> cls) {
		return cls.isArray();
	}

	public static boolean isCollection(Class<?> cls) {
		return Collection.class.isAssignableFrom(cls);
	}

	//

	public static boolean isBitmap(Class<?> cls) {
		return Bitmap.class.isAssignableFrom(cls);
	}

	public static boolean isDrawable(Class<?> cls) {
		return Drawable.class.isAssignableFrom(cls);
	}

	//

	public static boolean isJSONObject(Class<?> cls) {
		return JSONObject.class.isAssignableFrom(cls);
	}

	public static boolean isJSONArray(Class<?> cls) {
		return JSONArray.class.isAssignableFrom(cls);
	}

	//

	public static boolean isView(Class<?> cls) {
		return View.class.isAssignableFrom(cls);

	}

	public static boolean isPreference(Class<?> cls) {
		return Preference.class.isAssignableFrom(cls);
	}

	//

	public static boolean isModel(Class<?> cls) {
		return Model.class.isAssignableFrom(cls);
	}

	public static boolean isEntity(Class<?> cls) {
		return Entity.class.isAssignableFrom(cls);
	}

	protected TypeHelper() {
	}
}
