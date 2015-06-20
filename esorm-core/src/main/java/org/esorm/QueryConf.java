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
import org.esorm.impl.MutableEntityConfigurationImpl;
import org.esorm.qbuilder.QueryBuilder;
import org.esorm.utils.ParentedList;
import org.esorm.utils.ReadOnlyReverseIterable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vitalii Tymchyshyn
 */
public class QueryConf implements QueryRunner {
    private final QueryRunner parent;
    private ErrorHandler errorHandler;
    private List<ConnectionProvider> connectionProviders;
    private Iterable<ConnectionProvider> connectionProvidersIterable;
    private DataAccessor dataAccessor;
    private List<EntitiesConfigurator> entitiesConfigurators;
    private Iterable<EntitiesConfigurator> entitiesConfiguratorsIterable;
    private List<EntitiesManager> entitiesManagers;
    private Iterable<EntitiesManager> entitiesManagersIterable;
    private List<String> entityConfigurationLocations;
    private Iterable<String> entityConfigurationLocationsIterable;
    private List<String> entityImplementationLocations;
    private Iterable<String> entityImplementationLocationsIterable;
    private Map<String, EntityConfiguration> resolvedConfigurations;
    private Map<String, MutableEntityConfiguration> mutableConfigurations;
    private Map<Class<Enum>, Enum> lastSettings;
    private Map<Enum, Object> settings;
    //TODO add cache for often queries building.

    public QueryConf() {
        this((ErrorHandler) null);
    }

    public QueryConf(ErrorHandler errorHandler) {
        this(DefaultQueryConf.INSTANCE, errorHandler);
    }

    public QueryConf(QueryRunner parent) {
        this(parent, null);
    }

    public QueryConf(QueryRunner parent, ErrorHandler errorHandler) {
        this.parent = parent;
        this.errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler == null ? parent.getErrorHandler() : errorHandler;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public QueryConf errorHandler(ErrorHandler errorHandler) {
        setErrorHandler(errorHandler);
        return this;
    }

    public <T> ConnectionProvider<T> getConnectionProvider(Class<T> connectionClass) {
        for (ConnectionProvider provider : getConnectionProvidersIterable()) {
            if (connectionClass.isAssignableFrom(provider.getConnectionClass()))
                return provider;
        }
        throw new IllegalArgumentException("No connection provider registered for " + connectionClass);
    }

    public List<ConnectionProvider> getConnectionProviders() {
        if (connectionProviders == null) {
            connectionProviders = new ParentedList<ConnectionProvider>(parent.getConnectionProviders());
        }
        return connectionProviders;
    }

    public Iterable<ConnectionProvider> getConnectionProvidersIterable() {
        if (connectionProviders == null) {
            return parent.getConnectionProvidersIterable();
        } else {
            if (connectionProvidersIterable == null) {
                connectionProvidersIterable = new ReadOnlyReverseIterable<ConnectionProvider>(connectionProviders);
            }
            return connectionProvidersIterable;
        }
    }

    public void setConnectionProviders(List<ConnectionProvider> connectionProviders) {
        this.connectionProviders = connectionProviders;
        this.connectionProvidersIterable = null;
    }

    public QueryConf connectionProvider(ConnectionProvider connectionProvider) {
        getConnectionProviders().add(connectionProvider);
        return this;
    }

    public DataAccessor getDataAccessor() {
        return dataAccessor == null ? parent.getDataAccessor() : dataAccessor;
    }

    public void setDataAccessor(DataAccessor dataAccessor) {
        this.dataAccessor = dataAccessor;
    }

    public QueryConf dataAccessor(DataAccessor dataAccessor) {
        setDataAccessor(dataAccessor);
        return this;
    }

    public List<String> getEntityConfigurationLocations() {
        if (entityConfigurationLocations == null)
            entityConfigurationLocations = new ParentedList<String>(parent.getEntityConfigurationLocations());
        return entityConfigurationLocations;
    }

    public Iterable<String> getEntityConfigurationLocationsIterable() {
        if (entityConfigurationLocations == null)
            return parent.getEntityConfigurationLocationsIterable();
        else {
            if (entityConfigurationLocationsIterable == null) {
                entityConfigurationLocationsIterable = new ReadOnlyReverseIterable<String>(entityConfigurationLocations);
            }
            return entityConfigurationLocationsIterable;
        }
    }

    public void setEntityConfigurationLocations(List<String> entityConfigurationLocations) {
        this.entityConfigurationLocations = entityConfigurationLocations;
        this.entityConfigurationLocationsIterable = null;
    }

    public QueryConf entityConfigurationLocations(List<String> entityConfigurationLocations) {
        setEntityConfigurationLocations(entityConfigurationLocations);
        return this;
    }

    public QueryConf entityConfigurationLocation(String location) {
        getEntityConfigurationLocations().add(location);
        return this;
    }

    public List<String> getEntityImplementationLocations() {
        if (entityImplementationLocations == null)
            entityImplementationLocations = new ParentedList<String>(parent.getEntityImplementationLocations());
        return entityImplementationLocations;
    }

    public void setEntityImplementationLocations(List<String> entityImplementationLocations) {
        this.entityImplementationLocations = entityImplementationLocations;
        this.entityImplementationLocationsIterable = null;
    }

    public QueryConf entityImplementationLocations(List<String> entityImplementationLocations) {
        setEntityImplementationLocations(entityImplementationLocations);
        return this;
    }


    /* (non-Javadoc)
    * @see org.esorm.QueryRunner#getEntityBeanIterable()
    */

    public Iterable<String> getEntityImplementationLocationsIterable() {
        if (entityImplementationLocations == null)
            return parent.getEntityConfigurationLocationsIterable();
        else {
            if (entityImplementationLocationsIterable == null) {
                entityImplementationLocationsIterable = new ReadOnlyReverseIterable<String>(entityImplementationLocations);
            }
            return entityImplementationLocationsIterable;
        }
    }

    public QueryConf entityImplementationLocation(String entityImplementationLocation) {
        getEntityImplementationLocations().add(entityImplementationLocation);
        return this;
    }

    public QueryConf entityLocation(Class entityLocation) {
        return entityLocation(entityLocation.getName());
    }

    public QueryConf entityLocation(String entityLocation) {
        entityConfigurationLocation(entityLocation);
        return entityImplementationLocation(entityLocation);

    }

    public <T> T get(Class<?> configurationClass, Object id) {
        return this.<T>get(getConfiguration(configurationClass), id);
    }

    public <T> T get(EntityConfiguration configuration, Object id) {
        return getDataAccessor().<T>getOne(this, Queries.byId(this, configuration).build(), id);
    }

    public <T> void delete(T value) {
        //todo
    }


    private Map<String, EntityConfiguration> getResolvedConfigurations() {
        if (resolvedConfigurations == null)
            resolvedConfigurations = new HashMap<String, EntityConfiguration>();
        return resolvedConfigurations;
    }

    private Map<String, MutableEntityConfiguration> getMutableConfigurations() {
        if (mutableConfigurations == null)
            mutableConfigurations = new HashMap<String, MutableEntityConfiguration>();
        return mutableConfigurations;
    }

    public Iterable<EntitiesConfigurator> getEntitiesConfiguratorsIterable() {
        if (entitiesConfigurators == null)
            return parent.getEntitiesConfiguratorsIterable();
        if (entitiesConfiguratorsIterable == null) {
            entitiesConfiguratorsIterable = new ReadOnlyReverseIterable<EntitiesConfigurator>(entitiesConfigurators);
        }
        return entitiesConfiguratorsIterable;
    }

    public Iterable<EntitiesManager> getEntitiesManagersIterable() {
        if (entitiesManagers == null)
            return parent.getEntitiesManagersIterable();
        if (entitiesManagersIterable == null) {
            entitiesManagersIterable = new ReadOnlyReverseIterable<EntitiesManager>(entitiesManagers);
        }
        return entitiesManagersIterable;
    }

    public EntityConfiguration resolveConfiguration(String name, String configurationLocation, String implementationLocation) {
        EntityConfiguration rc = getResolvedConfigurations().get(name);
        if (rc != null) {
            assertConfigurationConsistent(rc, configurationLocation, implementationLocation);
            return rc;
        }
        Iterable<String> configurationLocations = configurationLocation == null ? getEntityConfigurationLocations() :
                Collections.singleton(configurationLocation);
        LazyManagedEntityConfiguration newConfiguration = null;
        for (EntitiesConfigurator configurator : getEntitiesConfiguratorsIterable()) {
            //TODO - Error handling
            newConfiguration = configurator.resolveConfiguration(name, configurationLocations, configurationLocation != null);
            if (newConfiguration != null) {
                break;
            }
        }
        if (newConfiguration == null)
            throw new IllegalStateException("Can't find configuration for bean " + name + " under " + configurationLocations);

        Iterable<String> implementationLocations = implementationLocation == null ? getEntityImplementationLocations() :
                Collections.singleton(implementationLocation);
        for (EntitiesManager manager : getEntitiesManagersIterable()) {
            EntityManager entityManager = manager.createManager(newConfiguration, implementationLocations, implementationLocation != null);
            if (entityManager != null) {
                newConfiguration.setManager(entityManager);
                getResolvedConfigurations().put(newConfiguration.getName(), newConfiguration);
                return newConfiguration;
            }
        }
        if (configurationLocation != null) {
            //Try with same location (class name) as configuration
            implementationLocations = configurationLocations;
            for (EntitiesManager manager : getEntitiesManagersIterable()) {
                EntityManager entityManager = manager.createManager(newConfiguration, implementationLocations, true);
                if (entityManager != null) {
                    newConfiguration.setManager(entityManager);
                    getResolvedConfigurations().put(newConfiguration.getName(), newConfiguration);
                    return newConfiguration;
                }
            }
        }
        throw new IllegalStateException("Can't find manager for bean " + name + " under " + implementationLocations);
    }

    /**
     * @param configuration
     * @param configurationLocation
     * @param managerLocation
     */
    private void assertConfigurationConsistent(EntityConfiguration configuration,
                                               String configurationLocation,
                                               String managerLocation) {
        if (configurationLocation != null && !configurationLocation.equals(configuration.getLocation()))
            throw new IllegalStateException("Requested entity " + configuration.getName() + " has configuration location " +
                    configurationLocation + " different to already resolved one: " + configuration.getLocation());
        if (managerLocation != null && !managerLocation.equals(configuration.getManager().getLocation()))
            throw new IllegalStateException("Requested entity " + configuration.getName() + " has manager location " +
                    managerLocation + " different to already resolved one: " + configuration.getManager().getLocation());
    }

    public MutableEntityConfiguration mutateConfiguration(EntityConfiguration configuration) {
        return mutateConfiguration(configuration.getName(), configuration);
    }

    public MutableEntityConfiguration mutateConfiguration(String name, EntityConfiguration configuration) {
        if (getMutableConfigurations().containsKey(name))
            throw new IllegalStateException("Mutable configuration with name " + name + " already exists");
        MutableEntityConfiguration rc = new MutableEntityConfigurationImpl(name, configuration);
        getMutableConfigurations().put(name, rc);
        return rc;
    }

    public MutableEntityConfiguration getMutableEntityConfiguration(String name) {
        if (mutableConfigurations != null) {
            MutableEntityConfiguration rc = mutableConfigurations.get(name);
            if (rc != null)
                return rc;
        }
        return parent.getMutableEntityConfiguration(name);
    }

    public EntityConfiguration getConfiguration(String name, String configurationLocation, String managerLocation) {
        EntityConfiguration rc = getMutableEntityConfiguration(name);
        if (rc != null) {
            assertConfigurationConsistent(rc, configurationLocation, managerLocation);
            return rc;
        }
        return resolveConfiguration(name, configurationLocation, managerLocation);

    }

    public EntityConfiguration getConfiguration(String name) {
        return getConfiguration(name, null, null);
    }

    public EntityConfiguration getConfiguration(Class<?> configurationClass) {
        return getConfiguration(configurationClass.getSimpleName(), configurationClass.getName(), null);
    }

    public <T> QueryBuilder<T> buildQuery() {
        return getDataAccessor().buildQuery(this);
    }

    public QueryConf customize() {
        return new QueryConf(this);
    }

    public QueryConf set(Enum key) {
        return set(key, true);
    }

    public QueryConf set(Enum key, Object value) {
        if (lastSettings == null) {
            lastSettings = new HashMap<Class<Enum>, Enum>();
            settings = new HashMap<Enum, Object>();
        }
        lastSettings.put(key.getDeclaringClass(), key);
        settings.put(key, value);
        return this;
    }

    @Override
    public <T extends Enum<T>> T getSelected(Class<T> clazz, T primaryValue) {
        return primaryValue != null ? primaryValue : getSelected(clazz);
    }

    public <T extends Enum<T>> T getSelected(Class<T> clazz) {
        T rc = lastSettings == null ? null : (T) lastSettings.get(clazz);
        return rc == null ? parent.getSelected(clazz) : rc;
    }

    @Override
    public <T> T get(Enum key, Class<T> resultClass, T defaultValue) {
        T rc = settings == null ? null : (T) settings.get(key);
        return rc == null ? parent.<T>get(key, resultClass, defaultValue) : rc;
    }

    @Override
    public <T> T get(Enum key, T defaultValue) {
        return get(key, (Class<T>) defaultValue.getClass(), defaultValue);
    }

    public <T> T get(Enum key, Class<T> resultClass) {
        return get(key, resultClass, null);
    }


}
