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
package org.droidparts.inner.reader;

import org.droidparts.inner.ann.FieldSpec;
import org.droidparts.inner.ann.inject.InjectAnn;
import org.droidparts.inner.ann.inject.InjectBundleExtraAnn;
import org.droidparts.inner.ann.inject.InjectDependencyAnn;
import org.droidparts.inner.ann.inject.InjectFragmentAnn;
import org.droidparts.inner.ann.inject.InjectParentActivityAnn;
import org.droidparts.inner.ann.inject.InjectResourceAnn;
import org.droidparts.inner.ann.inject.InjectSystemServiceAnn;
import org.droidparts.inner.ann.inject.InjectViewAnn;

import android.content.Context;
import android.view.View;

public class ValueReader {

	public static Object getVal(Context ctx, View root, Object target,
			FieldSpec<InjectAnn<?>> spec) throws Exception {
		Class<?> annType = spec.ann.getClass();
		Class<?> fieldType = spec.field.getType();
		String fieldName = spec.field.getName();

		Object val = null;
		if (annType == InjectDependencyAnn.class) {
			val = DependencyReader.readVal(ctx, fieldType);
		} else if (annType == InjectBundleExtraAnn.class) {
			InjectBundleExtraAnn ann2 = (InjectBundleExtraAnn) spec.ann;
			val = BundleExtraReader.readVal(target, ann2.key, ann2.optional);
		} else if (annType == InjectResourceAnn.class) {
			InjectResourceAnn ann2 = (InjectResourceAnn) spec.ann;
			val = ResourceReader.readVal(ctx, ann2.id, fieldType);
		} else if (annType == InjectSystemServiceAnn.class) {
			InjectSystemServiceAnn ann2 = (InjectSystemServiceAnn) spec.ann;
			val = SystemServiceReader.readVal(ctx, ann2.name, fieldType);
		} else if (annType == InjectViewAnn.class) {
			InjectViewAnn ann2 = (InjectViewAnn) spec.ann;
			val = ViewAndPreferenceReader.readVal(ctx, root, ann2.id,
					ann2.click, target, fieldType, fieldName);
		} else if (annType == InjectFragmentAnn.class) {
			InjectFragmentAnn ann2 = (InjectFragmentAnn) spec.ann;
			if (LegacyReader.isSupportAvaliable()
					&& LegacyReader.isSupportObject(target)) {
				val = LegacyReader.getFragment(target, ann2.id, fieldName);
			} else {
				val = FragmentsReader.getFragment(target, ann2.id,
						fieldName);
			}
		} else if (annType == InjectParentActivityAnn.class) {
			if (LegacyReader.isSupportAvaliable()
					&& LegacyReader.isSupportObject(target)) {
				val = LegacyReader.getParentActivity(target);
			} else {
				val = FragmentsReader.getParentActivity(target);
			}
		}
		return val;
	}

}
