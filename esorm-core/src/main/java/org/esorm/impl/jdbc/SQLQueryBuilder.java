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
import org.esorm.entity.EntityProperty;
import org.esorm.entity.db.Column;
import org.esorm.entity.db.SelectExpression;
import org.esorm.entity.db.ValueExpression;
import org.esorm.impl.db.ParsedFetchQuery;
import org.esorm.impl.parameters.*;
import org.esorm.parameters.ParameterMapper;
import org.esorm.qbuilder.FilterValue;
import org.esorm.qbuilder.QueryBuilder;
import org.esorm.qbuilder.QueryFilters;
import org.esorm.qbuilder.ValueFilters;

import javax.security.auth.login.Configuration;
import java.sql.Connection;
import java.util.*;

/**
 * @author Vitalii Tymchyshyn
 */
public class SQLQueryBuilder implements QueryBuilder{

    private final QueryRunner queryRunner;
    private EntityConfiguration entity;
    private final SQLQueryFilters filters = new SQLQueryFilters<QueryBuilder>(this);

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
        StringBuilder query = new StringBuilder();
        Map<SelectExpression, String> tablesInvolved = new HashMap<SelectExpression, String>();
        final Map<ValueExpression, Integer> resultColumns = new HashMap<ValueExpression, Integer>();
        query.append("select ");
        Iterable<EntityProperty> properties = entity.getProperties();
        for (EntityProperty property : properties) {
            ValueExpression expression = property.getExpression();
            if (!resultColumns.containsKey(expression)) {
                int num = resultColumns.size() + 1;
                resultColumns.put(expression, num);
                for (SelectExpression table : expression.getTables()) {
                    if (!tablesInvolved.containsKey(table)) {
                        int tableNum = tablesInvolved.size() + 1;
                        tablesInvolved.put(table, "t" + tableNum);
                    }
                }
                if (num != 1)
                    query.append(',');
                expression.appendQuery(query, tablesInvolved);
            }
        }
        if (resultColumns.isEmpty())
            throw new IllegalArgumentException("Nothing to select for " + entity.getName());
        //TODO - complex primary key by id / name
        query.append(" from ");
        Iterable<Column> firstTablePK = null;
        Map<SelectExpression, ? extends Iterable<Column>> primaryKeys = entity.getIdColumns();
        for (Map.Entry<SelectExpression, String> e : tablesInvolved.entrySet()) {
            if (firstTablePK != null)
                query.append(" join ");
            e.getKey().appendQuery(query, e.getValue());
            Iterable<Column> primaryKey = primaryKeys.get(e.getKey());
            if (primaryKey == null)
                throw new IllegalStateException("Table " + e.getKey() + " does not have primary key specified");
            if (firstTablePK == null) {
                firstTablePK = primaryKey;
            } else {
                Iterator<Column> primaryKeyIterator = primaryKey.iterator();
                String toAppend = " on ";
                for (Column firstColumn : firstTablePK) {
                    //TODO add .hasNext check
                    Column secondColumn = primaryKeyIterator.next();
                    query.append(toAppend);
                    toAppend = " and ";
                    firstColumn.appendQuery(query, tablesInvolved);
                    query.append("=");
                    secondColumn.appendQuery(query, tablesInvolved);
                }
                //TODO add .hasNext check
            }
        }
        ParameterMapper parameterMapper;
        if (filters.isEmpty()) {
            parameterMapper = NoParameterMapper.INSTANCE;
        } else {
            query.append(" where ");
            filters.addQueryText(query);
            parameterMapper = filters.getParameterMapper();
            if (parameterMapper == null)
                parameterMapper = NoParameterMapper.INSTANCE;
        }
        return new ParsedFetchQuery(entity, query.toString(),
                parameterMapper, resultColumns);
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

        ParameterMapper getParameterMapper();
    }

    private class NotSQLQueryBuilder<T> implements SQLQueryFilter{
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

        public ParameterMapper getParameterMapper() {
            return subFilter.getParameterMapper();
        }
    }

    private class SQLQueryFilters<T> implements QueryFilters<T>, SQLQueryFilter {
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

        private <X extends SQLQueryFilter> X addFilter(X rc) {
            filters.add(rc);
            return rc;
        }

        public QueryFilters<QueryFilters<T>> and() {
            return addFilter(new SQLQueryFilters<QueryFilters<T>>(this));
        }

        public QueryFilters<QueryFilters<T>> or() {
            return addFilter(new SQLQueryFilters<QueryFilters<T>>("or", this));
        }

        public QueryFilters<QueryFilters<T>> not() {
            return addFilter(new NotSQLQueryBuilder<QueryFilters<T>>(this)).getSubFilter();
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
            return addFilter(new SQLValueFilter<QueryFilters<T>>(this, new IdSQLValue()));
        }

        public ValueFilters<QueryFilters<T>> query(ParsedQuery query) {
            throw new UnsupportedOperationException();
        }

        public ValueFilters<QueryFilters<T>> expression(String value) {
            throw new UnsupportedOperationException();
        }

        public ParameterMapper getParameterMapper() {
            switch (filters.size()) {
                case 0:
                    return null;
                case 1:
                    return filters.get(0).getParameterMapper();
                default:
                    List<ParameterMapper> childrenMappers = new ArrayList<ParameterMapper>(filters.size());
                    for (SQLQueryFilter child : filters) {
                        ParameterMapper childMapper = child.getParameterMapper();
                        if (childMapper != null)
                            childrenMappers.add(childMapper);
                    }
                    return getParameterMapper(childrenMappers);
            }
        }

        private ParameterMapper getParameterMapper(List<ParameterMapper> childrenMappers) {
            switch (childrenMappers.size()) {
                case 0:
                    return null;
                case 1:
                    return childrenMappers.get(0);
                default:
                    return new MultiParameterMapper(childrenMappers.toArray(new ParameterMapper[childrenMappers.size()]));
            }
        }
    }

    private static abstract class SQLValue {
        protected abstract void addValue(StringBuilder builder);
        protected ParameterMapper getParameterMapper() {return null;}
    }
    private class IdSQLValue extends SQLValue {
        @Override
        protected void addValue(StringBuilder builder) {
            Set<SelectExpression> idExpressions = entity.getIdColumns().keySet();
            if (idExpressions.size() != 1)
                throw new UnsupportedOperationException("There should be only 1 id column in " + entity + " and found " +
                idExpressions.size());
            idExpressions.iterator().next().appendQuery(builder, "this_");
        }

        @Override
        protected ParameterMapper getParameterMapper() {
            return new TransformerParameterMapper(new IdParameterTransformer(), );
        }
    }
    private static class NullSQLValue extends SQLValue {
        private static NullSQLValue INSTANCE = new NullSQLValue();

        @Override
        protected void addValue(StringBuilder builder) {
            builder.append("null");
        }
    }

    private static class SQLValueFilter<R> implements SQLQueryFilter, ValueFilters<R>, FilterValue<R>{
        private final SQLValue leftValue;
        private final R ret;
        private String operation;
        private SQLValue rightValue;

        public SQLValueFilter(R ret, SQLValue leftValue) {
            this.leftValue = leftValue;
            this.ret = ret;
        }

        public void addQueryText(StringBuilder builder) {
            leftValue.addValue(builder);
            builder.append(operation);
            rightValue.addValue(builder);
        }

        @Override
        public ParameterMapper getParameterMapper() {
            ParameterMapper left = leftValue.getParameterMapper();
            ParameterMapper right = rightValue.getParameterMapper();
            if (left == null)
                return right;
            if (right == null)
                return left;
            return new MultiParameterMapper(left, right);
        }

        public boolean isEmpty() {
            if (operation == null || rightValue == null)
                throw new IllegalStateException("Filter data was not filled for " + leftValue);
            return false;
        }

        public FilterValue<R> eq() {
            setOperation("=");
            return this;
        }

        public FilterValue<R> gt() {
            setOperation(">");
            return this;
        }

        public FilterValue<R> lt() {
            setOperation("<");
            return this;
        }

        public FilterValue<R> ge() {
            setOperation(">=");
            return this;
        }

        public FilterValue<R> le() {
            setOperation("<=");
            return this;
        }

        public R value(Object value) {
            throw new UnsupportedOperationException();
        }

        public R param(String name) {
            throw new UnsupportedOperationException();
        }

        public R query(ParsedQuery subQuery) {
            throw new UnsupportedOperationException();
        }

        public R expression(String expression) {
            throw new UnsupportedOperationException();
        }

        public R isNull() {
            setOperation(" is ");
            rightValue = NullSQLValue.INSTANCE;
            return ret;
        }

        private void setOperation(String operation) {
            if (this.operation != null)
                throw new IllegalStateException("You must not call operation method multiple times");
            this.operation = operation;
        }
    }
}

