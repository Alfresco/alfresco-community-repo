package org.alfresco.web.ui.repo.tag.property;

/**
 * Allows the AssociationEditor component to be added to a JSP page
 * 
 * @author gavinc
 */
public class AssociationEditorTag extends BaseAssociationEditorTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.AssociationEditor";
   }
}
