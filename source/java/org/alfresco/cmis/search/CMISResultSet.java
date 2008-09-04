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


/**
 * A CMIS result set
 * 
 * @author andyh
 *
 */
public interface CMISResultSet extends Iterable<CMISResultSetRow>
{
    /**
     * Get the result set meta-data.
     * @return
     */
    public CMISResultSetMetaData getMetaData();
    
    /**
     * Get the start point for this results set in the overall
     * set of rows that match the query - this will be equals to the skip count 
     * set when executing the query.
     * @return
     */
    public int start();
    
    /**
     * Get the number of rows in this result set.
     * 
     * This will be less than or equal to the maximum number of rows requested or
     * the full length if no restriction on length is specified.
     * 
     * If a skip count is given, the length represents the number of results
     * after the skip count and does not include the items skipped.
     * 
     * @return
     */
    public int length();
    
    /**
     * Close the result set and release any resources held/
     * The result set is also bound to the transaction and will auto close at
     * the end of the transaction.
     */
    public void close();
    
    /**
     * Was this result set curtailed - are there more pages to the result set?
     * @return
     */
    public boolean hasMore();
    
    
    /**
     * Get the given row
     * @param i -the position in this result set - start + i gives the position in the overall result set
     * @return
     */
    public CMISResultSetRow getRow(int i);
  
}
