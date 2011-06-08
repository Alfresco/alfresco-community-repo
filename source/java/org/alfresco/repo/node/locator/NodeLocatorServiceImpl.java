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

package org.alfresco.repo.node.locator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeLocator;
import org.alfresco.service.cmr.repository.NodeLocatorService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class NodeLocatorServiceImpl implements NodeLocatorService
{
    private final Map<String, NodeLocator> locators = new HashMap<String, NodeLocator>();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NodeRef getNode(String locatorName, NodeRef source, Map<String, Serializable> params)
    {
        NodeLocator locator = locators.get(locatorName);
        if(locator == null)
        {
            String msg = "No NodeLocator is registered with name " +locatorName;
            throw new IllegalArgumentException(msg);
        }
        return locator.getNode(source, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(String locatorName, NodeLocator locator)
    {
        ParameterCheck.mandatory("locatorName", locatorName);
        ParameterCheck.mandatory("locator", locator);
        if(locators.containsKey(locatorName))
        {
            String msg = "Locator with name: " +locatorName + " is already registered!";
            throw new IllegalArgumentException(msg);
        }
        locators.put(locatorName, locator);
    }

}
