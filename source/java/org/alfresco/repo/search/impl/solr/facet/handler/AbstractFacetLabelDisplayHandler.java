/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.search.impl.solr.facet.handler;

import java.util.Set;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.util.PropertyCheck;

/**
 * A support class for facet label display handlers.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public abstract class AbstractFacetLabelDisplayHandler implements FacetLabelDisplayHandler
{
    protected ServiceRegistry serviceRegistry;
    protected Set<String> supportedFieldFacets;
    private FacetLabelDisplayHandlerRegistry registry;

    /**
     * Registers this instance of the facet handler with the registry. This will
     * call the {@link #init()} method and then register if the registry is available.
     * 
     */
    public final void register()
    {
        init();

        for (String fieldFacet : supportedFieldFacets)
        {
            registry.addDisplayHandler(fieldFacet, this);
        }

    }

    protected void init()
    {
        PropertyCheck.mandatory(this, "registry", registry);
        PropertyCheck.mandatory(this, "serviceRegistry", serviceRegistry);
        PropertyCheck.mandatory(this, "supportedFieldFacets", supportedFieldFacets);
    }

    /**
     * @param supportedFieldFacets the supportedFieldFacets to set
     */
    public void setSupportedFieldFacets(Set<String> supportedFieldFacets)
    {
        this.supportedFieldFacets = supportedFieldFacets;
    }

    /**
     * Set the service registry
     * 
     * @param services the service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Set the registry to register with
     * 
     * @param registry a metadata extracter registry
     */
    public void setRegistry(FacetLabelDisplayHandlerRegistry registry)
    {
        this.registry = registry;
    }
}
