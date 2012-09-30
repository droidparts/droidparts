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

import static org.droidparts.reflect.util.AnnUtil.getFieldAnns;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.droidparts.reflect.model.Ann;
import org.droidparts.reflect.model.inject.InjectSpec;
import org.droidparts.reflect.model.inject.ann.InjectAnn;
import org.droidparts.reflect.util.AnnUtil;

public class InjectAnnotationProcessor {

	protected final Class<?> cls;

	public InjectAnnotationProcessor(Class<?> cls) {
		this.cls = cls;
	}

	public InjectSpec[] getSpecs() {
		ArrayList<InjectSpec> list = new ArrayList<InjectSpec>();
		List<Field> fields = AnnUtil.listAnnotatedFields(cls);
		for (Field field : fields) {
			for (Ann<?> ann : getFieldAnns(cls, field)) {
				if (ann instanceof InjectAnn) {
					InjectSpec spec = new InjectSpec();
					spec.field = field;
					spec.injectAnn = (InjectAnn<?>) ann;
					list.add(spec);
					break;
				}
			}
		}
		return list.toArray(new InjectSpec[list.size()]);
	}

}
