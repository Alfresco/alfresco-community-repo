package org.alfresco.web.ui.repo.tag;


/**
 * Tag implementation to allow the category selector component to be placed on a JSP page
 * 
 * @author gavinc
 */
public class CategorySelectorTag extends ItemSelectorTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.CategorySelector";
   }
}
