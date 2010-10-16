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

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.esorm.*;
import org.esorm.entity.EntityProperty;
import org.esorm.entity.db.*;

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
    public <T> T get(QueryRunner queryRunner, EntityConfiguration configuration,
                     Object id)
    {
        return (T)run(GET_WORKER, queryRunner, configuration, id);
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

    private static class GetWorker<R> implements Worker<R, EntityConfiguration, Object>
    {
        /* (non-Javadoc)
         * @see org.esorm.impl.DataAccessorImpl.Worker#run(java.sql.Connection, org.esorm.QueryRunner, java.lang.Object, java.lang.Object)
         */
        public R run(Connection con, QueryRunner queryRunner,
                     EntityConfiguration configuration, Object id)
        throws SQLException
        {
            StringBuilder query = new StringBuilder();
            Map<SelectExpression, String> tablesInvolved = new HashMap<SelectExpression, String>();
            Map<ValueExpression, Integer> resultColumns =  new HashMap<ValueExpression, Integer>(); 
            query.append("select ");
            Iterable<EntityProperty> properties = configuration.getProperties();
            for (EntityProperty property : properties) {
                ValueExpression expression = property.getExpression();
                if (!resultColumns.containsKey(expression)) {
                    int num = resultColumns.size() + 1;
                    resultColumns.put(expression, num);
                    for (SelectExpression table : expression.getTables()) {
                        if (!tablesInvolved.containsKey(table)) {
                            int tableNum = tablesInvolved.size() + 1;
                            tablesInvolved.put(table, "t" + tableNum);
                        }
                    }
                    if (num != 1)
                        query.append(',');
                    expression.appendQuery(query, tablesInvolved);
                }
            }
            if (resultColumns.isEmpty())
                throw new IllegalArgumentException("Nothing to select for " + configuration.getName());
            //TODO - complex primary key by id / name
            query.append(" from ");
            Iterable<Column> firstTablePK = null;
            Map<SelectExpression, ? extends Iterable<Column>> primaryKeys = configuration.getPrimaryKeys();
            for (Entry<SelectExpression, String> e : tablesInvolved.entrySet()) {
                if (firstTablePK != null)
                    query.append(" join ");
                e.getKey().appendQuery(query, e.getValue());
                Iterable<Column> primaryKey = primaryKeys.get(e.getKey());
                if (primaryKey == null)
                    throw new IllegalStateException("Table " + e.getKey() + " does not have primary key specified");
                if (firstTablePK == null) {
                    firstTablePK = primaryKey;
                } else {
                    Iterator<Column> primaryKeyIterator = primaryKey.iterator();
                    String toAppend = " on ";
                    for (Column firstColumn : firstTablePK) {
                        //TODO add .hasNext check
                        Column secondColumn = primaryKeyIterator.next();
                        query.append(toAppend);
                        toAppend = " and ";
                        firstColumn.appendQuery(query, tablesInvolved);
                        query.append("=");
                        secondColumn.appendQuery(query, tablesInvolved);
                    }
                    //TODO add .hasNext check
                }
            }
            String toAppend = " where ";
            for(Column pkColumn : firstTablePK) {
                query.append(toAppend);
                toAppend = " and ";
                pkColumn.appendQuery(query, tablesInvolved);
                query.append("=?");
            }
            PreparedStatement stmt = con.prepareStatement(query.toString());
            ResultSet rs = null;
            List<Object> parsedId = parseId(id);
            boolean hadError = true;
            try {
                int i = 0;
                for(Column pkColumn : firstTablePK) {
                    stmt.setObject(i + 1, parsedId.get(i));
                    i++;
                }
                rs = stmt.executeQuery();
                if (!rs.next())
                    return null;
                EntityBuilder<R> entityBuilder = configuration.getManager().makeBuilder();
                entityBuilder.prepare();
                for (EntityProperty property: properties) {
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
        private List<Object> parseId(Object id)
        {
            // TODO Multi-column ids
            return Collections.singletonList(id);
        }
    }
}
