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
package org.droidparts.task.listener;

import org.droidparts.Injector;

import android.content.Context;

public abstract class SimpleAsyncTaskProgressListener implements
		AsyncTaskProgressListener {

	protected final Context ctx;

	public SimpleAsyncTaskProgressListener(Context ctx) {
		Injector.inject(ctx, this);
		this.ctx = ctx;
	}

	public void setTitle(int titleResId) {
		setTitle(ctx.getString(titleResId));
	}

	public void setMessage(int messageResId) {
		setTitle(ctx.getString(messageResId));
	}

}
