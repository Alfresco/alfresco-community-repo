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
