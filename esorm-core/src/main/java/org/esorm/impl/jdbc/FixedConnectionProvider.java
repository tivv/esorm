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
package org.esorm.impl.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.esorm.ConnectionProvider;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public class FixedConnectionProvider
implements ConnectionProvider
{
    private final Connection connection;
    
    public FixedConnectionProvider(Connection connection)
    {
        this.connection = connection;
    }

    /* (non-Javadoc)
     * @see org.esorm.ConnectionProvider#takeConnection()
     */
    public Connection takeConnection()
    {
        return connection;
    }

    /* (non-Javadoc)
     * @see org.esorm.ConnectionProvider#returnConnection(java.sql.Connection)
     */
    public void returnConnection(Connection con)
    {
        if (con != connection)
            throw new IllegalArgumentException("You must return same connection you've got");
    }
}
