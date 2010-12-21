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
package org.esorm;

import org.esorm.entity.db.SelectExpression;
import org.esorm.entity.db.ValueExpression;

import java.util.List;
import java.util.Map;

/**
 * @author Vitalii Tymchyshyn
 */
public interface PreparedQuery extends Iterable {
    Map<ValueExpression, Integer> getResultMapping();

    /**
     * @return map from each level (defined by table) to list or columns that group multiple result set records into
     *         one resulting entity
     */
    Map<SelectExpression, List<ValueExpression>> getResultGrouping();

    List<PreparedQuery> getChainedQueries();

    EntityConfiguration getResultConfiguration();

    QueryIterator iterator();
}
