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
package org.esorm.impl.db;

import org.esorm.RegisteredExceptionWrapper;
import org.esorm.entity.db.Table;

import java.io.IOException;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public class TableImpl
implements Table
{
    private final String catalog,schema, name;

    public TableImpl(String name)
    {
        this(null, name);
    }
    
    public TableImpl(String schema, String name) {
        this(null, schema, name);
    }
    
    public TableImpl(String catalog, String schema, String name)
    {
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.esorm.entity.db.SelectExpression#appendQuery(java.lang.Appendable, java.lang.String)
     */
    public void appendQuery(Appendable appendTo, String alias)
    {
        try
        {
            if (catalog != null) {
                appendTo.append(catalog);
                appendTo.append('.');
            }
            if (schema != null) {
                appendTo.append(schema);
                appendTo.append('.');
            }
            appendTo.append(name);
            appendTo.append(" as ");
            appendTo.append(alias);
        }
        catch (IOException e)
        {
            throw new RegisteredExceptionWrapper(e);
        }
    }

    public String getSchema()
    {
        return schema;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString() {
        return "TableImpl{" + (catalog != null ? catalog + '.' : "") +
                (schema != null ? schema + '.' : "") + name + "}";
    }
}
