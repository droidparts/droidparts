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
package org.droidparts.loader;

import org.droidparts.task.AsyncTask;
import org.droidparts.util.L;

public class AsyncTaskLoaderAdapter<Result> extends AsyncTaskLoader<Result> {

	private final AsyncTask<?, ?, Result> task;

	public AsyncTaskLoaderAdapter(AsyncTask<?, ?, Result> task) {
		super(task.getContext());
		this.task = task;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Result loadInBackground() {
		try {
			return task.executeInBackground();
		} catch (Exception e) {
			L.e(e);
			return null;
		}
	}

}
