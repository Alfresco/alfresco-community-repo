
/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.search.impl.lucene;

/**
 * Functions that can be applied to lucene fields
 * 
 * Currently upper and lower that perform a case insensitive match for untokenised fields.
 * (If the field is tokenised the match should already be case insensitive.)
 * 
 * @author andyh
 *
 */
public enum LuceneFunction
{
    /**
     * Match as if the field was converted to upper case.
     */
    UPPER, 
    /**
     * Match as if the field was converted to lower case.
     */
    LOWER, 
    /**
     * A normal lucene field match.
     */
    FIELD;
}   
