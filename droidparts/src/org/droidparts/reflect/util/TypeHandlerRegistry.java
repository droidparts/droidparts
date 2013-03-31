/**
 * Copyright 2013 Alex Yanchenko
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
package org.droidparts.reflect.util;

import java.util.HashMap;
import java.util.HashSet;

import org.droidparts.reflect.type.ArrayCollectionHandler;
import org.droidparts.reflect.type.BitmapHandler;
import org.droidparts.reflect.type.BooleanHandler;
import org.droidparts.reflect.type.ByteArrayHandler;
import org.droidparts.reflect.type.ByteHandler;
import org.droidparts.reflect.type.CharacterHandler;
import org.droidparts.reflect.type.DateHandler;
import org.droidparts.reflect.type.DoubleHandler;
import org.droidparts.reflect.type.EntityHandler;
import org.droidparts.reflect.type.EnumHandler;
import org.droidparts.reflect.type.FloatHandler;
import org.droidparts.reflect.type.IntegerHandler;
import org.droidparts.reflect.type.JSONArrayHandler;
import org.droidparts.reflect.type.JSONObjectHandler;
import org.droidparts.reflect.type.LongHandler;
import org.droidparts.reflect.type.ModelHandler;
import org.droidparts.reflect.type.ShortHandler;
import org.droidparts.reflect.type.StringHandler;
import org.droidparts.reflect.type.TypeHandler;
import org.droidparts.reflect.type.UUIDHandler;
import org.droidparts.reflect.type.UriHandler;

public class TypeHandlerRegistry {

	private static final HashSet<TypeHandler<?>> handlers = new HashSet<TypeHandler<?>>();

	private static final HashMap<Class<?>, TypeHandler<?>> map = new HashMap<Class<?>, TypeHandler<?>>();

	static {
		handlers.add(new BooleanHandler());
		handlers.add(new ByteHandler());
		handlers.add(new CharacterHandler());
		handlers.add(new DoubleHandler());
		handlers.add(new FloatHandler());
		handlers.add(new IntegerHandler());
		handlers.add(new LongHandler());
		handlers.add(new ShortHandler());
		handlers.add(new StringHandler());
		handlers.add(new EnumHandler());
		handlers.add(new DateHandler());
		handlers.add(new UUIDHandler());
		handlers.add(new UriHandler());
		handlers.add(new ByteArrayHandler());
		handlers.add(new JSONObjectHandler());
		handlers.add(new JSONArrayHandler());
		handlers.add(new BitmapHandler());
		handlers.add(new ModelHandler());
		handlers.add(new EntityHandler());
		handlers.add(new ArrayCollectionHandler());
	}

	@SuppressWarnings("unchecked")
	public static <T> TypeHandler<T> getHandler(Class<T> cls)
			throws IllegalArgumentException {
		TypeHandler<?> handler = map.get(cls);
		if (handler == null) {
			for (TypeHandler<?> h : handlers) {
				if (h.canHandle(cls)) {
					handler = h;
					map.put(cls, handler);
					break;
				}
			}
		}
		return (TypeHandler<T>) handler;
	}

	private TypeHandlerRegistry() {
	}

}
