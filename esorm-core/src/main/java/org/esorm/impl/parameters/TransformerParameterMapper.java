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
package org.esorm.impl.parameters;

import org.esorm.parameters.ParameterMapper;
import org.esorm.parameters.ParameterSetter;
import org.esorm.parameters.ParameterTransformer;

/**
 * @author Vitalii Tymchyshyn
 */
public class TransformerParameterMapper implements ParameterMapper {
    private final int inputNumber;
    private final int outputNumber;
    private final ParameterTransformer transformer;

    public TransformerParameterMapper(ParameterTransformer transformer, int parameterNumber) {
        this(transformer, parameterNumber, parameterNumber);
    }

    public TransformerParameterMapper(ParameterTransformer transformer, int inputNumber, int outputNumber) {
        this.inputNumber = inputNumber;
        this.outputNumber = outputNumber;
        this.transformer = transformer;
    }

    @SuppressWarnings({"unchecked"})
    public Object process(Object multiCallState, ParameterSetter setter, Object... inputValues) {
        setter.setParameter(outputNumber, transformer.transform(inputValues[inputNumber]));
        return null;
    }
}
