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

import org.esorm.ComplexProperty;
import org.esorm.EntitiesConfigurator;
import org.esorm.LazyManagedEntityConfiguration;
import org.esorm.entity.db.Column;
import org.esorm.entity.db.Table;
import org.esorm.impl.EntityPropertyImpl;
import org.esorm.impl.LazyManagedEntityConfigurationImpl;
import org.esorm.impl.PlainComplexPropertyImpl;
import org.esorm.impl.db.ColumnImpl;
import org.esorm.impl.db.TableImpl;
import org.esorm.utils.PojoUtils;
import org.esorm.utils.PojoUtils.NameFilter;
import org.jcloudlet.bean.Property;
import org.jcloudlet.bean.impl.PropertySelectorImpl;

import javax.persistence.*;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.esorm.utils.StringUtils.notEmpty;

/**
 * @author Vitalii Tymchyshyn
 */
public class JpaEntitiesConfigurator
        implements EntitiesConfigurator {
    /**
     */
    public LazyManagedEntityConfiguration resolveConfiguration(String name,
                                                               Iterable<String> configurationLocations) {
        return resolveConfiguration(name, configurationLocations, false);
    }

    /* (non-Javadoc)
     * @see org.esorm.EntitiesConfigurator#resolveConfiguration(java.lang.String, java.lang.Iterable)
     */

    public LazyManagedEntityConfiguration resolveConfiguration(String name,
                                                               Iterable<String> configurationLocations, boolean locationOverride) {
        Class<?> entityClass = PojoUtils.resolveClass(name, configurationLocations, locationOverride, NAME_FILTER);
        if (entityClass == null)
            return null;
        LazyManagedEntityConfigurationImpl rc = new LazyManagedEntityConfigurationImpl(name);
        rc.setLocation(entityClass.getName());
        Table primaryTable = getPrimaryTable(entityClass);
        String primaryTableName = primaryTable.getName();
        Map<String, List<SimplePropertyBuilder>> simpleProperties = new HashMap<String, List<SimplePropertyBuilder>>();
        Map<String, SimplePropertyBuilder> idProperties = fillProperties(entityClass,
                primaryTable, simpleProperties, rc.getComplexProperties());

        for (SimplePropertyBuilder idProperty : idProperties.values()) {
            rc.addIdColumn(idProperty.getColumn());
        }
        Map<String, Table> tables = new HashMap<String, Table>();
        tables.put(primaryTableName, primaryTable);
        addSecondaryTable(entityClass, tables, idProperties);
        addSecondaryTables(entityClass, tables, idProperties);
        for (List<SimplePropertyBuilder> e : simpleProperties.values()) {
            for (SimplePropertyBuilder property : e) {
                rc.getProperties().add(property.getEntityProperty());
            }
        }
        return rc;
    }

    private Map<String, SimplePropertyBuilder> fillProperties(Class<?> entityClass,
                                                              Table primaryTable,
                                                              Map<String, List<SimplePropertyBuilder>> simpleProperties,
                                                              Map<String, ComplexProperty> complexProperties) {
        SimplePropertyBuilder defaultIdProperty = null;
        Map<String, SimplePropertyBuilder> idProperties = null;
        List<Property> complexPropertiesList = null;
        for (Property property : new PropertySelectorImpl(entityClass).ignore(Transient.class).select()) {
            if (property.field() != null && Modifier.isTransient(property.field().getModifiers()))
                continue;
            if (PojoUtils.isSimpleClass(property.type())) {
                SimplePropertyBuilder simplePropertyBuilder = new SimplePropertyBuilder(property, primaryTable);
                String propertyTable = simplePropertyBuilder.getTableName();
                List<SimplePropertyBuilder> tableProperties = simpleProperties.get(propertyTable);
                if (tableProperties == null) {
                    tableProperties = new ArrayList<SimplePropertyBuilder>();
                    simpleProperties.put(propertyTable, tableProperties);
                }
                tableProperties.add(simplePropertyBuilder);
                Id id = property.annotation(Id.class);
                if (id != null) {
                    if (!primaryTable.getName().equals(propertyTable))
                        throw new IllegalStateException("@Id annotation on " + entityClass + " property " + property.name()
                                + " assigned to table " + propertyTable + " different from primary table " + primaryTable.getName());
                    if (idProperties == null) {
                        idProperties = new LinkedHashMap<String, SimplePropertyBuilder>();
                    }
                    idProperties.put(simplePropertyBuilder.getColumn().getName(), simplePropertyBuilder);
                } else if (idProperties == null && "id".equals(property.name()) && primaryTable.getName().equals(propertyTable)) {
                    defaultIdProperty = simplePropertyBuilder;
                }
            } else {
                if (complexPropertiesList == null)
                {
                    complexPropertiesList = new ArrayList<Property>();
                }
                complexPropertiesList.add(property);
            }
        }
        if (idProperties == null && defaultIdProperty != null)
            idProperties = Collections.singletonMap(defaultIdProperty.getColumn().getName(), defaultIdProperty);
        if (complexPropertiesList != null)
        {
            for (Property property : complexPropertiesList)
            {
                JoinColumn joinColumn = property.annotation(JoinColumn.class);
                List<Column> joinToColumns;
                if (joinColumn != null && !"".equals(joinColumn.name()))
                {
                    joinToColumns = Collections.<Column>singletonList(new ColumnImpl(primaryTable, joinColumn.name()));
                } else
                {
                    if (idProperties == null)
                    {
                        throw new IllegalStateException("Can't create a join for " + property + " without an explicit @JoinColumn or a primary key");
                    }
                    joinToColumns = new ArrayList<Column>(idProperties.size());
                    for (SimplePropertyBuilder idProperty : idProperties.values())
                    {
                        joinToColumns.add(idProperty.getColumn());
                    }
                }
                complexProperties.put(property.name(), new PlainComplexPropertyImpl(property.type(), null, joinToColumns));
            }
        }
        return idProperties;
    }

    private void addSecondaryTables(Class<?> entityClass, Map<String, Table> rc, Map<String, SimplePropertyBuilder> idProperties) {
        SecondaryTables secondaryTables = entityClass.getAnnotation(SecondaryTables.class);
        if (secondaryTables != null) {
            for (SecondaryTable secondaryTable : secondaryTables.value()) {
                addSecondaryTable(rc, secondaryTable, idProperties);
            }
        }
    }

    private void addSecondaryTable(Class<?> entityClass, Map<String, Table> rc, Map<String, SimplePropertyBuilder> idProperties) {
        SecondaryTable secondaryTable = entityClass.getAnnotation(SecondaryTable.class);
        if (secondaryTable != null) {
            addSecondaryTable(rc, secondaryTable, idProperties);
        }
    }

    private Table getPrimaryTable(Class<?> entityClass) {
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
                                   SecondaryTable secondaryTable, Map<String, SimplePropertyBuilder> primaryIdColumns) {
        final TableImpl table = new TableImpl(
                notEmpty(secondaryTable.catalog()), notEmpty(secondaryTable.schema()), secondaryTable.name());
        rc.put(secondaryTable.name(), table);
    }

    private static NameFilter NAME_FILTER = new NameFilter() {

        public boolean accept(Class<?> clazz, String name,
                              boolean locationOverride) {
            final Entity entityAnn = clazz.getAnnotation(Entity.class);
            if (entityAnn == null)
                return false;
            if (locationOverride)
                return true;
            return notEmpty(entityAnn.name(), clazz.getSimpleName()).equals(name);
        }
    };

    private static class SimplePropertyBuilder {
        private final Property property;
        private final String tableName;
        private final javax.persistence.Column columnAnn;
        private Table table;
        private ColumnImpl column;
        private EntityPropertyImpl entityProperty;

        private SimplePropertyBuilder(Property property, Table primaryTable) {
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

        public Property getProperty() {
            return property;
        }

        public String getTableName() {
            return tableName;
        }

        public javax.persistence.Column getColumnAnn() {
            return columnAnn;
        }

        public Table getTable() {
            return table;
        }

        public void setTable(Table table) {
            assert this.table == null;
            this.table = table;
            column = new ColumnImpl(table, property.name());
            if (columnAnn != null) {
                if (columnAnn.name().length() > 0)
                    column.setName(columnAnn.name());
                column.setInsertable(columnAnn.insertable());
                column.setUpdateable(columnAnn.updatable());
            }
            entityProperty = new EntityPropertyImpl(property.name(), column);
        }

        public Column getColumn() {
            assert column != null;
            return column;
        }

        public EntityPropertyImpl getEntityProperty() {
            assert entityProperty != null;
            return entityProperty;
        }

    }
}
