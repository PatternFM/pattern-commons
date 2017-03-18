/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fm.pattern.commons.util;

import java.lang.reflect.Field;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes" })
public final class Reflection {

	private static final Logger log = LoggerFactory.getLogger(Reflection.class);

	private Reflection() {

	}

	public static Object getValue(Object instance, String property) {
		try {
			return PropertyUtils.getProperty(instance, property);
		}
		catch (Exception e) {
			log.warn("Failed to get property '" + property + "':", e);
			return null;
		}
	}

	public static void setValue(Object instance, String property, Object value) {
		setValue(instance, property, value, 0);
	}

	public static void setValue(Object instance, String attribute, Object value, Integer ancestor) {
		try {
			String[] fields = attribute.contains(".") ? attribute.split("\\.") : new String[] { attribute };

			Field field = getTargetField(instance.getClass(), fields, 0, ancestor);
			if (field == null) {
				return;
			}

			Object object = attribute.contains(".") ? getTargetObject(instance, fields, 0, ancestor) : instance;
			field.set(object, value);
		}
		catch (Exception e) {
			log.warn("Unable to set value:", e);
			return;
		}
	}

	private static Field getTargetField(Class c, String[] fields, int index, int ancestors) {
		try {
			Class targetClass = targetClass(c, ancestors);
			if (targetClass == null) {
				return null;
			}

			Field field = targetClass.getDeclaredField(fields[index]);
			field.setAccessible(true);

			return (index + 1) == fields.length ? field : getTargetField(field.getType(), fields, index + 1, 0);
		}
		catch (Exception e) {
			log.warn("Unable to get target field:", e);
			return null;
		}
	}

	private static Object getTargetObject(Object object, String[] fields, int index, int ancestors) {
		try {
			Class targetClass = targetClass(object.getClass(), ancestors);
			if (targetClass == null) {
				return null;
			}

			Field field = targetClass.getDeclaredField(fields[index]);
			field.setAccessible(true);

			return (index + 2) == fields.length ? field.get(object) : getTargetObject(field.get(object), fields, index + 1, ancestors);
		}
		catch (Exception e) {
			log.error("Unable to get target object:", e);
			return null;
		}
	}

	private static Class targetClass(Class c, Integer ancestors) {
		if (ancestors == 0) {
			return c;
		}

		Class target = null;
		for (int i = 0; i < ancestors; i++) {
			target = target == null ? c.getSuperclass() : target.getSuperclass();
		}
		return target;
	}

}