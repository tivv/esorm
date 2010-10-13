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

import org.esorm.entity.EntityProperty;
import org.esorm.entity.db.ValueExpression;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public class EntityPropertyImpl
implements EntityProperty
{
    private final String name;
    private ValueExpression expression;
    private List<String> path;

    public EntityPropertyImpl(String name)
    {
        this.name = name;
    }

    public EntityPropertyImpl(String name, ValueExpression expression)
    {
        this.name = name;
        this.expression = expression;
    }

    public EntityPropertyImpl(String name, ValueExpression expression,
                              List<String> path)
    {
        this.name = name;
        this.expression = expression;
        this.path = path;
    }

    /* (non-Javadoc)
     * @see org.esorm.entity.EntityProperty#getPath()
     */
    public Iterable<String> getPath()
    {
        return path == null ? Collections.<String>emptyList() : path;
    }
    
    public void setPath(List<String> path)
    {
        this.path = path;
    }

    public EntityProperty path(List<String> path) {
        setPath(path);
        return this;
    }

    public EntityProperty path(String pathElement) {
        if (path == null)
            path = new ArrayList<String>();
        path.add(pathElement);
        return this;
    }

    /* (non-Javadoc)
     * @see org.esorm.entity.EntityProperty#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see org.esorm.entity.EntityProperty#getExpression()
     */
    public ValueExpression getExpression()
    {
        return expression;
    }

    public void setExpression(ValueExpression expression)
    {
        this.expression = expression;
    }

}
