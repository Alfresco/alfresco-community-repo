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
import java.util.List;
import java.util.ListIterator;

import org.alfresco.query.PermissionedResults;

/**
 * Wrapped list for permission checking (eg. used by canned queries) 
 * 
 * which can be used to indicate that:
 * 
 * - input results should be optionally permission checked up to a given maximum number of items (and then cutoff - ie. hasMoreItems = true)
 * - have permission been applied to output results and were the permission checks cut-off (ie. hasMoreItems = true) either due to max items or system-wide time limit
 * 
 * @author janv
 * @since 4.0
 */
@SuppressWarnings("hiding")
public class WrappedList<T> implements List<T>, PermissionedResults
{
    private List<T> wrapped;
    
    // input
    private int maxChecks = -1;
    
    // output
    private boolean permissionsApplied;
    private boolean hasMoreItems;
    
    public WrappedList(List<T> wrapped, int maxChecks)
    {
        this.wrapped = wrapped;
        this.maxChecks = maxChecks;
    }
    
    public WrappedList(List<T> wrapped, boolean permissionsApplied, boolean hasMoreItems)
    {
        this.wrapped = wrapped;
        this.permissionsApplied = permissionsApplied;
        this.hasMoreItems = hasMoreItems;
    }
    
    public int getMaxChecks()
    {
        return maxChecks;
    }
    
    public boolean hasMoreItems()
    {
        return hasMoreItems;
    }
    
    /* package */ void setHasMoreItems(boolean hasMoreItems)
    {
        this.hasMoreItems = hasMoreItems;
    }
    
    public boolean permissionsApplied()
    {
        return permissionsApplied;
    }
    
    /* package */ void setPermissionsApplied(boolean permissionsApplied)
    {
        this.permissionsApplied = permissionsApplied;
    }
    
    public List<T> getWrapped()
    {
        return wrapped;
    }
    
    @Override
    public Iterator<T> iterator()
    {
        return wrapped.iterator();
    }
    
    @Override
    public boolean add(T e)
    {
        return wrapped.add(e);
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
    
    @Override
    public void add(int index, T element)
    {
        wrapped.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        return wrapped.addAll(c);
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends T> c)
    {
        return wrapped.addAll(index, c);
    }
    
    @Override
    public T get(int index)
    {
        return wrapped.get(index);
    }
    
    @Override
    public int indexOf(Object o)
    {
        return wrapped.indexOf(o);
    }
    
    @Override
    public int lastIndexOf(Object o)
    {
        return wrapped.lastIndexOf(o);
    }
    
    @Override
    public ListIterator<T> listIterator()
    {
        return wrapped.listIterator();
    }
    
    @Override
    public ListIterator<T> listIterator(int index)
    {
        return wrapped.listIterator(index);
    }
    
    @Override
    public T remove(int index)
    {
        return wrapped.remove(index);
    }
    
    @Override
    public T set(int index, T element)
    {
        return wrapped.set(index, element);
    }
    
    @Override
    public List<T> subList(int fromIndex, int toIndex)
    {
        return wrapped.subList(fromIndex, toIndex);
    }
    
    @Override
    public <T> T[] toArray(T[] a)
    {
        return wrapped.toArray(a);
    }
}
