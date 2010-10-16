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
package org.esorm.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.esorm.ConnectionProvider;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public class DataSourceConnectionProvider
implements ConnectionProvider
{
    private DataSource dataSource;

    public DataSourceConnectionProvider()
    {
    }

    public DataSourceConnectionProvider(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    /* (non-Javadoc)
     * @see org.esorm.ConnectionProvider#takeConnection()
     */
    public Connection takeConnection()
    throws SQLException
    {
        return dataSource.getConnection();
    }

    /* (non-Javadoc)
     * @see org.esorm.ConnectionProvider#returnConnection(java.sql.Connection)
     */
    public void returnConnection(Connection con)
    throws SQLException
    {
        con.close();
    }

}
