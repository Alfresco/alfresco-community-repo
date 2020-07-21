/*
 * #%L
 * Alfresco Data model classes
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

package org.alfresco.repo.search.adaptor.lucene;

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
