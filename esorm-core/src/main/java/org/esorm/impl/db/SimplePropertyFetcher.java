/**
 *
 * Copyright 2010-2015 Vitalii Tymchyshyn
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

import org.esorm.RegisteredExceptionWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Vitalii Tymchyshyn
 */
public class SimplePropertyFetcher<R> implements PropertyFetcher<R> {
    private final String propertyName;
    private final int paramNum;

    public SimplePropertyFetcher(String propertyName, int paramNum) {
        this.propertyName = propertyName;
        this.paramNum = paramNum;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public R getPropertyValue(ResultSet resultSet) {
        try
        {
            return (R) resultSet.getObject(paramNum);
        } catch (SQLException e)
        {
            throw new RegisteredExceptionWrapper(e);
        }
    }
}
