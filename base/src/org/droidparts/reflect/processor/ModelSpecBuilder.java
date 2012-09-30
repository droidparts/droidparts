/**
 * Copyright 2012 Alex Yanchenko
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
package org.droidparts.reflect.processor;

import static org.droidparts.util.Strings.isEmpty;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.droidparts.model.Model;
import org.droidparts.reflect.model.json.ModelSpec;
import org.droidparts.reflect.model.json.ann.KeyAnn;
import org.droidparts.reflect.model.json.ann.ObjectAnn;
import org.droidparts.reflect.util.AnnUtil;

public class ModelAnnotationProcessor extends
		AbstractAnnotationProcessor<ModelSpec> {

	public ModelAnnotationProcessor(Class<? extends Model> cls) {
		super(cls);
	}

	@Override
	protected String modelClassName() {
		String name = null;
		ObjectAnn ann = (ObjectAnn) AnnUtil.getClassAnn(cls, ObjectAnn.class);
		if (ann != null) {
			name = ann.name;
		}
		if (isEmpty(name)) {
			name = cls.getSimpleName();
		}
		return name;
	}

	@Override
	protected ModelSpec[] modelClassFields() {
		ArrayList<ModelSpec> list = new ArrayList<ModelSpec>();
		for (Field field : getClassHierarchyFields()) {
			KeyAnn keyAnn = (KeyAnn) AnnUtil.getFieldAnn(cls, field,
					KeyAnn.class);
			if (keyAnn != null) {
				ModelSpec spec = new ModelSpec();
				spec.field = field;
				spec.multiFieldArgType = getMultiFieldArgType(field);
				spec.key.name = getKeyName(keyAnn, field);
				spec.key.optional = keyAnn.optional;
				list.add(spec);
			}
		}
		return list.toArray(new ModelSpec[list.size()]);
	}

	private String getKeyName(KeyAnn ann, Field field) {
		String name = ann.name;
		if (isEmpty(name)) {
			name = field.getName();
		}
		return name;
	}

}
