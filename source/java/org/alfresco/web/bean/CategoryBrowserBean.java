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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.bean;
 
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.bean.search.SearchContext;

public class CategoryBrowserBean
{
      public static String BEAN_NAME = "CategoryBrowserBean";
 
   private NodeService nodeService;
 
   private NodeRef currentCategory = null;
 
   private boolean includeSubcategories = false;
 
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
 
   public void setCurrentCategory(NodeRef currentCategory)
   {
      this.currentCategory = currentCategory;
   }
 
   public String getCurrentCategoryName()
   {
      String currentCategoryName = null;
      if (currentCategory != null)
         currentCategoryName = (String) this.nodeService.getProperty(currentCategory, ContentModel.PROP_NAME);
 
      return currentCategoryName;
   }
 
   public boolean isIncludeSubcategories()
   {
      return includeSubcategories;
   }
 
   public void setIncludeSubcategories(boolean includeSubcategories)
   {
      this.includeSubcategories = includeSubcategories;
   }
 
   public SearchContext generateCategorySearchContext()
   {
      SearchContext categorySearch = new SearchContext();
 
      String[] categories = new String[1];
      categories[0] = SearchContext.getPathFromSpaceRef(currentCategory, includeSubcategories);
 
      categorySearch.setText("");
      categorySearch.setCategories(categories);
 
      return categorySearch;
   }
}
