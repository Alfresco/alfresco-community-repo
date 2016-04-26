package org.alfresco.web.ui.common.tag.data;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.HtmlComponentTag;


/**
 * @author kevinr
 */
public class DataPagerTag extends HtmlComponentTag
{
   // ------------------------------------------------------------------------------
   // Component methods 
   
   private String dataPagerType;
   private String displayInput;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.DataPager";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      // UIDataPager is self rendering
      return null;
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringProperty(component, "dataPagerType", this.dataPagerType);
      setBooleanProperty(component, "displayInput", this.displayInput);
   }

   public void setDataPagerType(String dataPagerType)
   {
      this.dataPagerType = dataPagerType;
   }
   
   public void setDisplayInput(String displayInput)
   {
      this.displayInput = displayInput;
   }

   
}
