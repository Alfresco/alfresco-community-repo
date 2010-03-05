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

package org.alfresco.service.cmr.rendition;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This interface defines a strategy object used for finding a {@link NodeRef}.
 * 
 * @author Nick Smith
 */
public interface NodeLocator
{
    /**
     * Finds a {@link NodeRef} given a starting {@link NodeRef} and a
     * {@link Map} of parameters.
     * 
     * @param sourceNode the starting point for locating a new node.
     * @param params a {@link Map} of parameters.
     * @return the {@link NodeRef}.
     */
    NodeRef getNode(NodeRef sourceNode, Map<String, Serializable> params);
}
