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

import org.esorm.entity.EntityProperty;
import org.esorm.entity.db.Column;
import org.esorm.entity.db.SelectExpression;
import org.esorm.entity.db.ValueExpression;

import java.util.*;

/**
 * @author Vitalii Tymchyshyn
 */
public class Queries {
    public static ParsedQuery byId(final EntityConfiguration configuration) {
        final StringBuilder query = new StringBuilder();
        Map<SelectExpression, String> tablesInvolved = new HashMap<SelectExpression, String>();
        final Map<ValueExpression, Integer> resultColumns = new HashMap<ValueExpression, Integer>();
        query.append("select ");
        Iterable<EntityProperty> properties = configuration.getProperties();
        for (EntityProperty property : properties) {
            ValueExpression expression = property.getExpression();
            if (!resultColumns.containsKey(expression)) {
                int num = resultColumns.size() + 1;
                resultColumns.put(expression, num);
                for (SelectExpression table : expression.getTables()) {
                    if (!tablesInvolved.containsKey(table)) {
                        int tableNum = tablesInvolved.size() + 1;
                        tablesInvolved.put(table, "t" + tableNum);
                    }
                }
                if (num != 1)
                    query.append(',');
                expression.appendQuery(query, tablesInvolved);
            }
        }
        if (resultColumns.isEmpty())
            throw new IllegalArgumentException("Nothing to select for " + configuration.getName());
        //TODO - complex primary key by id / name
        query.append(" from ");
        Iterable<Column> firstTablePK = null;
        Map<SelectExpression, ? extends Iterable<Column>> primaryKeys = configuration.getIdColumns();
        for (Map.Entry<SelectExpression, String> e : tablesInvolved.entrySet()) {
            if (firstTablePK != null)
                query.append(" join ");
            e.getKey().appendQuery(query, e.getValue());
            Iterable<Column> primaryKey = primaryKeys.get(e.getKey());
            if (primaryKey == null)
                throw new IllegalStateException("Table " + e.getKey() + " does not have primary key specified");
            if (firstTablePK == null) {
                firstTablePK = primaryKey;
            } else {
                Iterator<Column> primaryKeyIterator = primaryKey.iterator();
                String toAppend = " on ";
                for (Column firstColumn : firstTablePK) {
                    //TODO add .hasNext check
                    Column secondColumn = primaryKeyIterator.next();
                    query.append(toAppend);
                    toAppend = " and ";
                    firstColumn.appendQuery(query, tablesInvolved);
                    query.append("=");
                    secondColumn.appendQuery(query, tablesInvolved);
                }
                //TODO add .hasNext check
            }
        }
        String toAppend = " where ";
        for (Column pkColumn : firstTablePK) {
            query.append(toAppend);
            toAppend = " and ";
            pkColumn.appendQuery(query, tablesInvolved);
            query.append("=?");
        }
        return new ParsedQuery() {
            public Type getType() {
                return Type.Fetch;
            }

            public EntityConfiguration getResultConfiguration() {
                return configuration;
            }

            public String getSQL() {
                return query.toString();
            }

            public Map<ValueExpression, Integer> getResultMapping() {
                return resultColumns;
            }

            public Map<String, Integer> getParameterIndexes() {
                return null;
            }

            public List<ParameterTransformer> getParameterTransformers() {
                return Collections.emptyList();
            }
        };
    }
}
