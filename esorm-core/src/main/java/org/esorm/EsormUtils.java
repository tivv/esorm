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
package org.esorm;

import java.sql.Connection;

import javax.sql.DataSource;

import org.esorm.impl.DataSourceConnectionProvider;
import org.esorm.impl.FixedConnectionProvider;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public final class EsormUtils
{
    private EsormUtils() {}
    public static ConnectionProvider connect(Connection con) {
        return new FixedConnectionProvider(con);
    }

    public static ConnectionProvider connect(DataSource ds) {
        return new DataSourceConnectionProvider(ds);
    }
}
