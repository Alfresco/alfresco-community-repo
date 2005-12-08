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
 * @author kevinr
 */
public final class MergeSort extends Sort
{
   /**
    * Constructor
    * 
    * @param data             a the List of String[] data to sort
    * @param column           the column getter method to use on the row to sort
    * @param bForward         true for a forward sort, false for a reverse sort
    * @param mode             sort mode to use (see IDataContainer constants)
    */
   public MergeSort(List data, String column, boolean bForward, String mode)
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
         // TODO: finish this!
         //mergesort(this.data, 0, this.data.size() - 1);
         
         /*a = this.data;
          
          int n = a.length;
          
          b = new int[(n+1) >> 1];
          mergesort(0, n-1);*/
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Private methods
   
   /*private static Object[] a, b;
   
   private static void mergesort(int lo, int hi)
   {
      if (lo<hi)
      {
         int m=(lo+hi) >> 1;
         mergesort(lo, m);
         mergesort(m+1, hi);
         merge(lo, m, hi);
      }
   }
   
   private static void merge(int lo, int m, int hi)
   {
      int i, j, k;
      
      i=0;
      j=lo;
      
      // copy first half of array a to auxiliary array b
      while (j <= m)
      {
         b[i++] = a[j++];
      }
      
      i=0;
      k=lo;
      
      // copy back next-greatest element at each time
      while (k < j && j <= hi)
      {
         if (b[i] <= a[j])
         {
            a[k++]=b[i++];
         }
         else
         {
            a[k++]=a[j++];
         }
      }
      
      // copy back remaining elements of first half (if any)
      while (k < j)
      {
         a[k++] = b[i++];
      }
   }*/
}
