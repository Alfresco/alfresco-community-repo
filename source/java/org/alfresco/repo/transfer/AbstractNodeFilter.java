/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.transfer.NodeFilter;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * A utility base class that simplifies the creation of new node filters.
 * 
 * When used in conjunction with the standard node crawler ({@link StandardNodeCrawlerImpl}),
 * node filters that extend this base class will automatically have the service registry
 * injected into them and their <code>init</code> operations invoked at the appropriate time. 
 * 
 * @author Brian
 * @since 3.4
 */
public abstract class AbstractNodeFilter implements NodeFilter
{
    protected ServiceRegistry serviceRegistry;

    public void init()
    {
        ParameterCheck.mandatory("serviceRegistry", serviceRegistry);
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

}
