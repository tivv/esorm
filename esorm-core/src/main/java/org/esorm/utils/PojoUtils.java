/**
 *
 * Copyright 2010 Vitalii Tymchyshyn
 * This file is part of EsORM.
 *
 * EsORM is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EsORM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with EsORM.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.esorm.utils;

import java.util.*;

/**
 * @author Vitalii Tymchyshyn
 */
public class PojoUtils {

    /**
     * @param s
     * @return
     */
    public static Class<?> getClass(String s) {
        try {
            return Class.forName(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static Class<?> resolveClass(String name, Iterable<String> locations, boolean locationOverride) {

        return resolveClass(name, locations, locationOverride, ClassNameFilter.INSTANCE);
    }

    public static Class<?> resolveClass(String name, Iterable<String> locations, boolean locationOverride, NameFilter filter) {
        for (String s : locations) {
            Class<?> entityClass;
            entityClass = getClass(s);
            if (entityClass != null && filter.accept(entityClass, name, locationOverride))
                return entityClass;
            entityClass = getClass(s + "." + name);
            if (entityClass != null && filter.accept(entityClass, name, locationOverride))
                return entityClass;
        }
        return null;

    }

    public interface NameFilter {
        boolean accept(Class<?> clazz, String name, boolean locationOverride);
    }

    public static class ClassNameFilter implements NameFilter {
        public static final ClassNameFilter INSTANCE = new ClassNameFilter();

        /* (non-Javadoc)
         * @see org.esorm.utils.PojoUtils.NameFilter#accept(java.lang.Class, java.lang.String, boolean)
         */

        public boolean accept(Class<?> clazz, String name,
                              boolean locationOverride) {
            return locationOverride || clazz.getSimpleName().equals(name);
        }
    }

    public static boolean isCollectionClass(Class<?> clazz) {
        return clazz.isArray() || Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz);
    }

    public static boolean isSimpleClass(Class<?> clazz) {
        return clazz.isPrimitive() || clazz.isEnum()
                || String.class.isAssignableFrom(clazz)
                || Number.class.isAssignableFrom(clazz)
                || String.class.isAssignableFrom(clazz)
                || Boolean.class.isAssignableFrom(clazz)
                || Date.class.isAssignableFrom(clazz)
                || Calendar.class.isAssignableFrom(clazz)
                || UUID.class.isAssignableFrom(clazz);
    }

    public static boolean isComplexClass(Class<?> clazz) {
        return !isSimpleClass(clazz);
    }
}
