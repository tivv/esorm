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
public class ParentedList<E>
extends AbstractList<E>
{
    private final List<E> parent;
    private final List<E> storage;

    public ParentedList(List<E> parent)
    {
        this(parent, new ArrayList<E>());
    }
    public ParentedList(List<E> parent, List<E> storage)
    {
        this.parent = parent;
        this.storage = storage;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#get(int)
     */
    public E get(int index)
    {
        return index < parent.size() ? parent.get(index) : 
            storage.get( index - parent.size() );
    }
    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    public int size()
    {
        return parent.size() + storage.size();
    }
    
    public void add(int index, E element)
    {
        if (index < parent.size())
            throw new IllegalArgumentException("You can't change parent list");
        storage.add(index - parent.size(), element);
    }
    public E remove(int index)
    {
        if (index < parent.size())
            throw new IllegalArgumentException("You can't change parent list");
        return super.remove(index - parent.size());
    }

}
