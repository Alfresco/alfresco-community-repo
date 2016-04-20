package org.alfresco.web.ui.repo.tag.property;

/**
 * Allows associations to be displayed as part of a property sheet component on a JSP page
 * 
 * @author gavinc
 */
public class AssociationTag extends PropertySheetItemTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return "org.alfresco.faces.AssociationRenderer";
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.Association";
   }
}
