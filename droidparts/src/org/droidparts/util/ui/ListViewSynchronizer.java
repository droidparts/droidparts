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
package org.droidparts.util.ui;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;

// https://github.com/vladexologija/PinterestListView/blob/master/src/com/vladimir/pinterestlistview/ItemsActivity.java
public class ListViewSynchronizer implements OnTouchListener, OnScrollListener {

	private final ListView leftListView;
	private final ListView rightListView;

	private int[] leftViewsHeights;
	private int[] rightViewsHeights;

	public ListViewSynchronizer(ListView first, ListView second) {
		leftListView = first;
		rightListView = second;
	}

	public void synchronize() {
		ListAdapter leftAdapter = leftListView.getAdapter();
		ListAdapter rightAdapter = rightListView.getAdapter();
		if (leftAdapter == null || rightAdapter == null) {
			throw new IllegalStateException("List adapters must be set.");
		}
		leftViewsHeights = new int[leftAdapter.getCount()];
		rightViewsHeights = new int[rightAdapter.getCount()];
		// reset getFirstVisiblePosition()
		leftListView.setAdapter(leftAdapter);
		rightListView.setAdapter(rightAdapter);
		//
		leftListView.setOnTouchListener(this);
		rightListView.setOnTouchListener(this);
		leftListView.setOnScrollListener(this);
		rightListView.setOnScrollListener(this);
	}

	public void desynchronize() {
		leftListView.setOnTouchListener(null);
		rightListView.setOnTouchListener(null);
		leftListView.setOnScrollListener(null);
		rightListView.setOnScrollListener(null);
	}

	/**
	 * Passing the touch event to the opposite list
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v == leftListView && !dispatched) {
			dispatched = true;
			rightListView.dispatchTouchEvent(event);
		} else if (v == rightListView && !dispatched) {
			dispatched = true;
			leftListView.dispatchTouchEvent(event);
		}
		dispatched = false;
		return false;
	}

	private boolean dispatched = false;

	/**
	 * Synchronizing scrolling Distance from the top of the first visible
	 * element opposite list: sum_heights(opposite invisible screens) -
	 * sum_heights(invisible screens) + distance from top of the first visible
	 * child
	 */
	@Override
	public void onScroll(AbsListView listView, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (listView.getChildAt(0) != null) {
			if (listView == leftListView) {
				leftViewsHeights[listView.getFirstVisiblePosition()] = listView
						.getChildAt(0).getHeight();

				int h = 0;
				for (int i = 0; i < rightListView.getFirstVisiblePosition(); i++) {
					h += rightViewsHeights[i];
				}

				int hi = 0;
				for (int i = 0; i < leftListView.getFirstVisiblePosition(); i++) {
					hi += leftViewsHeights[i];
				}

				int top = h - hi + listView.getChildAt(0).getTop();
				rightListView.setSelectionFromTop(
						rightListView.getFirstVisiblePosition(), top);
			} else if (listView == rightListView) {
				rightViewsHeights[listView.getFirstVisiblePosition()] = listView
						.getChildAt(0).getHeight();

				int h = 0;
				for (int i = 0; i < leftListView.getFirstVisiblePosition(); i++) {
					h += leftViewsHeights[i];
				}

				int hi = 0;
				for (int i = 0; i < rightListView.getFirstVisiblePosition(); i++) {
					hi += rightViewsHeights[i];
				}

				int top = h - hi + listView.getChildAt(0).getTop();
				leftListView.setSelectionFromTop(
						leftListView.getFirstVisiblePosition(), top);
			}

		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// pass
	}
}
