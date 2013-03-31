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
package org.droidparts.type;

import java.util.HashMap;
import java.util.HashSet;

import org.droidparts.type.handler.ArrayCollectionHandler;
import org.droidparts.type.handler.BitmapHandler;
import org.droidparts.type.handler.BooleanHandler;
import org.droidparts.type.handler.ByteArrayHandler;
import org.droidparts.type.handler.ByteHandler;
import org.droidparts.type.handler.CharacterHandler;
import org.droidparts.type.handler.DateHandler;
import org.droidparts.type.handler.DoubleHandler;
import org.droidparts.type.handler.EntityHandler;
import org.droidparts.type.handler.EnumHandler;
import org.droidparts.type.handler.FloatHandler;
import org.droidparts.type.handler.IntegerHandler;
import org.droidparts.type.handler.JSONArrayHandler;
import org.droidparts.type.handler.JSONObjectHandler;
import org.droidparts.type.handler.LongHandler;
import org.droidparts.type.handler.ModelHandler;
import org.droidparts.type.handler.ShortHandler;
import org.droidparts.type.handler.StringHandler;
import org.droidparts.type.handler.AbstractTypeHandler;
import org.droidparts.type.handler.UUIDHandler;
import org.droidparts.type.handler.UriHandler;

public class TypeHandlerRegistry {

	private static final HashSet<AbstractTypeHandler<?>> handlers = new HashSet<AbstractTypeHandler<?>>();

	private static final HashMap<Class<?>, AbstractTypeHandler<?>> map = new HashMap<Class<?>, AbstractTypeHandler<?>>();

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
	public static <T> AbstractTypeHandler<T> getHandler(Class<T> cls)
			throws IllegalArgumentException {
		AbstractTypeHandler<?> handler = map.get(cls);
		if (handler == null) {
			for (AbstractTypeHandler<?> h : handlers) {
				if (h.canHandle(cls)) {
					handler = h;
					map.put(cls, handler);
					break;
				}
			}
		}
		return (AbstractTypeHandler<T>) handler;
	}

	public static <T> AbstractTypeHandler<T> getHandlerOrThrow(Class<T> cls) {
		AbstractTypeHandler<T> handler = getHandler(cls);
		if (handler != null) {
			return handler;
		} else {
			throw new IllegalArgumentException("No handler for '"
					+ cls.getName() + "'.");
		}
	}

	private TypeHandlerRegistry() {
	}

}
