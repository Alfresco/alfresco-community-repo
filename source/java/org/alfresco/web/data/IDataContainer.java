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
package org.alfresco.web.data;

/**
 * @author kevinr
 */
public interface IDataContainer
{
   /**
    * Return the currently sorted column if any
    * 
    * @return current sorted column if any
    */
   public String getCurrentSortColumn();
   
   /**
    * Returns the current sort direction. Only valid if a sort column is set.
    * True is returned for descending sort, false for accending sort.
    * 
    * @return true for descending sort, false for accending sort
    */
   public boolean isCurrentSortDescending();
   
   /**
    * Returns the current page size used for this list, or -1 for no paging.
    */
   public int getPageSize();
   
   /**
    * Return the current page the list is displaying
    * 
    * @return Current page with zero based index
    */
   public int getCurrentPage();
   
   /**
    * Set the current page to display.
    * 
    * @param index      Zero based page index to display
    */
   public void setCurrentPage(int index);
   
   /**
    * Return the count of max available pages
    * 
    * @return count of max available pages
    */
   public int getPageCount();
   
   /**
    * Returns true if a row of data is available
    * 
    * @return true if data is available, false otherwise
    */
   public boolean isDataAvailable();
   
   /**
    * Returns the next row of data from the data model
    * 
    * @return next row of data as a Bean object
    */
   public Object nextRow();
   
   /**
    * Sort the dataset using the specified sort parameters
    * 
    * @param column        Column to sort
    * @param descending    True for descending sort, false for ascending
    * @param mode          Sort mode to use (see IDataContainer constants)
    */
   public void sort(String column, boolean descending, String mode);
   
   public final static String SORT_CASEINSENSITIVE = "case-insensitive";
   public final static String SORT_CASESENSITIVE   = "case-sensitive";
}
