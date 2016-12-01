/**
 * Copyright 2016 Alex Yanchenko
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
package org.droidparts.test.testcase.serialize;

import java.util.ArrayList;

import org.json.JSONObject;

import org.droidparts.annotation.serialize.JSON;
import org.droidparts.inner.ConverterRegistry;
import org.droidparts.inner.converter.ModelConverter;
import org.droidparts.model.Model;
import org.droidparts.persist.serializer.JSONSerializer;
import org.droidparts.test.R;

public class CustomSerializerJSONTestCase extends AbstractJSONTestCase {

	public void testAlbumCollection() throws Exception {
		JSONSerializer<AlbumCollection> ser = makeSerializer(AlbumCollection.class);
		JSONObject obj = getJSONObject(R.raw.albums_partial_json);
		ConverterRegistry.registerConverter(new AbstractAlbum.Converter());
		AlbumCollection ac = ser.deserialize(obj);
		assertEquals(2, ac.albums.size());
		assertTrue(ac.albums.get(0) instanceof NameAlbum);
		assertTrue(ac.albums.get(1) instanceof YearAlbum);
	}

	public static class AlbumCollection extends Model {
		private static final long serialVersionUID = 1L;
		@JSON
		public ArrayList<AbstractAlbum> albums;
	}

	public static abstract class AbstractAlbum extends Model {
		private static final long serialVersionUID = 1L;

		public static class Converter extends ModelConverter<AbstractAlbum> {

			@Override
			public boolean canHandle(Class<?> cls) {
				return cls == AbstractAlbum.class;
			}

			@Override
			protected JSONSerializer<? extends AbstractAlbum> getJSONSerializer(Class<AbstractAlbum> valType,
			                                                                    JSONObject src) {
				if (src.has("name")) {
					return new JSONSerializer<NameAlbum>(NameAlbum.class, null);
				} else if (src.has("year")) {
					return new JSONSerializer<YearAlbum>(YearAlbum.class, null);
				} else {
					throw new IllegalArgumentException();
				}
			}

		}
	}

	public static class NameAlbum extends AbstractAlbum {
		private static final long serialVersionUID = 1L;
		@JSON
		public String name;
	}

	public static class YearAlbum extends AbstractAlbum {
		private static final long serialVersionUID = 1L;
		@JSON
		public int year;
	}

}
