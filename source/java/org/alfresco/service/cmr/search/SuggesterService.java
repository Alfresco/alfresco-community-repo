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

package org.alfresco.service.cmr.search;

/**
 * A service that returns term suggestions
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public interface SuggesterService
{

    /**
     * Whether the Suggester is enabled (refer to 'solr.suggester.enabled' repository property) or not
     * 
     * @return true if the Suggester is enabled, false otherwise
     */
    public boolean isEnabled();

    /**
     * Get suggestions for the specified term {@link SuggesterParameters#term}
     * 
     * @param suggesterParameters the parameters to use
     * @return term suggestions result. Never <i>null</i>
     */
    public SuggesterResult getSuggestions(SuggesterParameters suggesterParameters);
}
