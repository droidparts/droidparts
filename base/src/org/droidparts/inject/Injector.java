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
package org.droidparts.inject;

import org.droidparts.inject.injector.DependencyProvider;
import org.droidparts.inject.injector.InjectorDelegate;
import org.droidparts.util.L;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.view.View;

/**
 * <meta-data android:name="droidparts_dependency_provider"
 * android:value="com.yanchenko.android.sample.DependencyProvider" />
 */
public class Injector {

	public static Context getApplicationContext() {
		return appCtx;
	}

	public static Injector get() {
		return Holder.INJECTOR;
	}

	public void setUp(Context ctx) {
		setContext(ctx);
		InjectorDelegate.setUp(appCtx);
	}

	public void tearDown() {
		InjectorDelegate.tearDown();
		appCtx = null;
	}

	public <T> T getDependency(Context ctx, Class<T> cls)
			throws RuntimeException {
		setContext(ctx);
		return DependencyProvider.getDependency(ctx, cls);
	}

	public void inject(Activity act) {
		setContext(act);
		View root = act.findViewById(android.R.id.content).getRootView();
		delegate.inject(act, root, act);
	}

	public void inject(Service serv) {
		setContext(serv);
		delegate.inject(serv, null, serv);
	}

	public void inject(Context ctx, Object target) {
		setContext(ctx);
		delegate.inject(ctx, null, target);
	}

	public void inject(Dialog dialog, Object target) {
		View root = dialog.findViewById(android.R.id.content).getRootView();
		inject(root, target);
	}

	public void inject(View view, Object target) {
		Context ctx = view.getContext();
		setContext(ctx);
		delegate.inject(ctx, view, target);
	}

	//

	private static volatile Context appCtx;
	private final InjectorDelegate delegate;

	static class Holder {
		static final Injector INJECTOR = new Injector();
	}

	private Injector() {
		InjectorDelegate fragmentsDelegate = null;
		try {
			fragmentsDelegate = (InjectorDelegate) Class.forName(
					"org.droidparts.inject.injector.FragmentsInjectorDelegate")
					.newInstance();
		} catch (Exception e) {
			L.v(e);
		}
		delegate = (fragmentsDelegate != null) ? fragmentsDelegate
				: new InjectorDelegate();
	}

	private static void setContext(Context ctx) {
		if (appCtx == null) {
			appCtx = ctx.getApplicationContext();
		}
	}

}
