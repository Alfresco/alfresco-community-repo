/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.service.cmr.transfer;

import java.util.Set;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author brian
 * 
 * NodeFinders find nodes related to the current node.
 * The NodeCrawler will first initialise this filter by calling the 
 * setServiceRegistry and init methods.  Then the findFrom method will be called to find 
 * other nodes.
 * 
 * @see org.alfresco.repo.transfer.ChildAssociatedNodeFinder
 */
public interface NodeFinder
{

    /**
     * @param thisNode
     * @param serviceRegistry 
     * @return
     */
    Set<NodeRef> findFrom(NodeRef thisNode);

    /**
     * called by the node crawler to initialise this class.
     */
    void init();

    /**
     * 
     * @param serviceRegistry
     */
    void setServiceRegistry(ServiceRegistry serviceRegistry);
}
