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

import org.esorm.*;
import org.esorm.qbuilder.QueryBuilder;
import org.esorm.qbuilder.QueryFilters;
import org.esorm.qbuilder.ValueFilters;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Vitalii Tymchyshyn
 */
public class SQLQueryBuilder implements QueryBuilder{

    private final QueryRunner queryRunner;
    private EntityConfiguration entity;
    private final SQLQueryFilters filters = new SQLQueryFilters<QueryBuilder>(operation, this);

    public SQLQueryBuilder(QueryRunner queryRunner) {
        this.queryRunner = queryRunner;
    }

    public QueryBuilder select(EntityConfiguration configuration) {
        if (entity != null) {
            throw new IllegalStateException("Entity is already set in this query");
        }
        this.entity = configuration;
        return this;
    }

    public QueryFilters<QueryBuilder> filter() {
        return filters;
    }

    public ParsedQuery build() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <R> QueryIterator<R> iterator() {
        return EsormUtils.perform(queryRunner, Connection.class, new EsormUtils.PerformRunner<QueryIterator<R>, Connection>() {
            public QueryIterator<R> perform(QueryRunner queryRunner, Connection connection) {
                return build().<R>prepare(connection).iterator();
            }
        });
    }

    public <R> QueryIterator<R> iterator(final Map<String, Object> params) {
        return EsormUtils.perform(queryRunner, Connection.class, new EsormUtils.PerformRunner<QueryIterator<R>, Connection>() {
            public QueryIterator<R> perform(QueryRunner queryRunner, Connection connection) {
                return build().<R>prepare(connection).iterator(params);
            }
        });
    }

    private interface SQLQueryFilter {
        void addQueryText(StringBuilder builder);
        boolean isEmpty();
    }

    private static class NotSQLQueryBuilder<T> implements SQLQueryFilter{
        private final SQLQueryFilters<T> subFilter;

        public NotSQLQueryBuilder(T ret) {
            subFilter = new SQLQueryFilters<T>(ret);
        }

        public boolean isEmpty() {
            return subFilter.isEmpty();
        }

        public SQLQueryFilters<T> getSubFilter() {
            return subFilter;
        }

        public void addQueryText(StringBuilder builder) {
            builder.append("not(");
            subFilter.addQueryText(builder);
            builder.append(')');
        }
    }

    private static class SQLQueryFilters<T> implements QueryFilters<T>, SQLQueryFilter {
        private final T ret;
        private final String operation;
        private List<SQLQueryFilter> filters = new ArrayList<SQLQueryFilter>();

        public SQLQueryFilters(T ret) {
            this("and", ret);
        }

        public SQLQueryFilters(String operation, T ret) {
            this.operation = operation;
            this.ret = ret;
        }

        public void addQueryText(StringBuilder builder) {
            boolean first = true;
            for (SQLQueryFilter filter : filters) {
                if (!filter.isEmpty()) {
                    if (!first) {
                        builder.append(' ').append(operation).append(' ');
                    }
                    builder.append('(');
                    filter.addQueryText(builder);
                    builder.append(')');
                    first = false;
                }
            }
            if (first)
                throw new IllegalStateException("Can't add empty filter to " + builder);
        }

        public boolean isEmpty() {
            for (SQLQueryFilter filter : filters) {
                if (!filter.isEmpty())
                    return false;
            }
            return true;
        }

        public QueryFilters<QueryFilters<T>> and() {
            SQLQueryFilters<QueryFilters<T>> rc = new SQLQueryFilters<QueryFilters<T>>(this);
            filters.add(rc);
            return rc;

        }

        public QueryFilters<QueryFilters<T>> or() {
            SQLQueryFilters<QueryFilters<T>> rc = new SQLQueryFilters<QueryFilters<T>>("or", this);
            filters.add(rc);
            return rc;
        }

        public QueryFilters<QueryFilters<T>> not() {
            NotSQLQueryBuilder<QueryFilters<T>> notFilter = new NotSQLQueryBuilder<QueryFilters<T>>(this);
            filters.add(notFilter);
            return notFilter.getSubFilter();
        }

        public QueryFilters<T> ql(String textFilter) {
            throw new UnsupportedOperationException();
        }

        public T done() {
            return ret;
        }

        public ValueFilters<QueryFilters<T>> property(String name) {
            throw new UnsupportedOperationException();
        }

        public ValueFilters<QueryFilters<T>> id() {
            throw new UnsupportedOperationException();
        }

        public ValueFilters<QueryFilters<T>> query(ParsedQuery query) {
            throw new UnsupportedOperationException();
        }

        public ValueFilters<QueryFilters<T>> expression(String value) {
            throw new UnsupportedOperationException();
        }
    }
}
}
