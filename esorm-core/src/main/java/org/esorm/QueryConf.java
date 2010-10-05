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

import org.esorm.impl.DefaultQueryConf;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public class QueryConf implements QueryRunner
{
    private final QueryRunner parent;
    private ErrorHandler errorHandler;
    private ConnectionProvider connectionProvider;
    
    public QueryConf() 
    {
        this((ErrorHandler)null);
    }

    public QueryConf(ErrorHandler errorHandler)
    {
        this(DefaultQueryConf.INSTANCE, errorHandler);
    }

    public QueryConf(QueryRunner parent)
    {
        this(parent, null);
    }
    
    public QueryConf(QueryRunner parent, ErrorHandler errorHandler)
    {
        this.parent = parent;
        this.errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler()
    {
        return errorHandler == null ? parent.getErrorHandler() : errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;
    }

    public ConnectionProvider getConnectionProvider()
    {
        return connectionProvider == null ? parent.getConnectionProvider() : connectionProvider;
    }

    public void setConnectionProvider(ConnectionProvider connectionProvider)
    {
        this.connectionProvider = connectionProvider;
    }

    public QueryRunner getParent()
    {
        return parent;
    }
    
    
}
