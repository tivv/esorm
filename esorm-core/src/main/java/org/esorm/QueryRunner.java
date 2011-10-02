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

import org.esorm.qbuilder.QueryBuilder;

import java.util.List;

/**
 * @author Vitalii Tymchyshyn
 */
public interface QueryRunner {
    public ErrorHandler getErrorHandler();

    /**
     * @param connectionClass
     * @param <T>
     * @return
     * @throws IllegalArgumentException if there is no provider for given class
     */
    public <T> ConnectionProvider<T> getConnectionProvider(Class<T> connectionClass);

    List<ConnectionProvider> getConnectionProviders();

    Iterable<ConnectionProvider> getConnectionProvidersIterable();

    public DataAccessor getDataAccessor();

    /**
     * @return
     */
    public List<String> getEntityConfigurationLocations();

    public Iterable<String> getEntityConfigurationLocationsIterable();

    /**
     * @return
     */
    public List<String> getEntityImplementationLocations();

    public Iterable<String> getEntityImplementationLocationsIterable();

    public MutableEntityConfiguration getMutableEntityConfiguration(String name);

    public Iterable<EntitiesConfigurator> getEntitiesConfiguratorsIterable();

    /**
     * @return
     */
    public Iterable<EntitiesManager> getEntitiesManagersIterable();

    public QueryConf customize();

    <T extends Enum<T>> T getSelected(Class<T> clazz);

    <T> T get(Enum key, T defaultValue);

    <T> T get(Enum key);

    EntityConfiguration getConfiguration(String name, String configurationLocation, String managerLocation);

    EntityConfiguration getConfiguration(String name);

    EntityConfiguration getConfiguration(Class<?> configurationClass);

    QueryBuilder buildQuery();
}

