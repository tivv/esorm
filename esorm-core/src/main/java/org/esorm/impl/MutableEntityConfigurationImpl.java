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

import org.esorm.*;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public class MutableEntityConfigurationImpl implements MutableEntityConfiguration
{
    private final EntityConfiguration parent;
    private EntityManager manager;
    private final String name;
    private String location;

    public MutableEntityConfigurationImpl(String name, EntityConfiguration parent)
    {
        this.parent = parent;
        this.name = name;
        this.location = parent.getLocation();
    }
    
    public EntityManager getManager()
    {
        return manager == null ? parent.getManager() : manager;
    }

    public void setManager(EntityManager manager)
    {
        this.manager = manager;
    }

    public String getName()
    {
        return name;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

}
