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

/**
 * @author Vitalii Tymchyshyn
 */
public class MultiParameterMapper implements ParameterMapper<MultiParameterMapper.StateSelector> {
    private final ParameterMapper[] children;

    public MultiParameterMapper(ParameterMapper... children) {
        this.children = children;
    }

    public StateSelector process(StateSelector multiCallState, ParameterSetter setter, Object... inputValues) {
        StateSelector rc = null;
        for (int i = 0, childrenLength = children.length; i < childrenLength; i++) {
            Object state = children[i].process(multiCallState == null ? null : multiCallState.getChildState(i),
                    setter, inputValues);
            if (state != null) {
                if (rc != null)
                    throw new IllegalStateException("Two parameter mappers require multiple calls");
                rc = new StateSelector(i, state);
            }
        }
        return rc;
    }

    public static class StateSelector {
        private final int childWithState;
        private final Object childState;

        private StateSelector(int childWithState, Object childState) {
            this.childWithState = childWithState;
            this.childState = childState;
        }

        public int getChildWithState() {
            return childWithState;
        }

        public Object getChildState() {
            return childState;
        }

        private Object getChildState(int i) {
            return i == getChildWithState() ? getChildState() : null;
        }
    }
}
