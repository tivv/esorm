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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.esorm.impl.jdbc.DataSourceConnectionProvider;
import org.esorm.impl.jdbc.FixedConnectionProvider;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public final class EsormUtils
{
    private static Logger LOG = Logger.getLogger(EsormUtils.class.getName());
    private EsormUtils() {}
    public static ConnectionProvider connect(Connection con) {
        return new FixedConnectionProvider(con);
    }

    public static ConnectionProvider connect(DataSource ds) {
        return new DataSourceConnectionProvider(ds);
    }

    public static <R,C> R perform(QueryRunner queryRunner, Class<C> connectionClass, PerformRunner<R, C> runner) {
        ConnectionProvider<C> provider = queryRunner.getConnectionProvider(connectionClass);
        C connection = provider.takeConnection();
        try {
            return runner.perform(queryRunner, connection);
        } finally {
            try {
                provider.returnConnection(connection);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Exception closing connection", e);
            }
        }
    }

    public interface  PerformRunner<R,C> {
        R perform(QueryRunner queryRunner, C connection);
    }
}
