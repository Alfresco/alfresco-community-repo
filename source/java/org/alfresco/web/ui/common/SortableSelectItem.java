package org.alfresco.web.ui.common;

import javax.faces.model.SelectItem;

/**
 * Wrapper class to facilitate case-insensitive sorting functionality against our SelectItem objects
 * 
 * @author Kevin Roast
 */
public final class SortableSelectItem extends SelectItem implements Comparable
{
   public SortableSelectItem(String value, String label, String sort)
   {
      super(value, label);
      this.sort = sort;
   }
   
   public int compareTo(Object obj2)
   {
      SortableSelectItem s2 = ((SortableSelectItem)obj2);
      if (this.sort == null)
      {
          if (s2 == null || s2.sort == null)
          {
             return 0;
          }
          return -1;
      }
      else
      {
         if (s2 == null || s2.sort == null)
         {
            return 1;
         }
         return this.sort.compareToIgnoreCase( s2.sort );
      }
   }
   
   private String sort;
}
