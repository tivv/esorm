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
package org.esorm.impl.jdbc.builder;

import org.esorm.entity.db.FromExpression;
import org.esorm.entity.db.ValueExpression;
import org.esorm.impl.parameters.MultiParameterMapper;
import org.esorm.impl.parameters.NoParameterMapper;
import org.esorm.impl.parameters.TransformerParameterMapper;
import org.esorm.parameters.ParameterMapper;
import org.esorm.parameters.ParameterTransformer;

import java.util.*;

/**
 * @author Vitalii Tymchyshyn
 */
@SuppressWarnings({"UnusedDeclaration"})
class BuildState implements Appendable {
    private int parametersWithoutMapper = 0;
    private final StringBuilder stringBuilder = new StringBuilder();
    private List<ParameterMapper> mappers = new ArrayList<ParameterMapper>();
    Map<FromExpression, TableSelectData> tablesInvolved = new LinkedHashMap<FromExpression, TableSelectData>();
    Map<ValueExpression, Integer> resultColumns = new HashMap<ValueExpression, Integer>();

    public StringBuilder getStringBuilder() {
        return stringBuilder;
    }

    public Map<FromExpression, TableSelectData> getTablesInvolved() {
        return tablesInvolved;
    }

    public Map<ValueExpression, Integer> getResultColumns() {
        return resultColumns;
    }

    public ParameterMapper getParameterMapper() {
        switch (mappers.size())
        {
            case 0:
                return NoParameterMapper.INSTANCE;
            case 1:
                return mappers.get(0);
            default:
                return new MultiParameterMapper(mappers.toArray(new ParameterMapper[mappers.size()]));
        }
    }

    public BuildState appendParameter() {
        stringBuilder.append('?');
        parametersWithoutMapper++;
        return this;
    }

    public BuildState appendParameter(ParameterMapper mapper) {
        mappers.add(mapper);
        stringBuilder.append('?');
        return this;
    }

    public BuildState appendParameter(ParameterTransformer transformer) {
        return appendParameter(new TransformerParameterMapper(transformer, getNextParameterNumber()));
    }

    public int getNextParameterNumber() {
        return mappers.size() + parametersWithoutMapper;
    }

    public BuildState append(Object obj) {
        stringBuilder.append(obj);
        return this;
    }

    public BuildState append(String str) {
        stringBuilder.append(str);
        return this;
    }

    public BuildState append(StringBuffer sb) {
        stringBuilder.append(sb);
        return this;
    }

    public BuildState append(CharSequence s) {
        stringBuilder.append(s);
        return this;
    }

    public BuildState append(CharSequence s, int start, int end) {
        stringBuilder.append(s, start, end);
        return this;
    }

    public BuildState append(char[] str) {
        stringBuilder.append(str);
        return this;
    }

    public BuildState append(char[] str, int offset, int len) {
        stringBuilder.append(str, offset, len);
        return this;
    }

    public BuildState append(boolean b) {
        stringBuilder.append(b);
        return this;
    }

    public BuildState append(char c) {
        stringBuilder.append(c);
        return this;
    }

    public BuildState append(int i) {
        stringBuilder.append(i);
        return this;
    }

    public BuildState append(long lng) {
        stringBuilder.append(lng);
        return this;
    }

    public BuildState append(float f) {
        stringBuilder.append(f);
        return this;
    }

    public BuildState append(double d) {
        stringBuilder.append(d);
        return this;
    }
}
