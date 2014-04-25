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
package org.droidparts.gram.contract;

public interface DB extends org.droidparts.contract.DB {

	int VERSION = 1;
	String FILE = "dpg.sqlite";

	public interface Table extends org.droidparts.contract.DB.Table {

		String IMAGES = "images";
		String FILTERS = "filters";

	}

	public interface Column extends org.droidparts.contract.DB.Column {

		String NAME = "name";

		String REMOTE_ID = "remote_id";
		String CAPTION = "caption";
		String THUMBNAIL_URL = "thumbnail_url";
		String IMAGE_URL = "image_url";
		String TAGS = "tags";
		String FILTER = "filter";

	}

}
