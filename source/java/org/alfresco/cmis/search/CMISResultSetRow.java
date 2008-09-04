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
package org.alfresco.cmis.search;

import java.io.Serializable;
import java.util.Map;

/**
 * A row in a CMISResultSet
 * 
 * @author andyh
 *
 */
public interface CMISResultSetRow
{
    /**
     * Get all the column data.
     * @return - a map of serializable column values with teh column name as the key
     */
    public Map<String, Serializable> getValues();
    
    /**
     * Get the data for a single column
     * @param columnName
     * @return the value
     */
    public Serializable getValue(String columnName);
    
    /**
     * Get the overall score.
     * 
     * @return
     */
    public float getScore();
    
    /**
     * Get the scores .
     * @return a map of selector name to score.
     */
    public Map<String, Float> getScores();
    
    /**
     * Get the score related to the names selector.
     * @param selectorName
     * @return - the score.
     */
    public float getScore(String selectorName);
    
    /**
     * Get the index of this result set in the result set 
     * If you want the overall position in paged reults you have to add the skipCount fo the result set. 
     * @return
     */
    public int getIndex();
    
    /**
     * Get the result set for which this row is a member.
     * @return - the result set.
     */
    public CMISResultSet getResultSet();
}
