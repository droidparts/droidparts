/**
 * Copyright 2014 Alex Yanchenko
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
import org.droidparts.inner.ann.bus.ReceiveEventsAnn;
import org.droidparts.inner.ann.inject.InjectAnn;
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

	static TableAnn getTableAnn(Class<?> c) {
		for (Annotation a : c.getDeclaredAnnotations()) {
			Class<?> at = a.annotationType();
			if (Table.class == at) {
				return new TableAnn((Table) a);
			}
		}
		return null;
	}

	static InjectAnn<?> getInjectAnn(Field f) {
		for (Annotation a : f.getDeclaredAnnotations()) {
			Class<?> at = a.annotationType();
			if (InjectView.class == at) {
				return new InjectViewAnn((InjectView) a);
			} else if (InjectFragment.class == at) {
				return new InjectFragmentAnn((InjectFragment) a);
			} else if (InjectDependency.class == at) {
				return new InjectDependencyAnn((InjectDependency) a);
			} else if (InjectBundleExtra.class == at) {
				return new InjectBundleExtraAnn((InjectBundleExtra) a);
			} else if (InjectParentActivity.class == at) {
				return new InjectParentActivityAnn((InjectParentActivity) a);
			} else if (InjectResource.class == at) {
				return new InjectResourceAnn((InjectResource) a);
			} else if (InjectSystemService.class == at) {
				return new InjectSystemServiceAnn((InjectSystemService) a);
			}
		}
		return null;
	}

	static KeyAnn getKeyAnn(Field f) {
		for (Annotation a : f.getDeclaredAnnotations()) {
			Class<?> at = a.annotationType();
			if (Key.class == at) {
				return new KeyAnn((Key) a);
			}
		}
		return null;
	}

	static ColumnAnn getColumnAnn(Field f) {
		for (Annotation a : f.getDeclaredAnnotations()) {
			Class<?> at = a.annotationType();
			if (Column.class == at) {
				return new ColumnAnn((Column) a);
			}
		}
		return null;
	}

	static ReceiveEventsAnn getReceiveEventsAnn(Method m) {
		for (Annotation a : m.getDeclaredAnnotations()) {
			Class<?> at = a.annotationType();
			if (ReceiveEvents.class == at) {
				return new ReceiveEventsAnn((ReceiveEvents) a);
			}
		}
		return null;
	}
}