/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Generic bridge table support with optional reference counting to allow multiple membership for an object via several
 * relationships.
 * 
 * @author Andy
 */
public class BridgeTable<T>
{
    HashMap<T, HashMap<Integer, HashMap<T, Counter>>> descendants = new HashMap<T, HashMap<Integer, HashMap<T, Counter>>>();

    HashMap<T, HashMap<Integer, HashMap<T, Counter>>> ancestors = new HashMap<T, HashMap<Integer, HashMap<T, Counter>>>();

    ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public void addLink(T parent, T child)
    {
        readWriteLock.writeLock().lock();
        try
        {
            addDescendants(parent, child);
            addAncestors(parent, child);
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }

    }

    public void addLink(Pair<T, T> link)
    {
        addLink(link.getFirst(), link.getSecond());
    }

    public void addLinks(Collection<Pair<T, T>> links)
    {
        for (Pair<T, T> link : links)
        {
            addLink(link);
        }
    }

    public void removeLink(T parent, T child)
    {
        readWriteLock.writeLock().lock();
        try
        {
            removeDescendants(parent, child);
            removeAncestors(parent, child);
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }

    }

    public void removeLink(Pair<T, T> link)
    {
        removeLink(link.getFirst(), link.getSecond());
    }

    public void removeLinks(Collection<Pair<T, T>> links)
    {
        for (Pair<T, T> link : links)
        {
            removeLink(link);
        }
    }

    public HashSet<T> getDescendants(T node)
    {
        return getDescendants(node, 1, Integer.MAX_VALUE);
    }

    public HashSet<T> getDescendants(T node, int position)
    {
        return getDescendants(node, position, position);
    }

    public HashSet<T> getDescendants(T node, int start, int end)
    {
        HashSet<T> answer = new HashSet<T>();
        HashMap<Integer, HashMap<T, Counter>> found = descendants.get(node);
        if (found != null)
        {
            for (Integer key : found.keySet())
            {
                if ((key.intValue() >= start) && (key.intValue() <= end))
                {
                    HashMap<T, Counter> asd = found.get(key);
                    answer.addAll(asd.keySet());
                }
            }
        }
        return answer;
    }

    public HashSet<T> getAncestors(T node)
    {
        return getAncestors(node, 1, Integer.MAX_VALUE);
    }

    public HashSet<T> getAncestors(T node, int position)
    {
        return getAncestors(node, position, position);
    }

    public HashSet<T> getAncestors(T node, int start, int end)
    {
        HashSet<T> answer = new HashSet<T>();
        HashMap<Integer, HashMap<T, Counter>> found = ancestors.get(node);
        if (found != null)
        {
            for (Integer key : found.keySet())
            {
                if ((key.intValue() >= start) && (key.intValue() <= end))
                {
                    HashMap<T, Counter> asd = found.get(key);
                    answer.addAll(asd.keySet());
                }
            }
        }
        return answer;
    }

    /**
     * @param parent T
     * @param child T
     */
    private void addDescendants(T parent, T child)
    {
        HashMap<Integer, HashMap<T, Counter>> parentsDescendants = descendants.get(parent);
        if (parentsDescendants == null)
        {
            parentsDescendants = new HashMap<Integer, HashMap<T, Counter>>();
            descendants.put(parent, parentsDescendants);
        }

        HashMap<Integer, HashMap<T, Counter>> childDescendantsToAdd = descendants.get(child);

        // add all the childs children to the parents descendants

        add(childDescendantsToAdd, Integer.valueOf(0), parentsDescendants, child);

        // add childs descendants to all parents ancestors at the correct depth

        HashMap<Integer, HashMap<T, Counter>> ancestorsToFixUp = ancestors.get(parent);

        if (ancestorsToFixUp != null)
        {
            for (Integer ancestorPosition : ancestorsToFixUp.keySet())
            {
                HashMap<T, Counter> ancestorsToFixUpAtPosition = ancestorsToFixUp.get(ancestorPosition);
                for (T ancestorToFixUpAtPosition : ancestorsToFixUpAtPosition.keySet())
                {
                    HashMap<Integer, HashMap<T, Counter>> ancestorDescendants = descendants.get(ancestorToFixUpAtPosition);
                    add(childDescendantsToAdd, ancestorPosition, ancestorDescendants, child);
                }
            }
        }
    }

    /**
     * @param parent T
     * @param child T
     */
    private void removeDescendants(T parent, T child)
    {
        HashMap<Integer, HashMap<T, Counter>> parentsDescendants = descendants.get(parent);
        if (parentsDescendants == null)
        {
            return;
        }

        HashMap<Integer, HashMap<T, Counter>> childDescendantsToRemove = descendants.get(child);

        // add all the childs children to the parents descendants

        remove(childDescendantsToRemove, Integer.valueOf(0), parentsDescendants, child);

        // add childs descendants to all parents ancestors at the correct depth

        HashMap<Integer, HashMap<T, Counter>> ancestorsToFixUp = ancestors.get(parent);

        if (ancestorsToFixUp != null)
        {
            for (Integer ancestorPosition : ancestorsToFixUp.keySet())
            {
                HashMap<T, Counter> ancestorsToFixUpAtPosition = ancestorsToFixUp.get(ancestorPosition);
                for (T ancestorToFixUpAtPosition : ancestorsToFixUpAtPosition.keySet())
                {
                    HashMap<Integer, HashMap<T, Counter>> ancestorDescendants = descendants.get(ancestorToFixUpAtPosition);
                    remove(childDescendantsToRemove, ancestorPosition, ancestorDescendants, child);
                }
            }
        }
    }
    
    /**
     * @param parent T
     * @param child T
     */
    private void removeAncestors(T parent, T child)
    {
        HashMap<Integer, HashMap<T, Counter>> childsAncestors = ancestors.get(child);
        if (childsAncestors == null)
        {
            return;
        }

        HashMap<Integer, HashMap<T, Counter>> parentAncestorsToRemove = ancestors.get(parent);

        // add all the childs children to the parents descendants

        remove(parentAncestorsToRemove, Integer.valueOf(0), childsAncestors, parent);

        // add childs descendants to all parents ancestors at the correct depth

        HashMap<Integer, HashMap<T, Counter>> decendantsToFixUp = descendants.get(child);

        if (decendantsToFixUp != null)
        {
            for (Integer descendantPosition : decendantsToFixUp.keySet())
            {
                HashMap<T, Counter> decendantsToFixUpAtPosition = decendantsToFixUp.get(descendantPosition);
                for (T descendantToFixUpAtPosition : decendantsToFixUpAtPosition.keySet())
                {
                    HashMap<Integer, HashMap<T, Counter>> descendantAncestors = ancestors.get(descendantToFixUpAtPosition);
                    remove(parentAncestorsToRemove, descendantPosition, descendantAncestors, parent);
                }
            }
        }
    }

    /**
     * @param toAdd HashMap<Integer, HashMap<T, Counter>>
     * @param position Integer
     * @param target HashMap<Integer, HashMap<T, Counter>>
     * @param node T
     */
    private void add(HashMap<Integer, HashMap<T, Counter>> toAdd, Integer position, HashMap<Integer, HashMap<T, Counter>> target, T node)
    {
        // add direct child
        Integer directKey = Integer.valueOf(position.intValue() + 1);
        HashMap<T, Counter> direct = target.get(directKey);
        if (direct == null)
        {
            direct = new HashMap<T, Counter>();
            target.put(directKey, direct);
        }
        Counter counter = direct.get(node);
        if (counter == null)
        {
            counter = new Counter();
            direct.put(node, counter);
        }
        counter.increment();

        if (toAdd != null)
        {
            for (Integer depth : toAdd.keySet())
            {
                Integer newKey = Integer.valueOf(position.intValue() + depth.intValue() + 1);
                HashMap<T, Counter> toAddAtDepth = toAdd.get(depth);
                HashMap<T, Counter> targetAtDepthPlusOne = target.get(newKey);
                if (targetAtDepthPlusOne == null)
                {
                    targetAtDepthPlusOne = new HashMap<T, Counter>();
                    target.put(newKey, targetAtDepthPlusOne);
                }

                for (T key : toAddAtDepth.keySet())
                {
                    Counter counterToAdd = toAddAtDepth.get(key);
                    Counter counterToAddTo = targetAtDepthPlusOne.get(key);
                    if (counterToAddTo == null)
                    {
                        counterToAddTo = new Counter();
                        targetAtDepthPlusOne.put(key, counterToAddTo);
                    }
                    counterToAddTo.add(counterToAdd);
                }
            }
        }
    }

    /**
     * @param toRemove HashMap<Integer, HashMap<T, Counter>>
     * @param position Integer
     * @param target HashMap<Integer, HashMap<T, Counter>>
     * @param node T
     */
    private void remove(HashMap<Integer, HashMap<T, Counter>> toRemove, Integer position, HashMap<Integer, HashMap<T, Counter>> target, T node)
    {
        // remove direct child
        Integer directKey = Integer.valueOf(position.intValue() + 1);
        HashMap<T, Counter> direct = target.get(directKey);
        if (direct != null)
        {
            Counter counter = direct.get(node);
            if (counter != null)
            {
                counter.decrement();
                if (counter.getCount() == 0)
                {
                    direct.remove(node);
                }
            }

        }

        if (toRemove != null)
        {
            for (Integer depth : toRemove.keySet())
            {
                Integer newKey = Integer.valueOf(position.intValue() + depth.intValue() + 1);
                HashMap<T, Counter> toRemoveAtDepth = toRemove.get(depth);
                HashMap<T, Counter> targetAtDepthPlusOne = target.get(newKey);
                if (targetAtDepthPlusOne != null)
                {
                    for (T key : toRemoveAtDepth.keySet())
                    {
                        Counter counterToRemove = toRemoveAtDepth.get(key);
                        Counter counterToRemoveFrom = targetAtDepthPlusOne.get(key);
                        if (counterToRemoveFrom != null)
                        {
                            counterToRemoveFrom.remove(counterToRemove);
                            if (counterToRemoveFrom.getCount() == 0)
                            {
                                targetAtDepthPlusOne.remove(key);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @param parent T
     * @param child T
     */
    private void addAncestors(T parent, T child)
    {
        HashMap<Integer, HashMap<T, Counter>> childsAncestors = ancestors.get(child);
        if (childsAncestors == null)
        {
            childsAncestors = new HashMap<Integer, HashMap<T, Counter>>();
            ancestors.put(child, childsAncestors);
        }

        HashMap<Integer, HashMap<T, Counter>> parentAncestorsToAdd = ancestors.get(parent);

        // add all the childs children to the parents descendants

        add(parentAncestorsToAdd, Integer.valueOf(0), childsAncestors, parent);

        // add childs descendants to all parents ancestors at the correct depth

        HashMap<Integer, HashMap<T, Counter>> descenantsToFixUp = descendants.get(child);

        if (descenantsToFixUp != null)
        {
            for (Integer descendantPosition : descenantsToFixUp.keySet())
            {
                HashMap<T, Counter> descenantsToFixUpAtPosition = descenantsToFixUp.get(descendantPosition);
                for (T descenantToFixUpAtPosition : descenantsToFixUpAtPosition.keySet())
                {
                    HashMap<Integer, HashMap<T, Counter>> descendatAncestors = ancestors.get(descenantToFixUpAtPosition);
                    add(parentAncestorsToAdd, descendantPosition, descendatAncestors, parent);
                }
            }
        }
    }

    public int size()
    {
        return ancestors.size();
    }
    
    private static class Counter
    {
        int count = 0;

        void increment()
        {
            count++;
        }

        void decrement()
        {
            count--;
        }

        int getCount()
        {
            return count;
        }

        void add(Counter other)
        {
            count += other.count;
        }
        
        void remove(Counter other)
        {
            count -= other.count;
        }
    }

    /**
     * @return Set<T>
     */
    public Set<T> keySet()
    {
        return ancestors.keySet();
    }
}
