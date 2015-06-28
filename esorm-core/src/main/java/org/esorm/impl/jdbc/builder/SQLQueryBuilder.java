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
package org.esorm.impl.jdbc.builder;

import org.esorm.*;
import org.esorm.entity.EntityProperty;
import org.esorm.entity.db.Column;
import org.esorm.entity.db.FromExpression;
import org.esorm.entity.db.ValueExpression;
import org.esorm.impl.db.ParsedFetchQuery;
import org.esorm.impl.db.PropertyFetcher;
import org.esorm.impl.db.SimplePropertyFetcher;
import org.esorm.impl.db.SingleBeanPropertyFetcher;
import org.esorm.impl.parameters.FixedValueParameterMapper;
import org.esorm.impl.parameters.NopParameterTransformer;
import org.esorm.impl.parameters.TransformerParameterMapper;
import org.esorm.parameters.ParameterMapper;
import org.esorm.parameters.ParameterSetter;
import org.esorm.qbuilder.*;

import java.sql.Connection;
import java.util.*;

import static org.esorm.utils.IterableUtils.getFirstValue;
import static org.esorm.utils.IterableUtils.toList;

/**
 * @author Vitalii Tymchyshyn
 */
public class SQLQueryBuilder<R> implements QueryBuilder<R> {

    private final QueryRunner queryRunner;
    private EntityConfiguration entity;
    private final SQLQueryFilters<QueryBuilder<R>> filters = new SQLQueryFilters<QueryBuilder<R>>(this);
    private int params;

    public SQLQueryBuilder(QueryRunner queryRunner) {
        this.queryRunner = queryRunner;
    }

    public QueryBuilder<R> select(EntityConfiguration configuration) {
        if (entity != null)
        {
            throw new IllegalStateException("Entity is already set in this query");
        }
        this.entity = configuration;
        return this;
    }

    public QueryFilters<QueryBuilder<R>> filter() {
        return filters;
    }

    public ParsedQuery build() {
        BuildState buildState = new BuildState();
        buildState.append("select ");
        addRootProperties(entity, buildState);
        if (buildState.getResultColumns().isEmpty())
            throw new IllegalArgumentException("Nothing to select for " + entity.getName());
        //TODO - complex primary key by id / name
        buildState.append(" from ");
        addFromList(buildState);
        if (filters.prepare())
        {
            buildState.append(" where ");
            filters.addQueryText(buildState);
        }
        return new ParsedFetchQuery(entity, buildState.getStringBuilder().toString(),
                buildState.getParameterMapper(), buildState.getRootFetcher());
    }

    private void addRootProperties(EntityConfiguration entity, BuildState buildState) {
        //TODO: Selecting entities with optional columns only
        List<PropertyFetcher> propertyFetchers = getProperties(entity, buildState, null);
        buildState.setRootFetcher(new SingleBeanPropertyFetcher(null, entity, propertyFetchers));
    }

    private List<PropertyFetcher> getProperties(EntityConfiguration entity, BuildState buildState, Iterable<Column> joinToColumns) {
        List<PropertyFetcher> propertyFetchers = new ArrayList<PropertyFetcher>();
        addSimpleProperties(entity, buildState, propertyFetchers, joinToColumns);
        addComplexProperties(entity, buildState, propertyFetchers);
        return propertyFetchers;
    }

    private void addSimpleProperties(EntityConfiguration entity, BuildState buildState, List<PropertyFetcher> propertyFetchers, Iterable<Column> joinToColumns) {
        StringBuilder query = buildState.getStringBuilder();
        Map<FromExpression, TableSelectData> tablesInvolved = buildState.getTablesInvolved();
        final Map<ValueExpression, Integer> resultColumns = buildState.getResultColumns();
        Iterable<EntityProperty> properties = entity.getProperties();
        for (EntityProperty property : properties)
        {
            ValueExpression expression = property.getExpression();
            Integer num = resultColumns.get(expression);
            if (num == null)
            {
                num = resultColumns.size() + 1;
                resultColumns.put(expression, num);
                for (FromExpression table : expression.getTables())
                {
                    if (!tablesInvolved.containsKey(table))
                    {
                        int tableNum = tablesInvolved.size() + 1;
                        TableSelectData tableSelectData;
                        if (tablesInvolved.isEmpty())
                        {
                            tableSelectData = new TableSelectData("t" + tableNum);
                            joinToColumns = entity.getIdColumns().get(table);
                        } else
                        {
                            tableSelectData = new TableSelectData(
                                    "t" + tableNum,
                                    "inner",
                                    entity.getIdColumns().get(table),
                                    joinToColumns
                            );
                        }
                        tablesInvolved.put(table, tableSelectData);
                    }
                }
                if (num != 1)
                    query.append(',');
                expression.appendQuery(query, tablesInvolved);
            }
            propertyFetchers.add(new SimplePropertyFetcher(property.getName(), num));
        }
    }

    private void addComplexProperties(EntityConfiguration entity, BuildState buildState, List<PropertyFetcher> propertyFetchers) {
        for (Map.Entry<String, ComplexProperty> entry : entity.getComplexProperties().entrySet())
        {
            ComplexProperty property = entry.getValue();
            ComplexProperty.FetchType fetchType = queryRunner.getSelected(ComplexProperty.FetchType.class, property.getFetchType());
            switch (fetchType)
            {
                case None:
                    break;
                case Join:
                    if (property.isCollection())
                    {
                        throw new UnsupportedOperationException("Collections are not supported yet");
                    } else
                    {
                        EntityConfiguration childEntity = property.getConfiguration(queryRunner);
                        propertyFetchers.add(new SingleBeanPropertyFetcher(entry.getKey(), childEntity,
                                getProperties(childEntity, buildState, property.getJoinToColumns())));
                    }
                    break;
                case Select:
                    //TODO
                    throw new UnsupportedOperationException("Chained selects are not supported yet");
            }
        }
    }

    private void addFromList(BuildState buildState) {
        Map<FromExpression, TableSelectData> tablesInvolved = buildState.getTablesInvolved();
        StringBuilder query = buildState.getStringBuilder();
        Map<FromExpression, ? extends Iterable<Column>> primaryKeys = entity.getIdColumns();
        for (Map.Entry<FromExpression, TableSelectData> e : tablesInvolved.entrySet())
        {
            if (e.getValue().getJoinType() != null)
                query.append(' ').append(e.getValue().getJoinType()).append(" join ");
            e.getKey().appendQuery(query, e.getValue().getAlias());
            if (e.getValue().getJoinType() != null)
            {
                Iterable<Column> joinToColumns = e.getValue().getJoinToColumns();
                Iterator<Column> joinToIterator = joinToColumns.iterator();
                String toAppend = " on ";
                for (Column firstColumn : e.getValue().getJoinColumns())
                {
                    //TODO add .hasNext check
                    Column secondColumn = joinToIterator.next();
                    query.append(toAppend);
                    toAppend = " and ";
                    firstColumn.appendQuery(query, tablesInvolved);
                    query.append("=");
                    secondColumn.appendQuery(query, tablesInvolved);
                }
                //TODO add .hasNext check
            }
        }
    }

    public QueryIterator<R> iterator() {
        return EsormUtils.perform(queryRunner, Connection.class, new EsormUtils.PerformRunner<QueryIterator<R>, Connection>() {
            public QueryIterator<R> perform(QueryRunner queryRunner, Connection connection) {
                return build().<R>prepare(connection).iterator().autoCloseQuery(true);
            }
        });
    }

    public QueryIterator<R> iterator(final Map<String, Object> params) {
        return EsormUtils.perform(queryRunner, Connection.class, new EsormUtils.PerformRunner<QueryIterator<R>, Connection>() {
            public QueryIterator<R> perform(QueryRunner queryRunner, Connection connection) {
                return build().<R>prepare(connection).iterator(params).autoCloseQuery(true);
            }
        });
    }

    private class NotSQLQueryFilter<T> implements SQLQueryFilter {
        private final SQLQueryFilters<T> subFilter;

        public NotSQLQueryFilter(T ret) {
            subFilter = new SQLQueryFilters<T>(ret);
        }

        public boolean prepare() {
            return subFilter.prepare();
        }

        public SQLQueryFilters<T> getSubFilter() {
            return subFilter;
        }

        public void addQueryText(BuildState builder) {
            builder.append("not(");
            subFilter.addQueryText(builder);
            builder.append(')');
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

        public void addQueryText(BuildState builder) {
            boolean first = true;
            for (SQLQueryFilter filter : filters)
            {
                if (!first)
                {
                    builder.append(' ').append(operation).append(' ');
                }
                builder.append('(');
                filter.addQueryText(builder);
                builder.append(')');
                first = false;
            }
            if (first)
                throw new IllegalStateException("Can't add empty filter to " + builder);
        }

        public boolean prepare() {
            for (Iterator<SQLQueryFilter> iterator = filters.iterator(); iterator.hasNext(); )
            {
                SQLQueryFilter filter = iterator.next();
                if (!filter.prepare())
                    iterator.remove();
            }
            return !filters.isEmpty();
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
            return addFilter(new NotSQLQueryFilter<QueryFilters<T>>(this)).getSubFilter();
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
            return addFilter(new SQLValueFilter<QueryFilters<T>>(this, new IdColumnsSQLValue()));
        }

        public ValueFilters<QueryFilters<T>> query(ParsedQuery query) {
            throw new UnsupportedOperationException();
        }

        public ValueFilters<QueryFilters<T>> expression(String value) {
            throw new UnsupportedOperationException();
        }

    }

    private static abstract class SQLValue {
        protected abstract void addValue(BuildState builder, int valueNum);

        protected int getNumValues() {
            return 1;
        }

        protected int getNumRows() {
            return 1;
        }

        protected boolean providesValues(int numValues) {
            return numValues == getNumValues();
        }

        protected void prepare() {
        }
    }

    private class IdColumnsSQLValue extends SQLValue {
        private List<Column> idColumns;

        @Override
        protected void prepare() {
            idColumns = toList(getFirstValue(entity.getIdColumns().values()));
        }

        @Override
        protected int getNumValues() {
            return idColumns.size();
        }

        @Override
        protected void addValue(BuildState builder, int valueNum) {
            idColumns.get(valueNum).appendQuery(builder, builder.getTablesInvolved());
        }

    }

    private static class NullSQLValue extends SQLValue {
        private static NullSQLValue INSTANCE = new NullSQLValue();

        @Override
        protected void addValue(BuildState builder, int valueNum) {
            builder.append("null");
        }

        @Override
        protected boolean providesValues(int numValues) {
            return true;
        }
    }

    private static class ObjectSQLValue extends SQLValue {
        private final Object value;

        public ObjectSQLValue(Object value) {
            this.value = value;
        }

        @Override
        protected void addValue(BuildState builder, int valueNum) {
            builder.appendParameter(new FixedValueParameterMapper(builder.getNextParameterNumber(), value));
        }
    }

    private static class ObjectListSQLValue extends SQLValue {
        private final int blockSize;
        private final Iterable values;

        private ObjectListSQLValue(int blockSize, Iterable values) {
            this.blockSize = blockSize;
            this.values = values;
        }

        @Override
        protected void addValue(BuildState builder, int valueNum) {
            builder.append('(');
            final int firstParam = builder.getNextParameterNumber();
            builder.appendParameter(new ParameterMapper<Iterator>() {
                @Override
                public Iterator process(Iterator state, ParameterSetter setter, Object... inputValues) {
                    if (state == null)
                    {
                        state = values.iterator();
                        if (!state.hasNext())
                        {
                            for (int i = 0; i < blockSize; i++)
                            {
                                setter.setParameter(i + firstParam, null);
                            }
                            return null;
                        }
                    }
                    Object firstVal = state.next();
                    setter.setParameter(firstParam, firstVal);
                    for (int i = 1; i < blockSize; i++)
                    {
                        setter.setParameter(i + firstParam, state.hasNext() ? state.next() : firstVal);
                    }
                    return state.hasNext() ? state : null;
                }
            });
            for (int i = 1; i < blockSize; i++)
            {
                builder.append(',').appendParameter();
            }
            builder.append(')');
        }

    }

    private static class PositionalParamSQLValue extends SQLValue {
        private final int paramNum;

        public PositionalParamSQLValue(int paramNum) {
            this.paramNum = paramNum;
        }

        @Override
        protected void addValue(BuildState builder, int valueNum) {
            builder.appendParameter(new TransformerParameterMapper(NopParameterTransformer.INSTANCE, paramNum,
                    builder.getNextParameterNumber()));
        }
    }

    private class SQLValueFilter<R> implements SQLQueryFilter, ValueFilters<R>, FilterValue<R>, FilterValues<R> {
        private final SQLValue leftValue;
        private final R ret;
        private String operation;
        private SQLValue rightValue;

        public SQLValueFilter(R ret, SQLValue leftValue) {
            this.leftValue = leftValue;
            this.ret = ret;
        }

        public void addQueryText(BuildState builder) {
            int numValues = leftValue.getNumValues();
            if (!rightValue.providesValues(numValues))
                throw new IllegalArgumentException("Incompatible values " + leftValue + " and " + rightValue
                        + " provided for operation " + operation);
            if (numValues != 1)
                throw new IllegalArgumentException(); //TODO
            leftValue.addValue(builder, 0);
            builder.append(operation);
            rightValue.addValue(builder, 0);
        }

        public boolean prepare() {
            if (operation == null || rightValue == null)
                throw new IllegalStateException("Filter data was not filled for " + leftValue);
            leftValue.prepare();
            rightValue.prepare();
            return true;
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

        @Override
        public FilterValues<R> in() {
            setOperation(" in ");
            return this;
        }

        public R value(Object value) {
            if (value == null)
            {
                if ("=".equals(operation))
                {
                    setRightValue(NullSQLValue.INSTANCE);
                    operation = " is ";
                    return ret;
                }
                throw new IllegalArgumentException("Only equal operation for null value is supported");
            }
            return setRightValue(new ObjectSQLValue(value));
        }

        @Override
        public R values(Object... values) {
            return values(Arrays.asList(values));
        }

        @Override
        public R values(Collection values) {
            return values(values.size(), values);
        }

        @Override
        public R values(int blockSize, Iterable values) {
            blockSize = Math.min(blockSize, queryRunner.get(Config.MaxParamBlockSize, Integer.class));
            return setRightValue(new ObjectListSQLValue(blockSize, values));
        }

        @Override
        public R param(int number) {
            params = Math.max(params, number + 1);
            return setRightValue(new PositionalParamSQLValue(number));
        }

        @Override
        public R param() {
            return param(params);
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

        private R setRightValue(SQLValue value) {
            if (rightValue != null)
                throw new IllegalStateException("Right value is already set");
            rightValue = value;
            return ret;
        }
    }
}

