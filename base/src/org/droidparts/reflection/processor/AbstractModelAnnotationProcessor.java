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

import static org.droidparts.reflection.util.TypeHelper.getFieldGenericArgs;

import java.lang.reflect.Field;
import java.util.List;

import org.droidparts.model.Model;
import org.droidparts.reflection.model.AbstractModelField;
import org.droidparts.reflection.util.ReflectionUtils;

public abstract class AbstractModelAnnotationProcessor<ModelFieldType> {

	protected final Class<? extends Model> cls;

	private String modelClassName;
	private ModelFieldType[] modelClassFields;

	public AbstractModelAnnotationProcessor(Class<? extends Model> cls) {
		this.cls = cls;
	}

	public final String getModelClassName() {
		if (modelClassName == null) {
			modelClassName = modelClassName();
		}
		return modelClassName;
	}

	public final ModelFieldType[] getModelClassFields() {
		if (modelClassFields == null) {
			modelClassFields = modelClassFields();
		}
		return modelClassFields;
	}

	protected abstract String modelClassName();

	protected abstract ModelFieldType[] modelClassFields();

	protected final List<Field> getClassHierarchyFields() {
		return ReflectionUtils.listAnnotatedFields(cls);
	}

	protected final void fillField(Field source, AbstractModelField target) {
		target.fieldName = source.getName();
		target.fieldClass = source.getType();
		Class<?>[] genericArgs = getFieldGenericArgs(source);
		if (genericArgs.length > 0) {
			target.fieldGenericArg = genericArgs[0];
		}
	}
}
