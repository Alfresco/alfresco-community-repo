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
