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
package org.droidparts.inject.injector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;

import org.droidparts.annotation.inject.InjectFragment;
import org.droidparts.annotation.inject.InjectParentActivity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

public class FragmentsInjectorDelegate extends InjectorDelegate {

	@Override
	protected boolean inject(Context ctx, View root, Object target,
			Annotation ann, Field field) {
		boolean success = super.inject(ctx, root, target, ann, field);
		if (!success) {
			Class<? extends Annotation> annType = ann.annotationType();
			if (annType == InjectFragment.class) {
				success = FragmentInjector.inject((FragmentActivity) target,
						(InjectFragment) ann, field);
			} else if (annType == InjectParentActivity.class) {
				success = ParentActivityInjector.inject((Fragment) target,
						field);
			}
		}
		return success;
	}

	@Override
	protected Bundle getIntentExtras(Object obj) {
		Bundle data = super.getIntentExtras(obj);
		if (obj instanceof Fragment) {
			data = ((Fragment) obj).getArguments();
		}
		return data;
	}

	@Override
	protected HashSet<Class<? extends Annotation>> getSupportedAnnotations() {
		HashSet<Class<? extends Annotation>> set = super
				.getSupportedAnnotations();
		set.add(InjectFragment.class);
		set.add(InjectParentActivity.class);
		return set;
	}

}
