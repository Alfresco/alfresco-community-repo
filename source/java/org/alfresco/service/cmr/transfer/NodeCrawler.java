/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.service.cmr.transfer;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The NodeCrawler finds nodes related to an initial group of nodes for the 
 * transfer service.
 * <p>
 * During the crawl method the node finders find nodes related to the staring nodes 
 * and then the filters can exclude unwanted nodes.   For example you could use the finders 
 * to walk down a tree of nodes and exclude nodes of a certain type.  
 * 
 * @see org.alfresco.repo.transfer.StandardNodeCrawlerImpl
 *
 * @author Brian 
 * @since 3.3
 */
public interface NodeCrawler
{
    public abstract Set<NodeRef> crawl(NodeRef... nodes);

    public abstract Set<NodeRef> crawl(Set<NodeRef> startingNodes);

    public abstract void setNodeFinders(NodeFinder... finders);

    public abstract void setNodeFilters(NodeFilter... filters);

}