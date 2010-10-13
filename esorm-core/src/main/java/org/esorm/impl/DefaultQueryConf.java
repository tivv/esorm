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

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.esorm.*;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public class DefaultQueryConf
implements QueryRunner
{
    private static final Logger LOG = Logger.getLogger(DefaultQueryConf.class.getName());
    public static final DefaultQueryConf INSTANCE = new DefaultQueryConf();
    private static final Iterable<EntitiesConfigurator> configurators = loadServices(EntitiesConfigurator.class);
    private static final Iterable<EntitiesManager> managers = loadServices(EntitiesManager.class);
    
    private DefaultQueryConf() {
        
    }

    public ErrorHandler getErrorHandler()
    {
        return ErrorHandlerImpl.INSTANCE;
    }

    public ConnectionProvider getConnectionProvider()
    {
        throw new UnsupportedOperationException("There is no default ConnectionProvider. Please supply one with setConnectionProvider method");
    }

    /* (non-Javadoc)
     * @see org.esorm.QueryRunner#getDataAccessor()
     */
    public DataAccessor getDataAccessor()
    {
        return DataAccessorImpl.INSTANCE;
    }

    /* (non-Javadoc)
     * @see org.esorm.QueryRunner#getConfigurationImports()
     */
    public List<String> getEntityConfigurationLocations()
    {
        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.esorm.QueryRunner#getConfigurationImportsIterable()
     */
    public Iterable<String> getEntityConfigurationLocationsIterable()
    {
        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.esorm.QueryRunner#getEntityBeanImports()
     */
    public List<String> getEntityImplementationLocations()
    {
        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.esorm.QueryRunner#getEntityBeanIterable()
     */
    public Iterable<String> getEntityImplementationLocationsIterable()
    {
        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.esorm.QueryRunner#getMutableEntityConfiguration(java.lang.String)
     */
    public MutableEntityConfiguration getMutableEntityConfiguration(String name)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.esorm.QueryRunner#getEntitiesConfiguratorsIterable()
     */
    public Iterable<EntitiesConfigurator> getEntitiesConfiguratorsIterable()
    {
        return configurators;
    }

    /* (non-Javadoc)
     * @see org.esorm.QueryRunner#getEntitiesManagersIterable()
     */
    public Iterable<EntitiesManager> getEntitiesManagersIterable()
    {
        return managers;
    }

    public QueryConf customize()
    {
        return new QueryConf(this);
    }
    
    @SuppressWarnings("unchecked")
    private static <T> List<T> loadServices(Class<T> clazz) {
        final Map<T, Double> services = new IdentityHashMap<T, Double>();
        Enumeration<URL> resourceList;
        try
        {
            resourceList = clazz.getClassLoader().getResources("/META-INF/services" + clazz.getName() + ".properties");
        }
        catch (IOException e)
        {
            throw new RegisteredExceptionWrapper("Could not load resources for " + clazz, e);
        }
        while(resourceList.hasMoreElements()) {
            URL resource = resourceList.nextElement();
            Properties properties = new Properties();
            try
            {
                properties.load(resource.openStream());
            }
            catch (IOException e)
            {
                LOG.log(Level.WARNING, "Could not load service list for " + clazz + " from " + resource, e);
            }
            for (Entry<Object, Object> e : properties.entrySet()) {
                T service;
                try
                {
                    service = (T) Class.forName(e.getKey().toString()).newInstance();
                }
                catch (Exception ex)
                {
                    LOG.log(Level.WARNING, "Service " + e.getKey() + " defined in " + resource + 
                        " could not be loaded and was skipped", ex);
                    continue;
                }
                Double priority;
                try {
                    priority = Double.valueOf(e.getValue().toString());
                } catch (NumberFormatException ex) {
                    LOG.warning("Service " + e.getKey() + " defined in " + resource + 
                        " has invalid priority " + e.getValue() + ": " + ex.getMessage() +
                        " Default priority used");
                    priority = 0.0;
                }
                services.put(service, priority);
            }
        }
        List<T> rc = new ArrayList<T>(services.keySet());
        Collections.sort(rc, new Comparator<T>()
            {

                public int compare(T o1, T o2)
                {
                    return services.get(o2).compareTo(services.get(o1));
                }
            });
        return Collections.unmodifiableList(rc);
    }
}
