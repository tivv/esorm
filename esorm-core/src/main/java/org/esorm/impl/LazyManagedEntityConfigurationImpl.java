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

import java.util.*;
import java.util.Map.Entry;

import org.esorm.EntityManager;
import org.esorm.LazyManagedEntityConfiguration;
import org.esorm.entity.EntityProperty;
import org.esorm.entity.db.Column;
import org.esorm.entity.db.SelectExpression;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public class LazyManagedEntityConfigurationImpl extends MutableEntityConfigurationImpl
implements LazyManagedEntityConfiguration
{
    private boolean fixed = false;

    /**
     * @param name
     */
    public LazyManagedEntityConfigurationImpl(String name)
    {
        super(name);
    }

    /* (non-Javadoc)
     * @see org.esorm.LazyManagedEntityConfiguration#setManager(org.esorm.EntityManager)
     */
    public void setManager(EntityManager manager)
    {
        checkNotFixed();
        setProperties(Collections.unmodifiableList(getProperties()));
        for (Entry<SelectExpression, List<Column>> e :getPrimaryKeys().entrySet()) {
            e.setValue(Collections.unmodifiableList(e.getValue()));
        }
        setPrimaryKeys(Collections.unmodifiableMap(getPrimaryKeys()));
        super.setManager(manager);
        fixed = true;

    }

    /**
     * 
     */
    private void checkNotFixed()
    {
        if (fixed)
            throw new IllegalStateException("Already fixed");
        
    }

    public void setLocation(String location)
    {
        checkNotFixed();
        super.setLocation(location);
    }

    public void setProperties(List<EntityProperty> properties)
    {
        checkNotFixed();
        super.setProperties(properties);
    }

    protected void setPrimaryKeys(Map<SelectExpression, List<Column>> primaryKeys)
    {
        checkNotFixed();
        super.setPrimaryKeys(primaryKeys);
    }

}
