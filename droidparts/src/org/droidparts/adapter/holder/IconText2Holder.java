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
package org.droidparts.adapter.holder;

import org.droidparts.annotation.inject.InjectView;

import android.view.View;
import android.widget.ImageView;

public class IconText2Holder extends Text2Holder {

	@InjectView(id = android.R.id.icon)
	public ImageView icon;

	public IconText2Holder(View view) {
		super(view);
	}

}
