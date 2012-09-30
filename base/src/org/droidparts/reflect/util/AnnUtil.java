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
package org.droidparts.reflect.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.droidparts.annotation.inject.InjectBundleExtra;
import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.annotation.inject.InjectFragment;
import org.droidparts.annotation.inject.InjectParentActivity;
import org.droidparts.annotation.inject.InjectResource;
import org.droidparts.annotation.inject.InjectSystemService;
import org.droidparts.annotation.inject.InjectView;
import org.droidparts.annotation.json.Key;
import org.droidparts.annotation.json.Object;
import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.reflect.model.Ann;
import org.droidparts.reflect.model.inject.ann.InjectBundleExtraAnn;
import org.droidparts.reflect.model.inject.ann.InjectDependencyAnn;
import org.droidparts.reflect.model.inject.ann.InjectFragmentAnn;
import org.droidparts.reflect.model.inject.ann.InjectParentActivityAnn;
import org.droidparts.reflect.model.inject.ann.InjectResourceAnn;
import org.droidparts.reflect.model.inject.ann.InjectSystemServiceAnn;
import org.droidparts.reflect.model.inject.ann.InjectViewAnn;
import org.droidparts.reflect.model.json.ann.KeyAnn;
import org.droidparts.reflect.model.json.ann.ObjectAnn;
import org.droidparts.reflect.model.sql.ann.ColumnAnn;
import org.droidparts.reflect.model.sql.ann.TableAnn;

public final class AnnUtil {

	public static <T extends Annotation> Ann<T> getClassAnn(
			Class<? extends Ann<T>> annCls, Class<?> cls) {
		for (Ann<?> ann : getClassAnns(cls)) {
			if (ann.getClass() == annCls) {
				@SuppressWarnings("unchecked")
				Ann<T> typedAnn = (Ann<T>) ann;
				return typedAnn;
			}
		}
		return null;
	}

	public static <T extends Annotation> Ann<T> getFieldAnn(
			Class<? extends Ann<T>> annCls, Class<?> cls, Field f) {
		for (Ann<?> ann : getFieldAnns(cls, f)) {
			if (ann.getClass().isAssignableFrom(annCls)) {
				@SuppressWarnings("unchecked")
				Ann<T> typedAnn = (Ann<T>) ann;
				return typedAnn;
			}
		}
		return null;
	}

	public static <T extends Annotation> Ann<T>[] getClassAnns(Class<?> cls) {
		return getAnns(cls.getAnnotations());
	}

	public static <T extends Annotation> Ann<T>[] getFieldAnns(Class<?> cls,
			Field f) {
		return getAnns(f.getAnnotations());
	}

	public static <T extends Annotation> Ann<T>[] getAnns(
			Annotation[] annotations) {
		ArrayList<Ann<?>> anns = new ArrayList<Ann<?>>();
		for (Annotation annotation : annotations) {
			Class<?> annotationType = annotation.annotationType();
			// class
			if (Table.class == annotationType) {
				anns.add(new TableAnn((Table) annotation));
			} else if (Object.class == annotationType) {
				anns.add(new ObjectAnn((Object) annotation));
			}
			// field
			else if (Column.class == annotationType) {
				anns.add(new ColumnAnn((Column) annotation));
			} else if (Key.class == annotationType) {
				anns.add(new KeyAnn((Key) annotation));
			}
			// inject
			else if (InjectBundleExtra.class == annotationType) {
				anns.add(new InjectBundleExtraAnn(
						(InjectBundleExtra) annotation));
			} else if (InjectDependency.class == annotationType) {
				anns.add(new InjectDependencyAnn((InjectDependency) annotation));
			} else if (InjectResource.class == annotationType) {
				anns.add(new InjectResourceAnn((InjectResource) annotation));
			} else if (InjectSystemService.class == annotationType) {
				anns.add(new InjectSystemServiceAnn(
						(InjectSystemService) annotation));
			} else if (InjectView.class == annotationType) {
				anns.add(new InjectViewAnn((InjectView) annotation));
			}
			// inject modern
			else if (InjectFragment.class == annotationType) {
				anns.add(new InjectFragmentAnn((InjectFragment) annotation));
			} else if (InjectParentActivity.class == annotationType) {
				anns.add(new InjectParentActivityAnn(
						(InjectParentActivity) annotation));
			}
		}
		@SuppressWarnings("unchecked")
		Ann<T>[] arr = anns.toArray(new Ann[anns.size()]);
		return arr;
	}

	public static List<Field> listAnnotatedFields(Class<?> cls) {
		ArrayList<Class<?>> clsTree = new ArrayList<Class<?>>();
		boolean enteredDroidParts = false;
		do {
			clsTree.add(0, cls);
			boolean inDroidParts = cls.getCanonicalName().startsWith(
					"org.droidparts");
			if (enteredDroidParts && !inDroidParts) {
				break;
			} else {
				enteredDroidParts = inDroidParts;
				cls = cls.getSuperclass();
			}
		} while (cls != null);
		ArrayList<Field> fields = new ArrayList<Field>();
		for (Class<?> c : clsTree) {
			for (Field f : c.getDeclaredFields()) {
				if (f.getAnnotations().length > 0) {
					fields.add(f);
				}
			}
		}
		return fields;
	}
}
