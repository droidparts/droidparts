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

import org.droidparts.inject.injector.InjectorDelegate;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;

/**
 * <meta-data android:name="droidparts_dependency_provider"
 * android:value="com.yanchenko.android.sample.DependencyProvider" />
 */
public class Injector {

	private static Context ctx;
	private final InjectorDelegate delegate;

	static class Holder {
		static final Injector INJECTOR = new Injector(new InjectorDelegate());
	}

	public static Injector get() {
		return Holder.INJECTOR;
	}

	public static Context getApplicationContext() {
		return ctx;
	}

	public void setUp(Context ctx) {
		init(ctx);
		InjectorDelegate.setUp(Injector.ctx);
	}

	public void tearDown() {
		InjectorDelegate.tearDown();
		ctx = null;
	}

	public void inject(Activity act) {
		init(act);
		// XXX
		View root = act.findViewById(android.R.id.content).getRootView();
		delegate.inject(act, root, act);
	}

	public void inject(Service serv) {
		init(serv);
		delegate.inject(serv, null, serv);
	}

	public void inject(Context ctx, Object target) {
		init(ctx);
		delegate.inject(ctx, null, target);
	}

	public void inject(View view, Object target) {
		Context ctx = view.getContext();
		init(ctx);
		delegate.inject(ctx, view, target);
	}

	public void inject(Object target) {
		if (ctx != null) {
			delegate.inject(ctx, null, target);
		} else {
			throw new IllegalStateException("No context provided.");
		}
	}

	protected Injector(InjectorDelegate delegate) {
		this.delegate = delegate;
	}

	private void init(Context ctx) {
		if (Injector.ctx == null) {
			Injector.ctx = ctx.getApplicationContext();
		}
	}

}
