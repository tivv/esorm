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

import java.lang.reflect.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.esorm.*;
import org.esorm.utils.PojoUtils;
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
        Class<?> entityClass = PojoUtils.resolveClass(newConfiguration.getName(), implementationLocations);
        return entityClass != null ? new PojoEntityManager(entityClass) : null;
    }

    public static class PojoEntityManager implements EntityManager {
        private final Class<?> clazz;
        private final Map<String, Property> properties;

        private PojoEntityManager(Class<?> clazz)
        {
            this.clazz = clazz;
            this.properties = new PropertySelectorImpl(clazz).asMap();
        }
        
        /* (non-Javadoc)
         * @see org.esorm.EntityManager#getLocation()
         */
        public String getLocation()
        {
            return clazz.getName();
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
                    if (clazz.isInterface()) {
                        bean = (R) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz}, new PojoInvocationHandler());
                    } else {
                        bean = (R) clazz.newInstance();
                    }
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
        public static class PojoInvocationHandler implements InvocationHandler {
            private Map<String, Object> propertyValues = new ConcurrentHashMap<String, Object>();

            /* (non-Javadoc)
             * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
             */
            public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
            {
                if (args != null && args.length > 1)
                    throw new UnsupportedOperationException("Method " + method + " is neither setter nor getter: too many arguments");
                String propertyName = method.getName();
                if (propertyName.startsWith("get") || propertyName.startsWith("set"))
                    propertyName = getPropertyName(propertyName, 3);
                else if (propertyName.startsWith("is"))
                    propertyName = getPropertyName(propertyName, 2);
                if (args != null && args.length == 1) {
                    propertyValues.put(propertyName, args[0]);
                    return proxy;
                }
                return propertyValues.get(propertyName);
            }

            private String getPropertyName(String methodName, int prefixLength)
            {
                if (methodName.length() == prefixLength || 
                    Character.isLowerCase(methodName.charAt(prefixLength)))
                    return methodName.substring(prefixLength);
                return Character.toLowerCase(methodName.charAt(prefixLength)) +
                    methodName.substring(prefixLength + 1);
            }
            
        }

        
    }

}
