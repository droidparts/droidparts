/**
 * Copyright 2014 Alex Yanchenko
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
package org.droidparts.adapter.widget;

import java.util.List;

import android.widget.Spinner;

public class SpinnerAdapter<T> extends ArrayAdapter<T> {

	protected final Spinner spinner;

	public SpinnerAdapter(Spinner spinner, List<T> list) {
		this(spinner, android.R.layout.simple_spinner_item, list);
	}

	public SpinnerAdapter(Spinner spinner, int rowResId, List<T> list) {
		super(spinner.getContext(), rowResId, list);
		setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.spinner = spinner;
	}

	public void setSelectedItem(T item) {
		for (int i = 0; i < getCount(); i++) {
			T it = getItem(i);
			if (it.equals(item)) {
				spinner.setSelection(i);
				break;
			}
		}
	}

	public T getSelectedItem() {
		@SuppressWarnings("unchecked")
		T selection = (T) spinner.getSelectedItem();
		return selection;
	}

}
