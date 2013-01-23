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
package org.droidparts.widget;

import static org.droidparts.util.Strings.isNotEmpty;

import org.droidparts.adapter.ui.TextWatcherAdapter;
import org.droidparts.adapter.ui.TextWatcherAdapter.TextWatcherListener;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;

public class ClearableEditText extends EditText implements OnTouchListener,
		TextWatcherListener {

	public interface Listener {
		void didClearEditText();
	}

	public void setClearDrawable(int drawableResId) {
		xD = getResources().getDrawable(drawableResId);
		xD.setBounds(0, 0, xD.getIntrinsicWidth(), xD.getIntrinsicHeight());
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	private Drawable xD;
	private Listener listener;

	public ClearableEditText(Context context) {
		super(context);
		init();
	}

	public ClearableEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setClearDrawable(android.R.drawable.presence_offline);
		setOnTouchListener(this);
		addTextChangedListener(new TextWatcherAdapter(this, this));
		onTextChanged(this, getText().toString());
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (getCompoundDrawables()[2] != null) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				boolean tappedX = event.getX() > (getWidth()
						- getPaddingRight() - xD.getIntrinsicWidth());
				if (tappedX) {
					setText("");
					if (listener != null) {
						listener.didClearEditText();
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onTextChanged(EditText view, String text) {
		Drawable x = isNotEmpty(getText()) ? xD : null;
		setCompoundDrawables(getCompoundDrawables()[0],
				getCompoundDrawables()[1], x, getCompoundDrawables()[3]);
	}

}
