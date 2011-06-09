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

import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.action.ParameterDefinition;

/**
 * Base class for all {@link NodeLocator} implementations. 
 * <p>Extending this class with automatically register the node locator with the NodeLocatorService.</p>
 * 
 * @author Nick Smith
 * @since 4.0
 */
public abstract class AbstractNodeLocator implements NodeLocator
{
    public void setNodeLocatorService(NodeLocatorService nodeLocatorService)
    {
        nodeLocatorService.register(getName(), this);
    }
    
    /**
    * {@inheritDoc}
    */
    public List<ParameterDefinition> getParameterDefinitions()
    {
        return Collections.emptyList();
    }
    
    public abstract String getName();
}
