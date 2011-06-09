/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.security.permissions.impl.acegi;

import java.util.Collection;
import java.util.Iterator;

/**
 * Wrapped result set with max checks cutoff (ie. max items to permission check)
 * 
 * Note: cutoff will be set to true if results have been cutoff (due to max permission checks count and/or time)
 * 
 * @author janv
 * @since 4.0
 */
public class MaxChecksCollection implements Collection<Object>
{
    private Collection<Object> wrapped;
    private int maxChecks;
    private boolean cutoff;
    
    public MaxChecksCollection(Collection<Object> wrapped, int maxChecks)
    {
        this.wrapped = wrapped;
        this.maxChecks = maxChecks;
    }
    
    public int getMaxChecks()
    {
        return maxChecks;
    }
    
    public boolean isCutoff()
    {
        return cutoff;
    }
    
    public void setCutoff(boolean cutoff)
    {
        this.cutoff = cutoff;
    }
    
    
    public Collection<Object> getWrapped()
    {
        return wrapped;
    }
    
    @Override
    public Iterator<Object> iterator()
    {
        return wrapped.iterator();
    }
    
    @Override
    public boolean add(Object e)
    {
        return wrapped.add(e);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(Collection c)
    {
        return wrapped.addAll(c);
    }
    
    @Override
    public void clear()
    {
        wrapped.clear();
    }
    
    @Override
    public boolean contains(Object o)
    {
        return wrapped.contains(o);
    }
    
    @Override
    public boolean containsAll(Collection<?> c)
    {
        return wrapped.containsAll(c);
    }
    
    @Override
    public boolean isEmpty()
    {
        return wrapped.isEmpty();
    }
    
    @Override
    public boolean remove(Object o)
    {
        return wrapped.remove(o);
    }
    
    @Override
    public boolean removeAll(Collection<?> c)
    {
        return wrapped.removeAll(c);
    }
    
    @Override
    public boolean retainAll(Collection<?> c)
    {
        return wrapped.retainAll(c);
    }
    
    @Override
    public int size()
    {
        return wrapped.size();
    }
    
    @Override
    public Object[] toArray()
    {
        return wrapped.toArray();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object[] toArray(Object[] a)
    {
        return wrapped.toArray(a);
    }
}
