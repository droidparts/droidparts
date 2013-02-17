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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

// http://stackoverflow.com/questions/2646028
public class VerticalScrollView extends ScrollView {

	private float xDistance, yDistance, lastX, lastY;

	public VerticalScrollView(Context context) {
		super(context);
	}

	public VerticalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VerticalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDistance = yDistance = 0f;
			lastX = ev.getX();
			lastY = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			float curX = ev.getX();
			float curY = ev.getY();
			xDistance += Math.abs(curX - lastX);
			yDistance += Math.abs(curY - lastY);
			lastX = curX;
			lastY = curY;
			if (xDistance > yDistance) {
				return false;
			}
		}
		return super.onInterceptTouchEvent(ev);
	}
}
