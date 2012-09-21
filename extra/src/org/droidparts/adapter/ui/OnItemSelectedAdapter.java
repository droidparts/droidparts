package org.droidparts.adapter.ui;

import android.view.View;
import android.widget.AdapterView;

public class OnItemSelectedAdapter implements
		android.widget.AdapterView.OnItemSelectedListener {

	public interface OnItemSelectedListener {

		void onItemSelected(AdapterView<?> view, int position);

	}

	private final AdapterView<?> view;
	private final OnItemSelectedListener listener;

	public OnItemSelectedAdapter(AdapterView<?> view,
			OnItemSelectedListener listener) {
		this.view = view;
		this.listener = listener;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		listener.onItemSelected(this.view, position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// pass
	}

}
