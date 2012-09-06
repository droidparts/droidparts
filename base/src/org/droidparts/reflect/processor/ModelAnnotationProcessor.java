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

import org.droidparts.annotation.json.Key;
import org.droidparts.model.Model;
import org.droidparts.reflect.model.ModelField;

public class ModelAnnotationProcessor extends
		AbstractAnnotationProcessor<ModelField> {

	public ModelAnnotationProcessor(Class<? extends Model> cls) {
		super(cls);
	}

	@Override
	protected String modelClassName() {
		org.droidparts.annotation.json.Object ann = cls
				.getAnnotation(org.droidparts.annotation.json.Object.class);
		if (ann != null) {
			return ann.value();
		} else {
			return cls.getSimpleName();
		}
	}

	@Override
	protected ModelField[] modelClassFields() {
		ArrayList<ModelField> list = new ArrayList<ModelField>();
		for (Field field : getClassHierarchyFields()) {
			Key keyAnn = field.getAnnotation(Key.class);
			if (keyAnn != null) {
				ModelField jsonField = new ModelField();
				fillField(field, jsonField);
				jsonField.keyName = getKeyName(keyAnn, field);
				jsonField.keyOptional = keyAnn.optional();
				list.add(jsonField);
			}
		}
		return list.toArray(new ModelField[list.size()]);
	}

	private String getKeyName(Key ann, Field field) {
		String name = ann.name();
		if (isEmpty(name)) {
			name = field.getName();
		}
		return name;
	}

}
