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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A registry which holds and provides the appropriate display handler for a
 * particular facet field.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class FacetLabelDisplayHandlerRegistry
{
    private final ConcurrentMap<String, FacetLabelDisplayHandler> registry;

    public FacetLabelDisplayHandlerRegistry()
    {
        this.registry = new ConcurrentHashMap<String, FacetLabelDisplayHandler>();
    }

    /**
     * Register an instance of {@code FacetLabelDisplayHandler} with the
     * specified field facet.
     * 
     * @param fieldFacet the field facet
     * @param displayHandler the display handler
     */
    public void addDisplayHandler(String fieldFacet, FacetLabelDisplayHandler displayHandler)
    {
        registry.putIfAbsent(fieldFacet, displayHandler);
    }

    /**
     * Gets the display handler.
     * 
     * @param fieldFacet the field facet to perform the lookup
     * @return the display handler or null if none found
     */
    public FacetLabelDisplayHandler getDisplayHandler(String fieldFacet)
    {
        return registry.get(fieldFacet);
    }
}
