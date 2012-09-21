package org.droidparts.task.listener;

import org.droidparts.inject.Injector;

import android.content.Context;

public abstract class SimpleAsyncTaskProgressListener implements
		AsyncTaskProgressListener {

	protected final Context ctx;

	public SimpleAsyncTaskProgressListener(Context ctx) {
		Injector.get().inject(ctx, this);
		this.ctx = ctx;
	}

	public void setTitle(int titleResId) {
		setTitle(ctx.getString(titleResId));
	}

	public void setMessage(int messageResId) {
		setTitle(ctx.getString(messageResId));
	}

}
