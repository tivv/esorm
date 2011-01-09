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
package org.esorm.impl;

import org.esorm.EntityConfiguration;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author Vitalii Tymchyshyn
 */
public class QueryCache {
    private final Map<EntityConfiguration, Map<Object, Object>> entityCache = new IdentityHashMap<EntityConfiguration, Map<Object, Object>>();

    public Object get(EntityConfiguration configuration, Object primaryKeyData) {
        Map<Object, Object> confCache = entityCache.get(configuration);
        return confCache == null ? null : confCache.get(primaryKeyData);
    }

    public void put(EntityConfiguration configuration, Object primaryKeyData, Object value) {
        Map<Object, Object> confCache = entityCache.get(configuration);
        if (confCache == null) {
            confCache = new HashMap<Object, Object>();
            entityCache.put(configuration, confCache);
        }
        confCache.put(primaryKeyData, value);
    }

    public void clear() {
        entityCache.clear();
    }
}
