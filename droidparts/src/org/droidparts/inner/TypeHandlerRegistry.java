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
package org.droidparts.inner;

import java.util.HashMap;
import java.util.HashSet;

import org.droidparts.inner.handler.AbstractTypeHandler;
import org.droidparts.inner.handler.ArrayCollectionHandler;
import org.droidparts.inner.handler.BitmapHandler;
import org.droidparts.inner.handler.BooleanHandler;
import org.droidparts.inner.handler.ByteArrayHandler;
import org.droidparts.inner.handler.ByteHandler;
import org.droidparts.inner.handler.CharacterHandler;
import org.droidparts.inner.handler.DateHandler;
import org.droidparts.inner.handler.DoubleHandler;
import org.droidparts.inner.handler.EntityHandler;
import org.droidparts.inner.handler.EnumHandler;
import org.droidparts.inner.handler.FloatHandler;
import org.droidparts.inner.handler.IntegerHandler;
import org.droidparts.inner.handler.JSONArrayHandler;
import org.droidparts.inner.handler.JSONObjectHandler;
import org.droidparts.inner.handler.LongHandler;
import org.droidparts.inner.handler.ModelHandler;
import org.droidparts.inner.handler.ShortHandler;
import org.droidparts.inner.handler.StringHandler;
import org.droidparts.inner.handler.UUIDHandler;
import org.droidparts.inner.handler.UriHandler;

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
