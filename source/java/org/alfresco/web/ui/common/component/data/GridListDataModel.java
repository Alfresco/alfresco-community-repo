/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.ui.common.component.data;

import java.util.List;

import org.alfresco.web.data.QuickSort;

/**
 * @author kevinr
 */
public class GridListDataModel implements IGridDataModel
{
   /**
    * Constructor
    * 
    * @param data    List of Object[] row data
    */
   public GridListDataModel(List data)
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
      return this.data.get(index);
   }
   
   /**
    * Return the number of rows in the data model
    * 
    * @return row count
    */
   public int size()
   {
      return this.data.size();
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
      try
      {
         QuickSort sorter = new QuickSort(this.data, column, !descending, mode);
         sorter.sort();
      }
      catch (Exception err)
      {
         throw new RuntimeException("Failed to sort data: " + err.getMessage(), err);
      }
   }
   
   private List data = null;
}
