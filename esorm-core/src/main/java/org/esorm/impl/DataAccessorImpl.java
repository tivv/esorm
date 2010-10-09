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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.esorm.*;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public class DataAccessorImpl implements DataAccessor
{
    public static final DataAccessorImpl INSTANCE = new DataAccessorImpl();
    
    private static final GetWorker<Object> GET_WORKER = new GetWorker<Object>();
    private static final Logger LOG = Logger.getLogger(DataAccessorImpl.class.getName());

    /* (non-Javadoc)
     * @see org.esorm.DataAccessor#get(org.esorm.QueryRunner, org.esorm.EntityDescription, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public <T> T get(QueryRunner queryRunner, EntityDescription description,
                     Object id)
    {
        return (T)run(GET_WORKER, queryRunner, description, id);
    }
    
    private <T, P1, P2> T run(Worker<T, P1, P2> worker, QueryRunner queryRunner, P1 param1, P2 param2) {
        Connection con = null;
        boolean hadError = true;
        try {
            con = queryRunner.getConnectionProvider().takeConnection();
            T rc = worker.run(con, queryRunner, param1, param2);
            hadError = false;
            return rc;
        } catch (Exception e) {
            queryRunner.getErrorHandler().handle(e);
            throw new IllegalStateException("Error handler could not handle", e);
        } finally {
            if (con != null) {
                try {
                    queryRunner.getConnectionProvider().returnConnection(con);
                } catch (Exception e) 
                {
                    if (!hadError) {
                        queryRunner.getErrorHandler().handle(e);
                    } else {
                        LOG.log(Level.WARNING , "Second exception on connection return dropped", e);
                    }
                }
            }
        }
    }
    
    private interface Worker<R, P1, P2> {
        R run(Connection con, QueryRunner queryRunner, P1 param1, P2 param2) throws SQLException;
    }
    
    private static class GetWorker<R> implements Worker<R, EntityDescription, Object>
    {
        /* (non-Javadoc)
         * @see org.esorm.impl.DataAccessorImpl.Worker#run(java.sql.Connection, org.esorm.QueryRunner, java.lang.Object, java.lang.Object)
         */
        public R run(Connection con, QueryRunner queryRunner,
                     EntityDescription param1, Object param2)
        throws SQLException
        {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
