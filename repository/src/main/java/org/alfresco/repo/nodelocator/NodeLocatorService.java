/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.nodelocator;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This service is responsible for locating {@link NodeRef}s in the repository using {@link NodeLocator} strategies.
 * 
 * @author Nick Smith
 * @since 4.0
 *
 */
@AlfrescoPublicApi
public interface NodeLocatorService
{
    /**
     * Locates and returns a {@link NodeRef} using the specified {@link NodeLocator}.
     * 
     * @param locatorName the name of the {@link NodeLocator} to use.
     * @param source the source node. Can be <code>null</code>.
     * @param params An arbitrary set of parameters. Can be <code>null</code>.
     * @return the node to be found or <code>null</code>.
     */
    NodeRef getNode(String locatorName, NodeRef source, Map<String, Serializable> params);
    
    void register(String locatorName, NodeLocator locator);
}
