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
package org.esorm.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.SecondaryTable;

import org.esorm.EntitiesConfigurator;
import org.esorm.LazyManagedEntityConfiguration;
import org.esorm.entity.EntityProperty;
import org.esorm.entity.db.Table;
import org.esorm.impl.*;
import org.esorm.impl.db.ColumnImpl;
import org.esorm.impl.db.TableImpl;
import org.esorm.utils.PojoUtils;
import org.esorm.utils.PojoUtils.NameFilter;
import org.jcloudlet.bean.Property;
import org.jcloudlet.bean.impl.PropertySelectorImpl;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public class JpaEntitiesConfigurator
implements EntitiesConfigurator
{
    /* (non-Javadoc)
     * @see org.esorm.EntitiesConfigurator#resolveConfiguration(java.lang.String, java.lang.Iterable)
     */
    /**
     * @deprecated Use {@link #resolveConfiguration(String,Iterable<String>,boolean)} instead
     */
    public LazyManagedEntityConfiguration resolveConfiguration(String name,
                                                               Iterable<String> configurationLocations)
    {
        return resolveConfiguration(name, configurationLocations, false);
    }

    /* (non-Javadoc)
     * @see org.esorm.EntitiesConfigurator#resolveConfiguration(java.lang.String, java.lang.Iterable)
     */
    public LazyManagedEntityConfiguration resolveConfiguration(String name,
                                                               Iterable<String> configurationLocations, boolean locationOverride)
    {
        Class<?> entityClass = PojoUtils.resolveClass(name, configurationLocations, locationOverride, NAME_FILTER);
        if (entityClass == null)
            return null;
        LazyManagedEntityConfigurationImpl rc = new LazyManagedEntityConfigurationImpl(name);
        rc.setLocation(entityClass.getName());
        Map<String, Table> tables = getTables(entityClass);
        EntityProperty defaultIdProperty = null;
        for (Property property: new PropertySelectorImpl(entityClass).select()) {
            final EntityPropertyImpl entityProperty = new EntityPropertyImpl(property.name(), new ColumnImpl(table, property.name()) );
            rc.getProperties().add(entityProperty); 
            if ("id".equals(property.name())) {
                defaultIdProperty = entityProperty;
            }
        }
        if (rc.getIdColumns().isEmpty() && defaultIdProperty != null)
            rc.addIdProperty(defaultIdProperty);
        return rc;
    }

    private Map<String, Table> getTables(Class<?> entityClass)
    {
        Map<String, Table> rc = new HashMap<String, Table>();
        javax.persistence.Table primaryTable = 
            entityClass.getAnnotation(javax.persistence.Table.class);
        if (primaryTable != null) {
            
        } else {
            rc.put(entityClass.getSimpleName(), new TableImpl(entityClass.getSimpleName()));
        }
        SecondaryTable secondaryTable = entityClass.getAnnotation(SecondaryTable.class);
        if (secondaryTable != null) {
            //TODO            
        }
            
        return rc;
    }

    private static NameFilter NAME_FILTER = new NameFilter()
    {
        
        public boolean accept(Class<?> clazz, String name,
                              boolean locationOverride)
        {
            final Entity entityAnn = clazz.getAnnotation(Entity.class);
            if (entityAnn == null)
                return false;
            if (locationOverride)
                return true;
            if ("".equals(entityAnn.name()))
                return clazz.getSimpleName().equals(name);
            return entityAnn.name().equals(name);
        }
    };
}
