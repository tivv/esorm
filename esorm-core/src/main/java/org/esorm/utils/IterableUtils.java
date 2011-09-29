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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vitalii Tymchyshyn
 */
public class IterableUtils {
    public static <R> List<R> toList(Iterable<R> values) {
        return values instanceof List ? (List<R>) values : copyToList(values);
    }

    public static <R> List<R> copyToList(Iterable<R> values) {
        List<R> rc = new ArrayList<R>();
        for (R value : values) {
            rc.add(value);
        }
        return rc;
    }

    public static <R> R getSingleValue(Iterable<R> collection) {
        return getSingleValue(collection, "Single value expected");

    }

    public static <R> R getSingleValue(Iterable<R> collection, String errorMessage) {
        Iterator<R> iterator = collection.iterator();
        if (!iterator.hasNext())
            throw new IllegalArgumentException(errorMessage);
        R rc = iterator.next();
        if (iterator.hasNext())
            throw new IllegalArgumentException(errorMessage);
        return rc;
    }

    public static <R> R getFirstValue(Iterable<R> collection) {
        return collection.iterator().next();
    }
}
