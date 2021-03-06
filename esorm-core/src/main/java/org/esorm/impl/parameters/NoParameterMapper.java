/**
 *
 * Copyright 2010-2011 Vitalii Tymchyshyn
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
package org.esorm.impl.parameters;

import org.esorm.parameters.ParameterMapper;
import org.esorm.parameters.ParameterSetter;

/**
 * @author Vitalii Tymchyshyn
 */
public class NoParameterMapper implements ParameterMapper {
    public static final NoParameterMapper INSTANCE = new NoParameterMapper();

    public Object process(Object multiCallState, ParameterSetter setter, Object... inputValues) {
        if (inputValues.length != 0)
            throw new IllegalStateException("The query takes no parameters and " + inputValues.length + " are provided");
        return null;
    }
}
