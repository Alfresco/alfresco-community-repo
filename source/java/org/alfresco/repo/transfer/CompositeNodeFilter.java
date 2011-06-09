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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.NodeFilter;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class CompositeNodeFilter extends AbstractNodeFilter
{
    private final Collection<NodeFilter> filters;

    public CompositeNodeFilter(NodeFilter... filters)
    {
        this.filters = Arrays.asList(filters);
    }
    
    public CompositeNodeFilter(Collection<NodeFilter> filters)
    {
        this.filters = filters;
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void init()
    {
        super.init();
        for (NodeFilter filter : filters)
        {
            if(filter instanceof AbstractNodeFilter)
            {
                AbstractNodeFilter nodeFilter = (AbstractNodeFilter) filter;
                nodeFilter.setServiceRegistry(serviceRegistry);
                nodeFilter.init();
            }
        }
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public boolean accept(NodeRef thisNode)
    {
        for (NodeFilter filter : filters)
        {
            if(filter.accept(thisNode)==false)
            {
                return false;
            }
        }
        return true;
    }
}
