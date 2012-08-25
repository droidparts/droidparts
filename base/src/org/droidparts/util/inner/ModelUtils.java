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
package org.droidparts.util.inner;

import java.lang.reflect.Field;
import java.util.List;

import org.droidparts.model.Model;
import org.droidparts.reflect.util.ReflectionUtils;
import org.droidparts.util.L;

public class ModelUtils {

	public static boolean equals(Model model, Object other) {
		if (other == null) {
			return false;
		} else if (model == other) {
			return true;
		} else if (other.getClass() == model.getClass()) {
			List<Field> fields = ReflectionUtils.listAnnotatedFields(model
					.getClass());
			for (Field f : fields) {
				try {
					Object thisF = f.get(model);
					Object otherF = f.get(other);
					if (thisF == null && otherF != null) {
						return false;
					} else if (!thisF.equals(otherF)) {
						return false;
					}
				} catch (Exception e) {
					L.d(e);
				}
			}
			return true;
		}
		return false;
	}

	public static int hashCode(Model model) {
		return toString(model).hashCode();
	}

	public static String toString(Model model) {
		Class<?> cls = model.getClass();
		StringBuilder sb = new StringBuilder();
		sb.append(cls.getSimpleName());
		sb.append(" [");
		List<Field> fields = ReflectionUtils.listAnnotatedFields(cls);
		for (int i = 0; i < fields.size(); i++) {
			Field field = fields.get(i);
			sb.append(field.getName());
			sb.append(": ");
			try {
				sb.append(String.valueOf(field.get(model)));
			} catch (Exception e) {
				L.d(e);
				sb.append("n/a");
			}
			if (i < fields.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	protected ModelUtils() {
	}

}
