/**
 *
 * Copyright 2010-2015 Vitalii Tymchyshyn
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

import org.esorm.EntityBuilder;
import org.esorm.EntityConfiguration;

import java.sql.ResultSet;
import java.util.List;

/**
 * @author Vitalii Tymchyshyn
 */
public class SingleBeanPropertyFetcher<R> implements PropertyFetcher<R> {
    private final String propertyName;
    private final EntityConfiguration configuration;
    private final List<PropertyFetcher> propertyFetchers;

    public SingleBeanPropertyFetcher(String propertyName, EntityConfiguration configuration, List<PropertyFetcher> propertyFetchers) {
        this.propertyName = propertyName;
        this.configuration = configuration;
        this.propertyFetchers = propertyFetchers;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public R getPropertyValue(ResultSet resultSet) {
        EntityBuilder<R> entityBuilder = configuration.getManager().makeBuilder();
        entityBuilder.prepare();
        for (PropertyFetcher fetcher : propertyFetchers)
        {
            entityBuilder.setProperty(fetcher.getPropertyName(), fetcher.getPropertyValue(resultSet));
        }
        return entityBuilder.build();
    }
}
