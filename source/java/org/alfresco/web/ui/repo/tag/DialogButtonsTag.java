package org.alfresco.web.ui.repo.tag;

import org.alfresco.web.ui.common.tag.HtmlComponentTag;

/**
 * Tag class that allows the UIDialogButtons component to be placed on a JSP.
 * 
 * @author gavinc
 */
public class DialogButtonsTag extends HtmlComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.DialogButtons";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return null;
   }
}
