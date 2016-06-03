package org.alfresco.web.ui.repo.tag;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.tag.HtmlComponentTag;
import org.alfresco.web.ui.repo.component.UILanguageSelector;
import org.alfresco.web.ui.repo.component.UIMimeTypeSelector;

/**
 * Tag class for the Language selector component
 * 
 * @author Yannick Pignot
 */
public class LanguageSelectorTag extends HtmlComponentTag
{
   /** The value */
   private String value;
      
   /** Whether the component is disabled */
   private String disabled;
   
   /** 
    *  If the value is 'true', only the missing translations of the node will be return
    *  Else returns all the languages  
    */
   private String onlyAvailable;
   
   /**
    * If the value is 'true', the language of the node will be returned.
    * 
    * Without effect if <code>onlyAvailable</code> is 'false'
    */
   private String returnCurLgge;
   
   
   @Override
   public String getComponentType()
   {
      return UILanguageSelector.COMPONENT_TYPE;
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
      
      if(onlyAvailable != null && "true".equalsIgnoreCase(onlyAvailable))
      {
         UILanguageSelector lggSelector = (UILanguageSelector) getComponentInstance();
         lggSelector.setOnlyAvailableLanguages(true);
         
         if(returnCurLgge != null && "true".equalsIgnoreCase(returnCurLgge))
           {
              lggSelector.setReturnCurrentLanguage(true);
           }
      }
      
   }
   
   @Override
   public void release()
   {
      super.release();
      
      this.value = null;
      this.disabled = null;
      this.onlyAvailable = null;
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

   /**
    * Sets whether the component should returns each language or only the available
    * translations of the cuyrrent node.
    * 
    * @param onlyAvailable
    */
   public void setOnlyAvailable(String onlyAvailable) 
   {
      this.onlyAvailable = onlyAvailable;
   }

   public String getOnlyAvailable() 
   {
      return onlyAvailable;
   }

   public String getReturnCurLgge() {
      return returnCurLgge;
   }

   /**
    * Sets whether the component should returns the language of the node 
     * 
    * Without effect if <code>onlyAvailable</code> is false.
    * 
    * @param returnCurLgge
    */
   public void setReturnCurLgge(String returnCurLgge) {
      this.returnCurLgge = returnCurLgge;
   }
}
