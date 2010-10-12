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
package org.esorm.utils;

import java.util.*;

/**
 * @author Vitalii Tymchyshyn
 *
 */
public class ReadOnlyReverseIterable<T>
implements Iterable<T>
{
    private final List<T> parent;
    
    public ReadOnlyReverseIterable(List<T> parent)
    {
        this.parent = parent;
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<T> iterator()
    {
        return new Iterator<T>()
            {
                private ListIterator<T> parentIterator = parent.listIterator(parent.size());
                
                public boolean hasNext()
                {
                    return parentIterator.hasPrevious();
                }

                public T next()
                {
                    return parentIterator.previous();
                }

                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
    }

}
