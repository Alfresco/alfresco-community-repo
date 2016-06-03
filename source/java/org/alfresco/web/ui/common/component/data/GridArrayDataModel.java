/*
 * #%L
 * Alfresco Repository WAR Community
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
package org.alfresco.web.ui.common.component.data;

/**
 * @author kevinr
 */
public class GridArrayDataModel implements IGridDataModel
{
   /**
    * Constructor
    * 
    * @param data    Array of Object (beans) row data 
    */
   public GridArrayDataModel(Object[] data)
   {
      this.data = data;
   }
   
   /**
    * Get a row object for the specified row index
    * 
    * @param index      valid row index
    * 
    * @return row object for the specified index
    */
   public Object getRow(int index)
   {
      return this.data[index];
   }
   
   /**
    * Return the number of rows in the data model
    * 
    * @return row count
    */
   public int size()
   {
      return this.data.length;
   }
   
   /**
    * Sort the data set using the specified sort parameters
    * 
    * @param column        Column to sort
    * @param descending    True for descending sort, false for ascending
    * @param mode          Sort mode to use (see IDataContainer constants)
    */
   public void sort(String column, boolean descending, String mode)
   {
   }
   
   private Object[] data = null;
}
