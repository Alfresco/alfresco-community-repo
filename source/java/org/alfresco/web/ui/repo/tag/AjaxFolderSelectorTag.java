/*
 * Created on 25-May-2005
 */
package org.alfresco.web.ui.repo.tag;


/**
 * @author Kevin Roast
 */
public class AjaxFolderSelectorTag extends AjaxItemSelectorTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.AjaxFolderPicker";
   }
}
