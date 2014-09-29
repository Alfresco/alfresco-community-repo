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


/**
 * An interface for Solr facet value and facet query result display label handler.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public interface FacetLabelDisplayHandler
{
    /**
     * Gets the user friendly display label for the returned facet value
     * 
     * @param value the facet value
     * @return user friendly display label or the original value, if there is no result
     */
    public FacetLabel getDisplayLabel(String value);
}
