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
import java.util.*;

import org.esorm.impl.*;
import org.esorm.utils.ParentedList;
import org.esorm.utils.ReadOnlyReverseIterable;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public class QueryConf implements QueryRunner
{
    private final QueryRunner parent;
    private ErrorHandler errorHandler;
    private ConnectionProvider connectionProvider;
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
    //TODO add cache for often queries building.
    
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
    
    public QueryConf errorHandler(ErrorHandler errorHandler) {
        setErrorHandler(errorHandler);
        return this;
    }

    public ConnectionProvider getConnectionProvider()
    {
        return connectionProvider == null ? parent.getConnectionProvider() : connectionProvider;
    }

    public void setConnectionProvider(ConnectionProvider connectionProvider)
    {
        this.connectionProvider = connectionProvider;
    }
    
    public QueryConf connectionProvider(ConnectionProvider connectionProvider) {
        setConnectionProvider(connectionProvider);
        return this;
    }

    public DataAccessor getDataAccessor()
    {
        return dataAccessor == null ? parent.getDataAccessor() : dataAccessor;
    }

    public void setDataAccessor(DataAccessor dataAccessor)
    {
        this.dataAccessor = dataAccessor;
    }
    
    public QueryConf dataAccessor(DataAccessor dataAccessor) {
        setDataAccessor(dataAccessor);
        return this;
    }

    public List<String> getEntityConfigurationLocations()
    {
        if (entityConfigurationLocations == null)
            entityConfigurationLocations = new ParentedList<String>(parent.getEntityConfigurationLocations());
        return entityConfigurationLocations;
    }

    public Iterable<String> getEntityConfigurationLocationsIterable()
    {
        if (entityConfigurationLocations == null)
            return parent.getEntityConfigurationLocationsIterable();
        else {
            if (entityConfigurationLocationsIterable == null) {
                entityConfigurationLocationsIterable = new ReadOnlyReverseIterable<String>(entityConfigurationLocations);
            }
            return entityConfigurationLocationsIterable; 
        }
    }
    
    public void setEntityConfigurationLocations(List<String> entityConfigurationLocations)
    {
        this.entityConfigurationLocations = entityConfigurationLocations;
        this.entityConfigurationLocationsIterable = null;
    }
    
    public QueryConf entityConfigurationLocations(List<String> entityConfigurationLocations)
    {
        setEntityConfigurationLocations(entityConfigurationLocations);
        return this;
    }
    
    public QueryConf entityConfigurationLocation(String location)
    {
        getEntityConfigurationLocations().add(location);
        return this;
    }

    public List<String> getEntityImplementationLocations()
    {
        if (entityImplementationLocations == null)
            entityImplementationLocations = new ParentedList<String>(parent.getEntityImplementationLocations());
        return entityImplementationLocations;
    }

    public void setEntityImplementationLocations(List<String> entityImplementationLocations)
    {
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
    public Iterable<String> getEntityImplementationLocationsIterable()
    {
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
        return get(getConfiguration(configurationClass), id);
    }
    
    public <T> T get(EntityConfiguration configuration, Object id) {
        return getDataAccessor().get(this, configuration, id);
    }
    
    
    private Map<String, EntityConfiguration> getResolvedConfigurations()
    {
        if (resolvedConfigurations == null)
            resolvedConfigurations = new HashMap<String, EntityConfiguration>();
        return resolvedConfigurations;
    }

    private Map<String, MutableEntityConfiguration> getMutableConfigurations()
    {
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
            newConfiguration = configurator.resolveConfiguration(name, configurationLocations);
            if (newConfiguration != null) {
                break;
            }
        }
        if (newConfiguration == null)
            throw new IllegalStateException("Can't find configuration for bean " + name + " under " + configurationLocations);
        
        Iterable<String> implementationLocations = implementationLocation == null ? getEntityImplementationLocations() : 
            Collections.singleton(implementationLocation);
        for (EntitiesManager manager : getEntitiesManagersIterable()) {
            EntityManager entityManager = manager.createManager(newConfiguration, implementationLocations);
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
                EntityManager entityManager = manager.createManager(newConfiguration, implementationLocations);
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
                                               String managerLocation)
    {
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
        if (mutableConfigurations != null)
        {
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

    public QueryConf customize()
    {
        return new QueryConf(this);
    }
    
    
}
