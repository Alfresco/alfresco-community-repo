/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
/*
 * Created on 25-May-2005
 */
package org.alfresco.web.ui.repo.tag;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.HtmlComponentTag;

/**
 * Base class for the item selector tag
 * 
 * @author gavinc
 */
public abstract class ItemSelectorTag extends HtmlComponentTag
{
   /** the value */
   private String value;

   /** the label */
   private String label;

   /** the spacing */
   private String spacing;
   
   /** the node style */
   private String nodeStyle;
   
   /** the node style class */
   private String nodeStyleClass;
   
   /** the id of initial selection */
   private String initialSelection;
   
   /** Whether the component is disabled */
   private String disabled;
   
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
      setStringProperty(component, "nodeStyle", this.nodeStyle);
      setStringProperty(component, "nodeStyleClass", this.nodeStyleClass);
      setIntProperty(component, "spacing", this.spacing);
      setBooleanProperty(component, "disabled", this.disabled);
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      
      this.value = null;
      this.label = null;
      this.spacing = null;
      this.nodeStyle = null;
      this.nodeStyleClass = null;
      this.initialSelection = null;
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
    * Set the label
    *
    * @param label     the label
    */
   public void setLabel(String label)
   {
      this.label = label;
   }

   /**
    * Set the spacing
    *
    * @param spacing     the spacing
    */
   public void setSpacing(String spacing)
   {
      this.spacing = spacing;
   }

   /**
    * Set the node style
    * 
    * @param nodeStyle  the node style
    */
   public void setNodeStyle(String nodeStyle)
   {
      this.nodeStyle = nodeStyle;
   }

   /**
    * Set the node style class
    * 
    * @param nodeStyleClass   the node style class
    */
   public void setNodeStyleClass(String nodeStyleClass)
   {
      this.nodeStyleClass = nodeStyleClass;
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
}
