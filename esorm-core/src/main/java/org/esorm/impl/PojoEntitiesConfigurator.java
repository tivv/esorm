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

import org.esorm.EntitiesConfigurator;
import org.esorm.LazyManagedEntityConfiguration;
import org.esorm.entity.db.Table;
import org.esorm.impl.PojoEntitiesManager.PojoEntityManager;
import org.esorm.impl.db.ColumnImpl;
import org.esorm.impl.db.TableImpl;
import org.esorm.utils.PojoUtils;
import org.jcloudlet.bean.Property;
import org.jcloudlet.bean.criteria.PropertySelector;
import org.jcloudlet.bean.impl.PropertySelectorImpl;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public class PojoEntitiesConfigurator
implements EntitiesConfigurator
{
    private String idPropertyName;
    
    public PojoEntitiesConfigurator(String idPropertyName)
    {
        this.idPropertyName = idPropertyName;
    }
    
    public PojoEntitiesConfigurator() {
        this("id");
    }

    /* (non-Javadoc)
     * @see org.esorm.EntitiesConfigurator#resolveConfiguration(java.lang.String, java.lang.Iterable)
     */
    public LazyManagedEntityConfiguration resolveConfiguration(String name,
                                                               Iterable<String> configurationLocations)
    {
        Class<?> entityClass = PojoUtils.resolveClass(name, configurationLocations);
        if (entityClass == null)
            return null;
        LazyManagedEntityConfigurationImpl rc = new LazyManagedEntityConfigurationImpl(name);
        Table table = new TableImpl(name);
        for (Property property: new PropertySelectorImpl(entityClass).select()) {
            final EntityPropertyImpl entityProperty = new EntityPropertyImpl(property.name(), new ColumnImpl(table, property.name()) );
            rc.getProperties().add(entityProperty); 
            if (idPropertyName.equals(property.name())) {
                rc.addIdProperty(entityProperty);
            }
        }
        return rc;
    }



    
}
