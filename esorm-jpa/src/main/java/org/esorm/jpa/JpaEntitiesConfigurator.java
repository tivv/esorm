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

import java.lang.reflect.Modifier;
import java.util.*;

import javax.persistence.*;

import org.esorm.EntitiesConfigurator;
import org.esorm.LazyManagedEntityConfiguration;
import org.esorm.entity.EntityProperty;
import org.esorm.entity.db.Column;
import org.esorm.entity.db.Table;
import org.esorm.impl.*;
import org.esorm.impl.db.ColumnImpl;
import org.esorm.impl.db.TableImpl;
import org.esorm.utils.*;
import org.esorm.utils.PojoUtils.NameFilter;
import org.jcloudlet.bean.Property;
import org.jcloudlet.bean.impl.PropertySelectorImpl;

import static org.esorm.utils.StringUtils.*;
/**
 * @author Vitalii Tymchyshyn
 *
 */
public class JpaEntitiesConfigurator
implements EntitiesConfigurator
{
    /**
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
        Table primaryTable = getPrimaryTable(entityClass);
        String primaryTableName = primaryTable.getName();
        Map<String, List<PropertyBuilder>> properties = new HashMap<String, List<PropertyBuilder>>();
        Map<String, PropertyBuilder> idProperties = fillProperties(entityClass,
            primaryTable, properties);
        
        for (PropertyBuilder idProperty: idProperties.values()) {
            rc.addIdColumn(idProperty.getColumn());
        }
        Map<String, Table> tables = new HashMap<String, Table>();
        tables.put(primaryTableName, primaryTable);
        addSecondaryTable(entityClass, tables, idProperties);
        addSecondaryTables(entityClass, tables, idProperties);
        return rc;
    }

    private Map<String, PropertyBuilder> fillProperties(Class<?> entityClass,
                                          Table primaryTable,
                                          Map<String, List<PropertyBuilder>> properties)
    {
        PropertyBuilder defaultIdProperty = null;
        Map<String, PropertyBuilder> idProperties = null;
        for (Property property: new PropertySelectorImpl(entityClass).ignore(Transient.class).select()) {
            if (property.field() != null && Modifier.isTransient(property.field().getModifiers()))
                continue;
            PropertyBuilder propertyBuilder = new PropertyBuilder(property, primaryTable);
            String propertyTable = propertyBuilder.getTableName();
            List<PropertyBuilder> tableProperties = properties.get(propertyTable);
            if (tableProperties == null) {
                tableProperties = new ArrayList<PropertyBuilder>();
                properties.put(propertyTable, tableProperties);
            }
            tableProperties.add(propertyBuilder);
            Id id = property.annotation(Id.class);
            if (id != null) {
                if (!primaryTable.getName().equals(propertyTable))
                    throw new IllegalStateException("@Id annotation on " + entityClass + " property " + property.name() 
                        + " assigned to table " + propertyTable + " different from primary table " + primaryTable.getName());
                if (idProperties == null) {
                    idProperties = new LinkedHashMap<String, PropertyBuilder>();
                }
                idProperties.put(propertyBuilder.getColumn().getName(), propertyBuilder);
            } else if (idProperties == null && "id".equals(property.name()) && primaryTable.getName().equals(propertyTable)) {
                defaultIdProperty = propertyBuilder;
            }
        }
        if (idProperties == null && defaultIdProperty != null)
            idProperties = Collections.singletonMap(defaultIdProperty.getColumn().getName(), defaultIdProperty);
        return idProperties;
    }

    private void addSecondaryTables(Class<?> entityClass, Map<String, Table> rc, Map<String, PropertyBuilder> idProperties)
    {
        SecondaryTables secondaryTables = entityClass.getAnnotation(SecondaryTables.class);
        if (secondaryTables != null) {
            for (SecondaryTable secondaryTable : secondaryTables.value()) {
                addSecondaryTable(rc, secondaryTable, idProperties);
            }
        }
    }

    private void addSecondaryTable(Class<?> entityClass, Map<String, Table> rc, Map<String, PropertyBuilder> idProperties)
    {
        SecondaryTable secondaryTable = entityClass.getAnnotation(SecondaryTable.class);
        if (secondaryTable != null) {
            addSecondaryTable(rc, secondaryTable, idProperties);
        }
    }

    private Table getPrimaryTable(Class<?> entityClass)
    {
        javax.persistence.Table primaryTable = 
            entityClass.getAnnotation(javax.persistence.Table.class);
        if (primaryTable != null) {
            final String tableName = notEmpty(primaryTable.name(), entityClass.getSimpleName());
            return new TableImpl(
                notEmpty(primaryTable.catalog()), notEmpty(primaryTable.schema()), tableName);
        } else {
            return new TableImpl(entityClass.getSimpleName());
        }
    }

    private void addSecondaryTable(Map<String, Table> rc,
                                   SecondaryTable secondaryTable, Map<String, PropertyBuilder> primaryIdColumns)
    {
        final TableImpl table = new TableImpl(
            notEmpty(secondaryTable.catalog()), notEmpty(secondaryTable.schema()), secondaryTable.name());
        rc.put(secondaryTable.name(), table);
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
            return notEmpty(entityAnn.name(), clazz.getSimpleName()).equals(name);
        }
    };
    
    private static class PropertyBuilder {
        private final Property property;
        private final String tableName;
        private final javax.persistence.Column columnAnn;
        private Table table;
        private ColumnImpl column;
        private EntityPropertyImpl entityProperty;
        
        private PropertyBuilder(Property property, Table primaryTable) {
            this.property = property;
            this.columnAnn = property.annotation(javax.persistence.Column.class);
            if (columnAnn != null) {
                tableName = notEmpty(columnAnn.table(), primaryTable.getName());
                if (primaryTable.getName().equals(tableName))
                    setTable(primaryTable);
            } else {
                tableName = primaryTable.getName();
                setTable(primaryTable);
            }
            
        }

        public Property getProperty()
        {
            return property;
        }

        public String getTableName()
        {
            return tableName;
        }

        public javax.persistence.Column getColumnAnn()
        {
            return columnAnn;
        }

        public Table getTable()
        {
            return table;
        }

        public void setTable(Table table)
        {
            assert this.table == null;
            this.table = table;
            column = new ColumnImpl(table, property.name());
            if (columnAnn != null) {
                if (columnAnn.name().length()>0)
                    column.setName(columnAnn.name());
                column.setInsertable(columnAnn.insertable());
                column.setUpdateable(columnAnn.updatable());
            }
            entityProperty = new EntityPropertyImpl(property.name(), column);
        }

        public Column getColumn()
        {
            assert column != null;
            return column;
        }

        public EntityPropertyImpl getEntityProperty()
        {
            assert entityProperty != null;
            return entityProperty;
        }

    }
}
