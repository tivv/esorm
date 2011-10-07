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
package org.esorm.impl.db;

import org.esorm.EntityConfiguration;
import org.esorm.PreparedQuery;
import org.esorm.QueryIterator;
import org.esorm.RegisteredExceptionWrapper;
import org.esorm.entity.db.SelectExpression;
import org.esorm.entity.db.ValueExpression;
import org.esorm.impl.AQueryIterator;
import org.esorm.impl.QueryCache;
import org.esorm.impl.parameters.PreparedStatementParameterSetter;
import org.esorm.parameters.ParameterMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.esorm.utils.AssertUtils.argNotNull;

/**
 * @author Vitalii Tymchyshyn
 */
public class PreparedFetchQuery<R> implements PreparedQuery<R> {
    private static final Logger LOG = Logger.getLogger(PreparedFetchQuery.class.getName());
    private final Connection con;
    private final String query;
    private final ParameterMapper parameterMapper;
    private final Map<ValueExpression, Integer> resultColumns;
    private final EntityConfiguration configuration;
    private final List<String> parameterIndexes;
    private final QueryCache queryCache;
    private PreparedStatement statement;
    private Object[] params;

    public PreparedFetchQuery(Connection con, QueryCache queryCache, EntityConfiguration configuration, String query, ParameterMapper parameterMapper, Map<ValueExpression, Integer> resultColumns, List<String> parameterIndexes) {
        argNotNull(parameterMapper, "Parameter mapper must not be null");
        this.con = con;
        this.queryCache = queryCache;
        this.query = query;
        this.parameterMapper = parameterMapper;
        this.resultColumns = resultColumns;
        this.configuration = configuration;
        this.parameterIndexes = parameterIndexes;
    }

    public Map<ValueExpression, Integer> getResultMapping() {
        return resultColumns;
    }

    public Map<SelectExpression, List<ValueExpression>> getResultGrouping() {
        return Collections.emptyMap();
    }

    public List<PreparedQuery> getChainedQueries() {
        return Collections.emptyList();
    }

    public EntityConfiguration getResultConfiguration() {
        return configuration;
    }

    public synchronized QueryIterator<R> iterator() {
        ensureStatement();
        try {
            Object callState = parameterMapper.process(null, new PreparedStatementParameterSetter(statement), params);
            ResultSetQueryIterator<R> firstCallIterator = makeNewIterator();
            return callState == null ? firstCallIterator : new MultiCallIterator(callState, firstCallIterator, params);
        } catch (SQLException e) {
            throw new RegisteredExceptionWrapper(e);
        }
    }

    private ResultSetQueryIterator<R> makeNewIterator() throws SQLException {
        LOG.log(Level.INFO, "Executing " + query);
        return new ResultSetQueryIterator<R>(this, queryCache, configuration, statement.executeQuery(), resultColumns);
    }

    public QueryIterator<R> iterator(Object... params) {
        return reset(params).iterator();
    }

    public QueryIterator<R> iterator(Map<String, Object> params) {
        return reset(params).iterator();
    }

    public PreparedFetchQuery<R> reset(Object... params) {
        ensureStatement();
        this.params = params;
        return this;
    }

    private void ensureStatement() {
        if (statement == null) {
            try {
                statement = con.prepareStatement(query);
            } catch (SQLException e) {
                throw new RegisteredExceptionWrapper(e);
            }
        }
    }

    public PreparedFetchQuery<R> reset(Map<String, Object> params) {
        if (parameterIndexes == null)
            throw new IllegalStateException("Query does not have named parameters");
        Object[] paramList = new Object[parameterIndexes.size()];
        for (int i = 0; i < paramList.length; i++) {
            String paramName = parameterIndexes.get(i);
            Object val = params.get(paramName);
            if (val == null && !params.containsKey(paramName))
                throw new IllegalArgumentException("Parameter " + paramName + " was expected. Put null to the map if you wish" +
                        " to set it to null");
            paramList[i] = val;
        }
        return reset(paramList);
    }


    public void close() {
        try {
            statement.close();
        } catch (SQLException e) {
            throw new RegisteredExceptionWrapper(e);
        }
    }

    private class MultiCallIterator extends AQueryIterator<R> {
        private Object callState;
        private QueryIterator<R> delegate;
        private Object[] params;

        public MultiCallIterator(Object callState, ResultSetQueryIterator<R> firstCallIterator, Object[] params) {
            this.callState = callState;
            this.delegate = firstCallIterator;
            this.params = params;
        }

        @Override
        public void clearEntityCache() {
            queryCache.clear();
        }

        @Override
        public boolean hasNext() {
            if (delegate == null)
                return false;
            while (!delegate.hasNext()) {
                delegate.close();
                delegate = null;
                if (callState == null)
                    return false;
                makeNewDelegate();
            }
            return true;
        }

        private void makeNewDelegate() {
            synchronized (PreparedFetchQuery.this) {
                callState = parameterMapper.process(callState, new PreparedStatementParameterSetter(statement), params);
                try {
                    delegate = makeNewIterator();
                } catch (SQLException e) {
                    throw new RegisteredExceptionWrapper(e);
                }
            }
        }

        @Override
        public R next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return delegate.next();
        }

        @Override
        public void remove() {
            //TODO - correctly process during delegate switch
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {
            if (delegate != null) {
                delegate.close();
                delegate = null;
            }
            if (autoCloseQuery)
                PreparedFetchQuery.this.close();
        }
    }
}
