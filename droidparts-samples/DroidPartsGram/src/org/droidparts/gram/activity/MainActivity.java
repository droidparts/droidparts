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
package org.droidparts.gram.activity;

import java.util.ArrayList;

import org.droidparts.annotation.bus.ReceiveEvents;
import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.gram.R;
import org.droidparts.gram.misc.DialogFactory;
import org.droidparts.gram.model.Image;
import org.droidparts.util.L;

public class MainActivity extends PopularImageListActivity {

	@InjectDependency
	private DialogFactory dialogFactory;

	@ReceiveEvents
	private void onEvent(String name, ArrayList<Image> data) {
		dialogFactory.showToast(getString(R.string.event_format, name));
		L.d(data);
	}

}
