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
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.droidparts.annotation.bus.ReceiveEvents;
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
import org.droidparts.inner.ann.bus.ReceiveEventsAnn;
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

	static <T extends Annotation> Ann<T>[] getClassAnns(Class<?> c) {
		return toAnns(c.getAnnotations());
	}

	static <T extends Annotation> Ann<T>[] getFieldAnns(Field f) {
		return toAnns(f.getAnnotations());
	}

	static <T extends Annotation> Ann<T>[] getMethodAnns(Method m) {
		return toAnns(m.getAnnotations());
	}

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
		Class<?> type = annotation.annotationType();
		if (InjectBundleExtra.class == type) {
			return new InjectBundleExtraAnn((InjectBundleExtra) annotation);
		} else if (InjectDependency.class == type) {
			return new InjectDependencyAnn((InjectDependency) annotation);
		} else if (InjectResource.class == type) {
			return new InjectResourceAnn((InjectResource) annotation);
		} else if (InjectSystemService.class == type) {
			return new InjectSystemServiceAnn((InjectSystemService) annotation);
		} else if (InjectView.class == type) {
			return new InjectViewAnn((InjectView) annotation);
		} else if (InjectFragment.class == type) {
			return new InjectFragmentAnn((InjectFragment) annotation);
		} else if (InjectParentActivity.class == type) {
			return new InjectParentActivityAnn(
					(InjectParentActivity) annotation);
		} else if (Table.class == type) {
			return new TableAnn((Table) annotation);
		} else if (Column.class == type) {
			return new ColumnAnn((Column) annotation);
		} else if (Key.class == type) {
			return new KeyAnn((Key) annotation);
		} else if (ReceiveEvents.class == type) {
			return new ReceiveEventsAnn((ReceiveEvents) annotation);
		} else {
			return null;
		}
	}

}