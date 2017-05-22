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
package org.droidparts.persist.serializer;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.util.Pair;

import org.droidparts.Injector;
import org.droidparts.model.Model;
import org.droidparts.util.L;

public abstract class AbstractSerializer<ModelType extends Model, Obj, Arr> {

	// ASCII GS (group separator), '->' for readability
	public static final String SUB = "->" + (char) 29;

	protected final Class<ModelType> cls;
	private Context ctx;

	protected AbstractSerializer(Class<ModelType> cls, Context ctx) {
		this.cls = cls;
		if (ctx != null) {
			this.ctx = ctx.getApplicationContext();
			Injector.inject(ctx, this);
		}
	}

	protected Context getContext() {
		return ctx;
	}

	public abstract ModelType deserialize(Obj obj) throws Exception;

	public abstract ArrayList<ModelType> deserializeAll(Arr obj) throws Exception;

	protected static Pair<String, String> getNestedKeyParts(String key) {
		int firstSep = key.indexOf(SUB);
		if (firstSep != -1) {
			String subKey = key.substring(0, firstSep);
			String leftKey = key.substring(firstSep + SUB.length());
			Pair<String, String> pair = Pair.create(subKey, leftKey);
			return pair;
		} else {
			return null;
		}
	}

	protected static void logOrThrow(Object src, boolean optional, String part, Exception e)
			throws SerializerException {
		ArrayList<String> parts = new ArrayList<String>();
		if (e instanceof SerializerException) {
			parts.addAll(Arrays.asList(((SerializerException) e).getParts()));
		} else {
			parts.add(e.getMessage());
		}
		parts.add(0, part);
		String[] arr = parts.toArray(new String[parts.size()]);
		if (optional) {
			L.d(SerializerException.createMessage(src, arr));
		} else {
			throw new SerializerException(src, arr);
		}
	}

}