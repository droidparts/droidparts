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

import static org.droidparts.util.Strings.isEmpty;

import org.droidparts.adapter.ui.TextWatcherAdapter;
import org.droidparts.adapter.ui.TextWatcherAdapter.TextWatcherListener;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

public class ClearableEditText extends EditText implements OnTouchListener,
		OnFocusChangeListener, TextWatcherListener {

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
		super.setOnTouchListener(this);
		super.setOnFocusChangeListener(this);
		addTextChangedListener(new TextWatcherAdapter(this, this));
		onTextChanged(this, getText().toString());
	}

	@Override
	public void setOnTouchListener(OnTouchListener l) {
		this.l = l;
	}

	@Override
	public void setOnFocusChangeListener(OnFocusChangeListener f) {
		this.f = f;
	}

	private OnTouchListener l;
	private OnFocusChangeListener f;
	
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
		if (l != null) {
			return l.onTouch(v, event);
		}
		return false;
	}

	@Override
	public void onTextChanged(EditText view, String text) {
		toggleClearDrawable(text);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(hasFocus) {
			toggleClearDrawable(getText().toString());
		}
		if (f != null) {
			f.onFocusChange(v, hasFocus);
		}
	}

	private void toggleClearDrawable(String text) {
		Drawable x = isEmpty(text) ? null : xD;
		setCompoundDrawables(getCompoundDrawables()[0],
				getCompoundDrawables()[1], x, getCompoundDrawables()[3]);
	}
}
