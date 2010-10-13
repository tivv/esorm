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

import org.esorm.impl.PojoEntitiesManager.PojoEntityManager;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public class PojoUtils
{

    /**
     * @param s
     * @return
     */
    public static Class<?> getClass(String s)
    {
        try {
            return Class.forName(s);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static Class<?> resolveClass(String name, Iterable<String> locations) {
        for (String s : locations) {
            Class<?> entityClass;
            entityClass = getClass(s);
            if (entityClass != null)
                return entityClass;
            entityClass = getClass(s + "." + name);
            if (entityClass != null)
                return entityClass;
        }
        return null;
        
    }

}
