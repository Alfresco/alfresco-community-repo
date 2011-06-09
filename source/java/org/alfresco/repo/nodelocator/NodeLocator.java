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

package org.alfresco.repo.nodelocator;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A strategy for locating a {@link NodeRef} in the repository, given a source node and an arbitrary set of parameters.
 * 
 * @author Nick Smith
 * @since 4.0
 *
 */
public interface NodeLocator
{
    /**
     * Finds a {@link NodeRef} given a starting {@link NodeRef} and a
     * {@link Map} of parameters.
     * Returns <code>null</code> if the specified node could not be found.
     * 
     * @param sourceNode the starting point for locating a new node. The source node. Can be <code>null</code>.
     * @param params an arbitrary {@link Map} of parameters.Can be <code>null</code>.
     * @return the node to be found or <code>null</code>.
     */
    NodeRef getNode(NodeRef source, Map<String, Serializable> params);
    
    /**
     * A list containing the parmameter defintions for this {@link NodeLocator}.
     * 
     * @return  a list of parameter definitions
     */
    public List<ParameterDefinition> getParameterDefinitions();
}
