/*
 * Created on 25-May-2005
 */
package org.alfresco.web.ui.repo.tag;


/**
 * @author Mike Hatfield
 */
public class AjaxTagSelectorTag extends AjaxItemSelectorTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.AjaxTagPicker";
   }
}
