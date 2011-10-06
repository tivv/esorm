/**
 *
 * Copyright 2010-2011 Vitalii Tymchyshyn
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
package org.esorm.qbuilder;

import org.esorm.EntityConfiguration;
import org.esorm.ParsedQuery;
import org.esorm.QueryIterator;
import org.esorm.ann.Default;

import java.util.Map;

/**
 * @author Vitalii Tymchyshyn
 */
public interface QueryBuilder<R> extends Iterable<R> {
    public enum Config {
        @Default("256")
        MaxParamBlockSize
    }

    /**
     * Specify what entities to select
     *
     * @return
     */
    QueryBuilder<R> select(EntityConfiguration configuration);

    /**
     * Specify filtering criteria
     *
     * @return
     */
    QueryFilters<QueryBuilder<R>> filter();

    ParsedQuery build();

    QueryIterator<R> iterator();

    QueryIterator<R> iterator(Map<String, Object> params);
}
