/**
 * Copyright 2011 Alex Yanchenko
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
package org.droidparts.task;

import org.droidparts.inject.Injector;
import org.droidparts.model.Tuple;

import android.content.Context;

public abstract class AsyncTask<Params, Progress, Result> extends
		android.os.AsyncTask<Params, Progress, Tuple<Exception, Result>> {

	protected final Context ctx;
	protected final AsyncTaskListener<Result> listener;

	public AsyncTask(Context ctx) {
		this(ctx, null);
	}

	public AsyncTask(Context ctx, AsyncTaskListener<Result> listener) {
		this.ctx = ctx;
		this.listener = listener;
		Injector.inject(ctx, this);
	}

	@Override
	protected final Tuple<Exception, Result> doInBackground(Params... params) {
		Result res = null;
		Exception ex = null;
		try {
			res = executeInBackground(params);
		} catch (Exception e) {
			ex = e;
		}
		return new Tuple<Exception, Result>(ex, res);
	}

	@Override
	protected final void onPostExecute(Tuple<Exception, Result> result) {
		if (result.k != null) {
			onFailurePostExecute(result.k);
		} else {
			onSuccessPostExecute(result.v);
		}
	}

	protected abstract Result executeInBackground(Params... params)
			throws Exception;

	protected void onSuccessPostExecute(Result result) {
		if (listener != null) {
			listener.onAsyncTaskSuccess(result);
		}
	}

	protected void onFailurePostExecute(Exception exception) {
		if (listener != null) {
			listener.onAsyncTaskFailure(exception);
		}
	}

}
