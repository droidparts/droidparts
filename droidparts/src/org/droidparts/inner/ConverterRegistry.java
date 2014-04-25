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

import java.util.ArrayList;
import java.util.HashMap;

import org.droidparts.inner.converter.ArrayCollectionConverter;
import org.droidparts.inner.converter.BitmapConverter;
import org.droidparts.inner.converter.BooleanConverter;
import org.droidparts.inner.converter.ByteArrayConverter;
import org.droidparts.inner.converter.ByteConverter;
import org.droidparts.inner.converter.CharacterConverter;
import org.droidparts.inner.converter.Converter;
import org.droidparts.inner.converter.DateConverter;
import org.droidparts.inner.converter.DoubleConverter;
import org.droidparts.inner.converter.EntityConverter;
import org.droidparts.inner.converter.EnumConverter;
import org.droidparts.inner.converter.FloatConverter;
import org.droidparts.inner.converter.IntegerConverter;
import org.droidparts.inner.converter.JSONArrayConverter;
import org.droidparts.inner.converter.JSONObjectConverter;
import org.droidparts.inner.converter.LongConverter;
import org.droidparts.inner.converter.ModelConverter;
import org.droidparts.inner.converter.ShortConverter;
import org.droidparts.inner.converter.StringConverter;
import org.droidparts.inner.converter.UUIDConverter;
import org.droidparts.inner.converter.UriConverter;

public class ConverterRegistry {

	private static final ArrayList<Converter<?>> converters = new ArrayList<Converter<?>>();

	private static final HashMap<Class<?>, Converter<?>> map = new HashMap<Class<?>, Converter<?>>();

	static {
		registerConverter(new BooleanConverter());
		registerConverter(new ByteConverter());
		registerConverter(new CharacterConverter());
		registerConverter(new DoubleConverter());
		registerConverter(new FloatConverter());
		registerConverter(new IntegerConverter());
		registerConverter(new LongConverter());
		registerConverter(new ShortConverter());
		registerConverter(new StringConverter());
		registerConverter(new EnumConverter());
		registerConverter(new DateConverter());
		registerConverter(new UUIDConverter());
		registerConverter(new UriConverter());
		registerConverter(new ByteArrayConverter());
		registerConverter(new JSONObjectConverter());
		registerConverter(new JSONArrayConverter());
		registerConverter(new BitmapConverter());
		registerConverter(new ModelConverter());
		registerConverter(new EntityConverter());
		registerConverter(new ArrayCollectionConverter());
	}

	public static void registerConverter(Converter<?> converter) {
		converters.add(converter);
	}

	@SuppressWarnings("unchecked")
	public static <T> Converter<T> getConverter(Class<T> cls)
			throws IllegalArgumentException {
		Converter<?> converter = map.get(cls);
		if (converter == null) {
			for (Converter<?> conv : converters) {
				if (conv.canHandle(cls)) {
					converter = conv;
					map.put(cls, converter);
					break;
				}
			}
		}
		if (converter != null) {
			return (Converter<T>) converter;
		} else {
			throw new IllegalArgumentException("No converter for '"
					+ cls.getName() + "'.");
		}
	}

	private ConverterRegistry() {
	}

}
