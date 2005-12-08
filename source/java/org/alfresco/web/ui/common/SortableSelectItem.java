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
      if (this.sort == null && obj2 == null) return 0;
      if (this.sort == null) return -1;
      if (obj2 == null) return 1;
      return this.sort.compareToIgnoreCase( ((SortableSelectItem)obj2).sort );
   }
   
   private String sort;
}
