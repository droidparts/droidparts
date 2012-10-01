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
package org.droidparts.reflect.model;

import java.lang.reflect.Field;

public abstract class FieldSpec<AnnType extends Ann<?>> {

	public final Field field;
	public final Class<?> multiFieldArgType;

	public final AnnType ann;

	public FieldSpec(Field field, Class<?> multiFieldArgType, AnnType ann) {
		this.field = field;
		this.multiFieldArgType = multiFieldArgType;
		this.ann = ann;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ", fieldName:" + field.getName()
				+ ", fieldType:" + field.getType() + ", multiFieldArgType:"
				+ multiFieldArgType + ", ann:" + ann;
	}

}