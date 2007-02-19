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
package org.alfresco.web.ui.repo.tag.property;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.BaseComponentTag;

/**
 * Base class for all association editor tag implementations
 * 
 * @author gavinc
 */
public abstract class BaseAssociationEditorTag extends BaseComponentTag
{
   private String associationName;
   private String availableOptionsSize;
   private String selectItemMsg;
   private String selectItemsMsg;
   private String selectedItemsMsg;
   private String noSelectedItemsMsg;
   private String disabled;
   private String value;
   
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
      
      setStringStaticProperty(component, "availableOptionsSize", this.availableOptionsSize);
      setStringProperty(component, "associationName", this.associationName);
      setStringProperty(component, "selectItemMsg", this.selectItemMsg);
      setStringProperty(component, "selectItemsMsg", this.selectItemsMsg);
      setStringProperty(component, "selectedItemsMsg", this.selectedItemsMsg);
      setStringProperty(component, "noSelectedItemsMsg", this.noSelectedItemsMsg);
      setStringProperty(component, "value", this.value);
      setBooleanProperty(component, "disabled", this.disabled);
   }
   
   /**
    * @param value The value to set.
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * Sets the association name
    * 
    * @param associationName The association name
    */
   public void setAssociationName(String associationName)
   {
      this.associationName = associationName;
   }
   
   /**
    * @param availableOptionsSize Sets the size of the available options size when 
    *        multiple items can be selected
    */
   public void setAvailableOptionsSize(String availableOptionsSize)
   {
      this.availableOptionsSize = availableOptionsSize;
   }
   
   /**
    * Sets the message to display for the no selected items
    * 
    * @param noSelectedItemsMsg The message
    */
   public void setNoSelectedItemsMsg(String noSelectedItemsMsg)
   {
      this.noSelectedItemsMsg = noSelectedItemsMsg;
   }
   
   /**
    * Sets the message to display for the selected items
    * 
    * @param selectedItemsMsg The message
    */
   public void setSelectedItemsMsg(String selectedItemsMsg)
   {
      this.selectedItemsMsg = selectedItemsMsg;
   }

   /**
    * Sets the message to display for inviting the user to select an item
    * 
    * @param selectItemMsg The message
    */
   public void setSelectItemMsg(String selectItemMsg)
   {
      this.selectItemMsg = selectItemMsg;
   }
   
   /**
    * Sets the message to display for inviting the user to select items
    * 
    * @param selectItemsMsg The message
    */
   public void setSelectItemsMsg(String selectItemsMsg)
   {
      this.selectItemsMsg = selectItemsMsg;
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
    * @see javax.faces.webapp.UIComponentTag#release()
    */
   public void release()
   {
      this.associationName = null;
      this.availableOptionsSize = null;
      this.selectItemMsg = null;
      this.selectItemsMsg = null;
      this.selectedItemsMsg = null;
      this.noSelectedItemsMsg = null;
      this.disabled = null;
      this.value = null;

      super.release();
   }
}
