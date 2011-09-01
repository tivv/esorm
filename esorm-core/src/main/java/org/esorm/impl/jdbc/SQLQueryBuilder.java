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
package org.esorm.impl.jdbc;

import org.esorm.EntityConfiguration;
import org.esorm.ParsedQuery;
import org.esorm.PreparedQuery;
import org.esorm.qbuilder.QueryBuilder;
import org.esorm.qbuilder.QueryFilters;

import java.util.Map;

/**
 * @author Vitalii Tymchyshyn
 */
public class SQLQueryBuilder implements QueryBuilder{
    public QueryBuilder select(EntityConfiguration configuration) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public QueryFilters<QueryBuilder> filter() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ParsedQuery build() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <R> PreparedQuery<R> prepare() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <R> PreparedQuery<R> prepare(Map<String, Object> params) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
