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

import org.esorm.ComplexProperty;
import org.esorm.EntityConfiguration;
import org.esorm.EntityManager;
import org.esorm.MutableEntityConfiguration;
import org.esorm.entity.EntityProperty;
import org.esorm.entity.db.Column;
import org.esorm.entity.db.SelectExpression;
import org.esorm.utils.IterableUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Vitalii Tymchyshyn
 */
public class MutableEntityConfigurationImpl implements MutableEntityConfiguration {
    private final String name;
    private EntityManager manager;
    private String location;
    private List<EntityProperty> properties;
    private Map<SelectExpression, List<Column>> idColumns;
    private Map<String, ComplexProperty> complexProperties;

    public MutableEntityConfigurationImpl(String name) {
        this.name = name;
        this.idColumns = new HashMap<SelectExpression, List<Column>>();
        this.properties = new ArrayList<EntityProperty>();
        this.complexProperties = new HashMap<String, ComplexProperty>();
    }

    public MutableEntityConfigurationImpl(String name, EntityConfiguration parent) {
        this.name = name;
        this.manager = parent.getManager();
        this.location = parent.getLocation();
        properties = IterableUtils.copyToList(parent.getProperties());
        complexProperties = new HashMap<String, ComplexProperty>(parent.getComplexProperties());
        idColumns = new HashMap<SelectExpression, List<Column>>();
        for (Entry<SelectExpression, ? extends Iterable<Column>> e : parent.getIdColumns().entrySet()) {
            idColumns.put(e.getKey(), IterableUtils.copyToList(e.getValue()));
        }

    }

    public EntityManager getManager() {
        return manager;
    }

    public void setManager(EntityManager manager) {
        this.manager = manager;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public List<EntityProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<EntityProperty> properties) {
        this.properties = properties;
    }

    /* (non-Javadoc)
     * @see org.esorm.EntityConfiguration#getPrimaryKeys()
     */

    public Map<SelectExpression, List<Column>> getIdColumns() {
        return idColumns;
    }

    public Map<String, ComplexProperty> getComplexProperties() {
        return complexProperties;
    }

    protected void setIdColumns(Map<SelectExpression, List<Column>> idColumns) {
        this.idColumns = idColumns;
    }

    public MutableEntityConfiguration addIdProperty(EntityProperty property) {
        properties.add(property);
        Column column = (Column) property.getExpression();
        return addIdColumn(column);
    }

    public MutableEntityConfiguration addIdColumn(Column column) {
        final SelectExpression table = column.getTable();
        List<Column> expressionList = idColumns.get(table);
        if (expressionList == null) {
            expressionList = new ArrayList<Column>();
            idColumns.put(table, expressionList);
        }
        expressionList.add(column);
        return this;
    }

}
