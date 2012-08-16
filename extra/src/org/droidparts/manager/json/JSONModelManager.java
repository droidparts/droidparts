/**
 * Copyright 2012 Alex Yanchenko
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
package org.droidparts.manager.json;

import org.droidparts.http.HTTPException;
import org.droidparts.http.RESTClient;
import org.droidparts.model.Model;
import org.droidparts.serializer.json.JSONSerializer;

public abstract class JSONModelManager<T extends Model, IdClass> {

	protected final RESTClient client;
	protected final JSONSerializer<T> serializer;

	public JSONModelManager(RESTClient client, JSONSerializer<T> serializer) {
		this.client = client;
		this.serializer = serializer;
	}

	public IdClass create(T item) throws HTTPException {
		return null;
	}

	public T read(IdClass id) throws HTTPException {
		return null;
	}

	public boolean update(T item) throws HTTPException {
		return false;
	}

	public boolean delete(IdClass id) throws HTTPException {
		return false;
	}

}
