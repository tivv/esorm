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

import org.esorm.QueryIterator;
import org.esorm.RegisteredExceptionWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.NoSuchElementException;

/**
 * @author Vitalii Tymchyshyn
 */
public class ResultSetQueryIterator implements QueryIterator {
    private final ResultSet resultSet;

    public ResultSetQueryIterator(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public boolean hasNext() {
        try {
            return !resultSet.isClosed() && !resultSet.isLast();
        } catch (SQLException e) {
            throw new RegisteredExceptionWrapper(e);
        }
    }

    public Object next() {
        try {
            if (resultSet.isClosed() || !resultSet.next()) {
                resultSet.close();
                throw new NoSuchElementException();
            }
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
}
