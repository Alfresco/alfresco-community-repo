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
