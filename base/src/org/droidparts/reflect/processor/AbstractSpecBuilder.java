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

import static org.droidparts.reflect.util.ReflectionUtils.getArrayType;
import static org.droidparts.reflect.util.ReflectionUtils.getFieldGenericArgs;
import static org.droidparts.reflect.util.TypeHelper.isArray;
import static org.droidparts.reflect.util.TypeHelper.isCollection;

import java.lang.reflect.Field;
import java.util.List;

import org.droidparts.model.Model;
import org.droidparts.reflect.model.FieldSpec;
import org.droidparts.reflect.util.AnnUtil;

public abstract class AbstractAnnotationProcessor<FieldSpecType extends FieldSpec> {

	protected final Class<? extends Model> cls;

	private String modelClassName;
	private FieldSpecType[] modelClassFields;

	public AbstractAnnotationProcessor(Class<? extends Model> cls) {
		this.cls = cls;
	}

	public final String getModelClassName() {
		if (modelClassName == null) {
			modelClassName = modelClassName();
		}
		return modelClassName;
	}

	public final FieldSpecType[] getModelClassFields() {
		if (modelClassFields == null) {
			modelClassFields = modelClassFields();
		}
		return modelClassFields;
	}

	protected abstract String modelClassName();

	protected abstract FieldSpecType[] modelClassFields();

	protected final List<Field> getClassHierarchyFields() {
		return AnnUtil.listAnnotatedFields(cls);
	}

	protected final Class<?> getMultiFieldArgType(Field field) {
		Class<?> cls = null;
		Class<?> fieldType = field.getType();
		if (isArray(fieldType)) {
			cls = getArrayType(fieldType);
		} else if (isCollection(fieldType)) {
			Class<?>[] genericArgs = getFieldGenericArgs(field);
			cls = (genericArgs.length > 0) ? genericArgs[0] : Object.class;
		}
		return cls;
	}

}
