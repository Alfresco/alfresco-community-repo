package org.alfresco.web.ui.repo.tag.property;

/**
 * Allows the ChildAssociationEditor component to be added to a JSP page
 * 
 * @author gavinc
 */
public class ChildAssociationEditorTag extends BaseAssociationEditorTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.ChildAssociationEditor";
   }
}
