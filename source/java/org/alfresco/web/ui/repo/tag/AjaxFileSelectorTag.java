/*
 * Created on 25-May-2005
 */
package org.alfresco.web.ui.repo.tag;

import javax.faces.component.UIComponent;


/**
 * @author Kevin Roast
 */
public class AjaxFileSelectorTag extends AjaxItemSelectorTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.AjaxFilePicker";
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringProperty(component, "mimetypes", this.mimetypes);
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      this.mimetypes = null;
   }
   
   /**
    * Set the mimetypes
    *
    * @param mimetypes     the mimetypes
    */
   public void setMimetypes(String mimetypes)
   {
      this.mimetypes = mimetypes;
   }


   /** the mimetypes */
   private String mimetypes;
}
