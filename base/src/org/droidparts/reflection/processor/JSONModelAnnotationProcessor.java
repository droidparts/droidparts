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
package org.droidparts.reflection.processor;

import static org.droidparts.util.Strings.isEmpty;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.droidparts.annotation.json.Key;
import org.droidparts.model.Model;
import org.droidparts.reflection.model.JSONModelField;

public class JSONModelAnnotationProcessor extends
		ModelAnnotationProcessor<JSONModelField> {

	public JSONModelAnnotationProcessor(Class<? extends Model> cls) {
		super(cls);
	}

	@Override
	public String getModelClassName() {
		org.droidparts.annotation.json.Object ann = cls
				.getAnnotation(org.droidparts.annotation.json.Object.class);
		if (ann != null) {
			return ann.value();
		} else {
			return cls.getSimpleName();
		}
	}

	@Override
	public JSONModelField[] getModelClassFields() {
		ArrayList<JSONModelField> list = new ArrayList<JSONModelField>();
		for (Field field : getClassHierarchyFields()) {
			Key columnAnn = field.getAnnotation(Key.class);
			if (columnAnn != null) {
				JSONModelField jsonField = new JSONModelField();
				fillField(field, jsonField);
				jsonField.keyName = getKeyName(columnAnn, field);
				list.add(jsonField);
			}
		}
		return list.toArray(new JSONModelField[list.size()]);
	}

	private String getKeyName(Key ann, Field field) {
		String name = ann.name();
		if (isEmpty(name)) {
			name = field.getName();
		}
		return name;
	}

}
