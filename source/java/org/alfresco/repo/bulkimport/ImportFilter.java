/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
