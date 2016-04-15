
package org.alfresco.web.ui.repo.tag;

/**
 * Tag class to allow the web project folder selector to be used on a JSP
 * 
 * @author gavinc
 */
public class WebProjectFolderSelectorTag extends ItemSelectorTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.WebProjectFolderSelector";
   }
}
