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
package org.droidparts.test.testcase.serialize;

import android.test.AndroidTestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import org.droidparts.model.Model;
import org.droidparts.persist.serializer.JSONSerializer;
import org.droidparts.util.ResourceUtils;

abstract class AbstractJSONTestCase extends AndroidTestCase {

	protected final JSONObject getJSONObject(int resId) throws Exception {
		return new JSONObject(getJSONString(resId));
	}

	protected final JSONArray getJSONArray(int resId) throws Exception {
		return new JSONArray(getJSONString(resId));
	}

	protected final String getJSONString(int resId) {
		return ResourceUtils.readRawResource(getContext(), resId);
	}

	protected final <T extends Model> JSONSerializer<T> makeSerializer(Class<T> cls) {
		return new JSONSerializer<T>(cls, getContext());
	}

}
