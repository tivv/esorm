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

import org.esorm.EntityBuilder;
import org.esorm.EntityConfiguration;
import org.esorm.RegisteredExceptionWrapper;
import org.esorm.entity.EntityProperty;
import org.esorm.entity.db.ValueExpression;
import org.esorm.impl.AQueryIterator;
import org.esorm.impl.QueryCache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author Vitalii Tymchyshyn
 */
public class ResultSetQueryIterator<E> extends AQueryIterator<E> {
    private final EntityConfiguration configuration;
    private final ResultSet resultSet;
    private final Map<ValueExpression, Integer> resultColumns;
    private PreparedFetchQuery<E> fetchQuery;
    private final QueryCache queryCache;
    private boolean closed;

    public ResultSetQueryIterator(PreparedFetchQuery<E> fetchQuery, QueryCache queryCache, EntityConfiguration configuration, ResultSet resultSet, Map<ValueExpression, Integer> resultColumns) {
        this.fetchQuery = fetchQuery;
        this.queryCache = queryCache;
        this.configuration = configuration;
        this.resultSet = resultSet;
        this.resultColumns = resultColumns;
    }

    public boolean hasNext() {
        try {
            if (isClosed()) {
                return false;
            }
            if (resultSet.isLast()) {
                close();
                return false;
            }
            return true;
        } catch (SQLException e) {
            throw new RegisteredExceptionWrapper(e);
        }
    }

    private boolean isClosed() throws SQLException {
        return closed;
    }

    public E next() {
        try {
            if (isClosed() || !resultSet.next()) {
                resultSet.close();
                throw new NoSuchElementException();
            }
            EntityBuilder<E> entityBuilder = configuration.getManager().makeBuilder();
            entityBuilder.prepare();
            for (EntityProperty property : configuration.getProperties()) {
                entityBuilder.setProperty(property.getName(),
                        resultSet.getObject(resultColumns.get(property.getExpression())));
            }
            return entityBuilder.build();
        } catch (SQLException e) {
            throw new RegisteredExceptionWrapper(e);
        }
    }

    public void remove() {
        try {
            resultSet.deleteRow();
        } catch (SQLFeatureNotSupportedException e) {
            throw new UnsupportedOperationException(e);
        } catch (SQLException e) {
            throw new RegisteredExceptionWrapper(e);
        }
    }

    public void close() {
        try {
            if (isClosed())
                return;
            closed = true;
            resultSet.close();
            if (autoCloseQuery)
                fetchQuery.close();
        } catch (SQLException e) {
            throw new RegisteredExceptionWrapper(e);
        }
    }

    public void clearEntityCache() {
        queryCache.clear();
    }

}
