package org.alfresco.web.ui.common.tag.description;

import javax.faces.component.UIComponent;

import org.springframework.extensions.webscripts.ui.common.tag.BaseComponentTag;

/**
 * Tag class to allow the description component to be used on a JSP page
 * 
 * @author gavinc
 */
public class DescriptionTag extends BaseComponentTag
{
   private String controlValue;
   private String text;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.Description";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return null;
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setStringProperty(component, "controlValue", this.controlValue);
      setStringProperty(component, "text", this.text);
   }

   /**
    * @param controlValue The value of the control this description is for
    */
   public void setControlValue(String controlValue)
   {
      this.controlValue = controlValue;
   }

   /**
    * @param text Sets the description text
    */
   public void setText(String text)
   {
      this.text = text;
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      
      this.controlValue = null;
      this.text = null;
   }
}
