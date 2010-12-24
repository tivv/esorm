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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Vitalii Tymchyshyn
 */
public class PreparedFetchQuery<R> implements PreparedQuery<R> {
    private final Map<ValueExpression, Integer> resultColumns;
    private final EntityConfiguration configuration;
    private final PreparedStatement statement;

    public PreparedFetchQuery(EntityConfiguration configuration, PreparedStatement stmt, Map<ValueExpression, Integer> resultColumns) {
        this.resultColumns = resultColumns;
        this.configuration = configuration;
        statement = stmt;
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

    public QueryIterator<R> iterator() {
        try {
            return new ResultSetQueryIterator<R>(configuration, statement.executeQuery(), resultColumns);
        } catch (SQLException e) {
            throw new RegisteredExceptionWrapper(e);
        }
    }

    public void close() {
        try {
            statement.close();
        } catch (SQLException e) {
            throw new RegisteredExceptionWrapper(e);
        }
    }
}
