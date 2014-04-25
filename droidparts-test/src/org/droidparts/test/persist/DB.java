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
package org.droidparts.test.persist;

public interface DB extends org.droidparts.contract.DB {

	public interface Table extends org.droidparts.contract.DB.Table {

		String ALBUMS = "_albums_";
		String TRACKS = "_tracks_";
		String TAGS = "_tags_";

		String ALBUM_TO_TAG = "_album_to_tag_";

	}

	public interface Column extends org.droidparts.contract.DB.Column {

		String YEAR = "_year_";
		String NAME = "_name_";
		String COMMENT = "_comment_";

		String ALBUM_ID = "_album_";
		String TAG_ID = "_tag_";

	}

}
