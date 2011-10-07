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
package org.esorm.impl.parameters;

import org.esorm.RegisteredExceptionWrapper;
import org.esorm.parameters.ParameterSetter;
import org.esorm.utils.AssertUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Vitalii Tymchyshyn
 */
public class PreparedStatementParameterSetter implements ParameterSetter {
    private final PreparedStatement statement;

    public PreparedStatementParameterSetter(PreparedStatement statement) {
        AssertUtils.argNotNull(statement, "Statement must be filled");
        this.statement = statement;
    }

    public void setParameter(int number, Object value) {
        try {
            statement.setObject(number + 1, value);
        } catch (SQLException e) {
            throw new RegisteredExceptionWrapper(e);
        }
    }
}
