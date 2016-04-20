package org.alfresco.web.ui.common.tag.debug;


/**
 * Tag implementation used to place the Repository properties component on a page.
 * 
 * @author kevinr
 */
public class RepositoryPropertiesTag extends BaseDebugTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.debug.RepositoryProperties";
   }
}
