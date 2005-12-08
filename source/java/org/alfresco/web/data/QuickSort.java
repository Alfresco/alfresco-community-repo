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
