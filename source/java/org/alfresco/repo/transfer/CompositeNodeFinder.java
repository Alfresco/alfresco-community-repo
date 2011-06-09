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

package org.alfresco.repo.transfer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.NodeFinder;

/**
 * A {@link NodeFinder} that sums the results of multiple {@link NodeFinder}s.
 * @author Nick Smith
 * @since 4.0
 *
 */
public class CompositeNodeFinder extends AbstractNodeFinder
{
    private final Collection<NodeFinder> finders;

    public CompositeNodeFinder(NodeFinder... finders)
    {
        this.finders = Arrays.asList(finders);
    }
    
    public CompositeNodeFinder(Collection<NodeFinder> finders)
    {
        this.finders = finders;
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void init()
    {
        super.init();
        for (NodeFinder finder : finders)
        {
            if(finder instanceof AbstractNodeFinder)
            {
                AbstractNodeFinder nodeFinder = (AbstractNodeFinder) finder;
                nodeFinder.setServiceRegistry(serviceRegistry);
                nodeFinder.init();
            }
        }
    }
    
    /**
    * {@inheritDoc}
    */
    public Set<NodeRef> findFrom(NodeRef thisNode)
    {
        HashSet<NodeRef> results = new HashSet<NodeRef>();
        for (NodeFinder finder : finders)
        {
            Set<NodeRef> result = finder.findFrom(thisNode);
            if(result != null)
            {
                results.addAll(result);
            }
        }
        return results;
    }
}
