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

import org.esorm.*;
import org.esorm.entity.EntityProperty;
import org.esorm.entity.db.ValueExpression;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Vitalii Tymchyshyn
 */
public class DataAccessorImpl implements DataAccessor {
    public static final DataAccessorImpl INSTANCE = new DataAccessorImpl();

    private static final GetWorker<Object> GET_WORKER = new GetWorker<Object>();
    private static final Logger LOG = Logger.getLogger(DataAccessorImpl.class.getName());

    /* (non-Javadoc)
     * @see org.esorm.DataAccessor#get(org.esorm.QueryRunner, org.esorm.EntityDescription, java.lang.Object)
     */

    @SuppressWarnings("unchecked")
    public <T> T get(QueryRunner queryRunner, ParsedQuery query,
                     Object... params) {
        if (query.getType() != ParsedQuery.Type.Fetch)
            throw new IllegalArgumentException("Fetch query expected");
        return (T) run(GET_WORKER, queryRunner, query, params);
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
                } catch (Exception e) {
                    if (!hadError) {
                        queryRunner.getErrorHandler().handle(e);
                    } else {
                        LOG.log(Level.WARNING, "Second exception on connection return dropped", e);
                    }
                }
            }
        }
    }

    private interface Worker<R, P1, P2> {
        R run(Connection con, QueryRunner queryRunner, P1 param1, P2 param2) throws SQLException;
    }

    private static class GetWorker<R> implements Worker<R, ParsedQuery, Object[]> {
        /* (non-Javadoc)
         * @see org.esorm.impl.DataAccessorImpl.Worker#run(java.sql.Connection, org.esorm.QueryRunner, java.lang.Object, java.lang.Object)
         */

        public R run(Connection con, QueryRunner queryRunner,
                     ParsedQuery query, Object[] params)
                throws SQLException {
            EntityConfiguration configuration = query.getResultConfiguration();
            PreparedStatement stmt = con.prepareStatement(query.getSQL());
            ResultSet rs = null;
            List<Object> parsedId = parseId(params);
            boolean hadError = true;
            try {
                for (int i = 0; i < parsedId.size(); i++) {
                    stmt.setObject(i + 1, parsedId.get(i));
                }
                rs = stmt.executeQuery();
                if (!rs.next())
                    return null;
                Map<ValueExpression, Integer> resultColumns = query.getResultMapping();
                EntityBuilder<R> entityBuilder = configuration.getManager().makeBuilder();
                entityBuilder.prepare();
                for (EntityProperty property : configuration.getProperties()) {
                    entityBuilder.setProperty(property.getName(),
                            rs.getObject(resultColumns.get(property.getExpression())));
                }
                R rc = entityBuilder.build();
                if (rs.next())
                    throw new IllegalStateException("More than one row returned");
                hadError = false;
                return rc;
            } finally {
                if (!hadError) {
                    if (rs != null)
                        rs.close();
                    stmt.close();
                } else {
                    try {
                        if (rs != null)
                            rs.close();
                        stmt.close();
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Second exception while statement close", e);
                    }
                }
            }
        }

        /**
         * @param id
         * @return
         */
        private List<Object> parseId(Object... id) {
            // TODO Multi-column ids
            return Arrays.asList(id);
        }
    }
}
