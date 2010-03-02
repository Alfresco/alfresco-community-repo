/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
