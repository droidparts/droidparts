/**
 * Copyright 2017 Alex Yanchenko
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
package org.droidparts.persist.serializer;

import org.droidparts.util.Strings;

public class SerializerException extends Exception {
	private static final long serialVersionUID = 1L;

	public static String createMessage(Object src, String[] parts) {
		return String.format("Missing or invalid %s in '%s'.", Strings.join(parts, " | "), String.valueOf(src));
	}

	private final String[] parts;

	public SerializerException(Object src, String[] parts) {
		super(createMessage(src, parts));
		this.parts = parts;
	}

	public String[] getParts() {
		return parts;
	}

}
