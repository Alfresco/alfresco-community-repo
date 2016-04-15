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

package org.alfresco.repo.bulkimport;

/**
 * Definition of a source filter - a class that filters out importable items idenfitied from the source
 * directory from the import.
 * 
 * Note that source filters can be "chained", in which case each source filter effectively has
 * "veto" power - if any single filter requests that a given importable item be filtered, it
 * <strong>will</strong> be filtered.
 *
 * @since 4.0
 */
public interface ImportFilter
{
    
    /**
     * Method that checks whether the given file or folder should be filtered.
     * 
     * @param importableItem The source importable item to check for filtering <i>(will not be null)</i>.
     * @return True if the given importable item should be filtered, false otherwise. 
     */
    boolean shouldFilter(final ImportableItem importableItem);
    
}
