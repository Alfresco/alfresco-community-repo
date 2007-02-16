/*
 * Copyright (C) 2005 Alfresco, Inc.
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

import java.util.List;

/**
 * QuickSort
 * 
 * Implementation of a locale sensitive Quick Sort algorithm. The sorting supports
 * locale specific case sensitive, case in-sensitive and numeric data sorting. The
 * numeric sorting handles integer, floating point and scientific formats, with
 * short-circuit value parsing.
 * 
 * @author Kevin Roast
 */
public final class QuickSort extends Sort
{
   /**
    * Constructor
    * 
    * @param data             a the List of String[] data to sort
    * @param column           the column getter method to use on the row to sort
    * @param bForward         true for a forward sort, false for a reverse sort
    * @param mode             sort mode to use (see IDataContainer constants)
    */
   public QuickSort(List data, String column, boolean bForward, String mode)
   {
      super(data, column, bForward, mode);
   }
   
   
   // ------------------------------------------------------------------------------
   // Sort Implementation
   
   /**
    * Runs the Quick Sort routine on the current dataset
    */
   public void sort()
   {
      if (this.data.size() != 0)
      {
         qsort(this.data, 0, this.data.size() - 1);
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Private methods
   
   /**
    * recursive Quicksort function.
    * 
    * @param v       the array out of which to take a slice.
    * @param lower   the lower bound of this slice.
    * @param upper   the upper bound of this slice.
    */
   private void qsort(final List v, final int lower, final int upper)
   {
      int sliceLength = upper - lower + 1 ;
      if (sliceLength > 1)
      {
         if (sliceLength < 7)
         {
            // Insertion sort on smallest datasets
            for (int i=lower; i<=upper; i++)
            {
               if (this.bForward == true)
               {
                  for (int j=i; j > lower && getComparator().compare(this.keys.get(j - 1), this.keys.get(j)) > 0; j--)
                  {
                     // swap both the keys and the actual row data
                     swap(this.keys, j - 1, j);
                     swap(v, j - 1, j);
                  }
               }
               else
               {
                  for (int j=i; j > lower && getComparator().compare(this.keys.get(j - 1), this.keys.get(j)) < 0; j--)
                  {
                     // swap both the keys and the actual row data
                     swap(this.keys, j - 1, j);
                     swap(v, j - 1, j);
                  }
               }
            }
         }
         else
         {
            int pivotIndex = partition(v, lower, upper);
            qsort(v, lower, pivotIndex);
            qsort(v, pivotIndex + 1, upper);
         }
      }
   }
   
   /**
    *  Partition an array in two using the pivot value that is at the
    *  centre of the array being partitioned.
    *
    *  This partition implementation based on that in Winder, R
    *  (1993) "Developing C++ Software", Wiley, p.395.  NB. This
    *  implementation (unlike most others) does not guarantee that
    *  the split point contains the pivot value.  Unlike other
    *  implementations, it requires only < (or >) relation and not
    *  both < and <= (or > and >=).  Also, it seems easier to program
    *  and to understand.
    *
    *  @param v       the List out of which to take a slice.
    *  @param lower   the lower bound of this slice.
    *  @param upper   the upper bound of this slice.
    */
   private int partition(final List v, int lower, int upper)
   {
      List keys = this.keys;
      Object pivotValue = keys.get((upper + lower + 1) >> 1) ;
      
      int size = keys.size();
      
      while (lower <= upper)
      {
         if (this.bForward == true)
         {
            while (getComparator().compare(keys.get(lower), pivotValue) < 0)
            {
               lower++;
            }
            while (getComparator().compare(pivotValue, keys.get(upper)) < 0)
            {
               upper--;
            }
         }
         else
         {
            while (getComparator().compare(keys.get(lower), pivotValue) > 0)
            {
               lower++;
            }
            while (getComparator().compare(pivotValue, keys.get(upper)) > 0)
            {
               upper--;
            }
         }
         if (lower <= upper)
         {
            if (lower < upper)
            {
               swap(keys, lower, upper);
               swap(v, lower, upper);
            }
            lower++;
            upper--;
         }
      }
      
      return upper;
   }
   
} // end class QuickSort
