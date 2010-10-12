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

import java.util.Map;

import org.esorm.*;
import org.jcloudlet.bean.Property;
import org.jcloudlet.bean.impl.PropertySelectorImpl;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public class PojoEntitiesManager
implements EntitiesManager
{

    /* (non-Javadoc)
     * @see org.esorm.EntitiesManager#createManager(org.esorm.EntityConfiguration, java.lang.Iterable)
     */
    public EntityManager createManager(EntityConfiguration newConfiguration,
                                       Iterable<String> implementationLocations)
    {
        Class<?> entityClass = null;
        for (String s : implementationLocations) {
            entityClass = getClass(s);
            if (entityClass != null)
                return new PojoEntityManager(s, entityClass);
            entityClass = getClass(s + "." + newConfiguration.getName());
            if (entityClass != null)
                return new PojoEntityManager(s, entityClass);
        }
        return null;
    }

    /**
     * @param s
     * @return
     */
    private Class<?> getClass(String s)
    {
        try {
            return Class.forName(s);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static class PojoEntityManager implements EntityManager {
        private final String location;
        private final Class<?> clazz;
        private final Map<String, Property> properties;

        private PojoEntityManager(String location, Class<?> clazz)
        {
            this.location = location;
            this.clazz = clazz;
            this.properties = new PropertySelectorImpl(clazz).asMap();
        }
        
        /* (non-Javadoc)
         * @see org.esorm.EntityManager#getLocation()
         */
        public String getLocation()
        {
            return location;
        }

        /* (non-Javadoc)
         * @see org.esorm.EntityManager#makeBuilder()
         */
        public <R> EntityBuilder<R> makeBuilder()
        {
            return new PojoEntityBuilder<R>();
        }
        
        public class PojoEntityBuilder<R> implements EntityBuilder<R> {
            private R bean;

            /* (non-Javadoc)
             * @see org.esorm.EntityBuilder#prepare()
             */
            public void prepare()
            {
                try
                {
                    bean = (R) clazz.newInstance();
                }
                catch (RuntimeException e) {
                    throw e;
                }
                catch (Exception e)
                {
                    throw new RegisteredExceptionWrapper(e);
                }
                
            }

            /* (non-Javadoc)
             * @see org.esorm.EntityBuilder#setProperty(java.lang.String, java.lang.Object)
             */
            public void setProperty(String name, Object value)
            {
                Property property = properties.get(name);
                if (property == null)
                    throw new IllegalArgumentException("Class " + clazz.getName() + " do not have property " + name);
                property.set(bean, value);
                
            }

            /* (non-Javadoc)
             * @see org.esorm.EntityBuilder#build()
             */
            public R build()
            {
                R rc = bean;
                bean = null;
                return rc;
            }
            
        }

        
    }

}
