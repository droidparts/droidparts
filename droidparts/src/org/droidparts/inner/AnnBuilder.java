/**
 * Copyright 2013 Alex Yanchenko
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
package org.droidparts.inner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.droidparts.annotation.inject.InjectBundleExtra;
import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.annotation.inject.InjectFragment;
import org.droidparts.annotation.inject.InjectParentActivity;
import org.droidparts.annotation.inject.InjectResource;
import org.droidparts.annotation.inject.InjectSystemService;
import org.droidparts.annotation.inject.InjectView;
import org.droidparts.annotation.json.Key;
import org.droidparts.annotation.sql.Column;
import org.droidparts.annotation.sql.Table;
import org.droidparts.inner.ann.Ann;
import org.droidparts.inner.ann.inject.InjectBundleExtraAnn;
import org.droidparts.inner.ann.inject.InjectDependencyAnn;
import org.droidparts.inner.ann.inject.InjectFragmentAnn;
import org.droidparts.inner.ann.inject.InjectParentActivityAnn;
import org.droidparts.inner.ann.inject.InjectResourceAnn;
import org.droidparts.inner.ann.inject.InjectSystemServiceAnn;
import org.droidparts.inner.ann.inject.InjectViewAnn;
import org.droidparts.inner.ann.json.KeyAnn;
import org.droidparts.inner.ann.sql.ColumnAnn;
import org.droidparts.inner.ann.sql.TableAnn;

public final class AnnBuilder {

	@SuppressWarnings("unchecked")
	static <T extends Annotation> Ann<T> getClassAnn(
			Class<? extends Ann<T>> annCls, Class<?> cls) {
		return pickAnn(annCls, (Ann<T>[]) getClassAnns(cls));
	}

	@SuppressWarnings("unchecked")
	static <T extends Annotation> Ann<T> getFieldAnn(
			Class<? extends Ann<T>> annCls, Class<?> cls, Field f) {
		return pickAnn(annCls, (Ann<T>[]) getFieldAnns(cls, f));
	}

	private static <T extends Annotation> Ann<T> pickAnn(
			Class<? extends Ann<T>> annCls, Ann<T>[] anns) {
		for (Ann<?> ann : anns) {
			if (ann.getClass().isAssignableFrom(annCls)) {
				@SuppressWarnings("unchecked")
				Ann<T> typedAnn = (Ann<T>) ann;
				return typedAnn;
			}
		}
		return null;
	}

	static <T extends Annotation> Ann<T>[] getClassAnns(Class<?> cls) {
		return toAnns(cls.getAnnotations());
	}

	static <T extends Annotation> Ann<T>[] getFieldAnns(Class<?> cls, Field f) {
		return toAnns(f.getAnnotations());
	}

	//

	@SuppressWarnings("unchecked")
	private static <T extends Annotation> Ann<T>[] toAnns(
			Annotation[] annotations) {
		ArrayList<Ann<?>> anns = new ArrayList<Ann<?>>();
		for (Annotation annotation : annotations) {
			Ann<?> ann = toAnn(annotation);
			if (ann != null) {
				anns.add(ann);
			}
		}
		return anns.toArray(new Ann[anns.size()]);
	}

	private static Ann<?> toAnn(Annotation annotation) {
		Class<?> annotationType = annotation.annotationType();
		Ann<?> ann = null;
		// class
		if (Table.class == annotationType) {
			ann = new TableAnn((Table) annotation);
		}
		// field
		else if (Column.class == annotationType) {
			ann = new ColumnAnn((Column) annotation);
		} else if (Key.class == annotationType) {
			ann = new KeyAnn((Key) annotation);
		}
		// inject
		else if (InjectBundleExtra.class == annotationType) {
			ann = new InjectBundleExtraAnn((InjectBundleExtra) annotation);
		} else if (InjectDependency.class == annotationType) {
			ann = new InjectDependencyAnn((InjectDependency) annotation);
		} else if (InjectResource.class == annotationType) {
			ann = new InjectResourceAnn((InjectResource) annotation);
		} else if (InjectSystemService.class == annotationType) {
			ann = new InjectSystemServiceAnn((InjectSystemService) annotation);
		} else if (InjectView.class == annotationType) {
			ann = new InjectViewAnn((InjectView) annotation);
		} else if (InjectFragment.class == annotationType) {
			ann = new InjectFragmentAnn((InjectFragment) annotation);
		} else if (InjectParentActivity.class == annotationType) {
			ann = new InjectParentActivityAnn((InjectParentActivity) annotation);
		}
		return ann;
	}

}