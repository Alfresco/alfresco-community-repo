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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the {@link NodeLocatorService} which is responsible for locating a 
 * {@link NodeRef} using a named lookup strategy.
 * 
 * @author Nick Smith
 * @since 4.0
 */
public class NodeLocatorServiceImpl implements NodeLocatorService
{
    /** Logger */
    private static Log logger = LogFactory.getLog(NodeLocatorServiceImpl.class);
    
    private final Map<String, NodeLocator> locators = new HashMap<String, NodeLocator>();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NodeRef getNode(String locatorName, NodeRef source, Map<String, Serializable> params)
    {
        NodeLocator locator = locators.get(locatorName);
        
        if (locator == null)
        {
            String msg = "No NodeLocator is registered with name: " + locatorName;
            throw new IllegalArgumentException(msg);
        }
        
        NodeRef node = locator.getNode(source, params);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Node locator named '" + locatorName + "' found node: " + node);
        }
        
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(String locatorName, NodeLocator locator)
    {
        ParameterCheck.mandatory("locatorName", locatorName);
        ParameterCheck.mandatory("locator", locator);
        
        if (locators.containsKey(locatorName))
        {
            String msg = "Locator with name '" + locatorName + "' is already registered!";
            throw new IllegalArgumentException(msg);
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Registered node locator: " + locatorName);
        }
        
        locators.put(locatorName, locator);
    }

}
