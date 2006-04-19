package org.alfresco.web.ui.repo.tag;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.tag.HtmlComponentTag;
import org.alfresco.web.ui.repo.component.UIMimeTypeSelector;

/**
 * Tag class for the MIME type selector component
 * 
 * @author gavinc
 */
public class MimeTypeSelectorTag extends HtmlComponentTag
{
   /** The value */
   private String value;
   
   /** Whether the component is disabled */
   private String disabled;
   
   @Override
   public String getComponentType()
   {
      return UIMimeTypeSelector.COMPONENT_TYPE;
   }

   @Override
   public String getRendererType()
   {
      return ComponentConstants.JAVAX_FACES_MENU;
   }
   
   @Override
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setStringBindingProperty(component, "value", this.value);
      setBooleanProperty(component, "disabled", this.disabled);
   }
   
   @Override
   public void release()
   {
      super.release();
      
      this.value = null;
      this.disabled = null;
   }
   
   /**
    * Set the value
    *
    * @param value     the value
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * Sets whether the component should be rendered in a disabled state
    * 
    * @param disabled true to render the component in a disabled state
    */
   public void setDisabled(String disabled)
   {
      this.disabled = disabled;
   }
}
