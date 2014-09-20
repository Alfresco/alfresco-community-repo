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

import java.util.List;

import org.alfresco.util.Pair;

/**
 * Term suggestions response object
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public interface SuggesterResult
{

    /**
     * Get the number of suggestions
     * 
     * @return
     */
    long getNumberFound();

    /**
     * Get the list of suggestions as ("term", "weight") pairs. Never <i>null</i>.
     * 
     * @return list of suggestions
     */
    List<Pair<String, Integer>> getSuggestions();
}
