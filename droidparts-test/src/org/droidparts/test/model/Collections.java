/**
 * Copyright 2015 Alex Yanchenko
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
package org.droidparts.test.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.droidparts.annotation.serialize.JSON;
import org.droidparts.annotation.serialize.XML;
import org.droidparts.model.Model;

public class Collections extends Model {
	private static final long serialVersionUID = 1L;

	@JSON(key = "albums")
	@XML(tag = "albums")
	public ArrayList<Album> albumsColl;

	@JSON(key = "albums")
	@XML(tag = "albums")
	public Album[] albumsArr;

	@JSON(key = "map")
	public HashMap<String, String> map;

}
