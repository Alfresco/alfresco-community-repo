package org.alfresco.web.ui.common.tag;

import javax.faces.component.UIComponent;

/**
 * Tag to place the image picker component and radio renderer inside
 * a rounded corner panel
 * 
 * @author gavinc
 */
public class ImagePickerRadioPanelTag extends ImagePickerRadioTag
{
   private String panelBorder;
   private String panelBgcolor;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.ImagePicker";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return "org.alfresco.faces.RadioPanel";
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringProperty(component, "panelBorder", this.panelBorder);
      setStringProperty(component, "panelBgcolor", this.panelBgcolor);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.panelBorder = null;
      this.panelBgcolor = null;
   }

   public String getPanelBgcolor()
   {
      return panelBgcolor;
   }

   public void setPanelBgcolor(String panelBgcolor)
   {
      this.panelBgcolor = panelBgcolor;
   }

   public String getPanelBorder()
   {
      return panelBorder;
   }

   public void setPanelBorder(String panelBorder)
   {
      this.panelBorder = panelBorder;
   }
}
