/*
 * Created on 25-May-2005
 */
package org.alfresco.web.ui.repo.tag;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.HtmlComponentTag;

/**
 * Base class for the item selector tag
 * 
 * @author Kevin Roast
 */
public abstract class AjaxItemSelectorTag extends HtmlComponentTag
{
   /** the value */
   private String value;

   /** the label */
   private String label;

   /** the id of initial selection */
   private String initialSelection;
   
   /** Whether the component is single or multi-select */
   private String singleSelect;
   
   /** Whether the component is disabled */
   private String disabled;
   
   /** the height */
   private String height;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public abstract String getComponentType();
   
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
      
      setStringBindingProperty(component, "value", this.value);
      setStringBindingProperty(component, "initialSelection", this.initialSelection);
      setStringProperty(component, "label", this.label);
      setBooleanProperty(component, "singleSelect", this.singleSelect);
      setBooleanProperty(component, "disabled", this.disabled);
      setStringProperty(component, "height", this.height);
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      
      this.value = null;
      this.label = null;
      this.singleSelect = null;
      this.initialSelection = null;
      this.disabled = null;
      this.height = null;
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
    * Set the label
    *
    * @param label     the label
    */
   public void setLabel(String label)
   {
      this.label = label;
   }

   /**
    * Set the singleSelect
    *
    * @param singleSelect     the singleSelect
    */
   public void setSingleSelect(String singleSelect)
   {
      this.singleSelect = singleSelect;
   }
   
   /**
    * Sets the id of the item to be initially selected, this is overridden
    * however if a value is supplied
    * 
    * @param initialSelection The id of the initial selected item
    */
   public void setInitialSelection(String initialSelection)
   {
      this.initialSelection = initialSelection;
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
    * Set the height
    *
    * @param height     the height
    */
   public void setHeight(String height)
   {
      this.height = height;
   }
}
