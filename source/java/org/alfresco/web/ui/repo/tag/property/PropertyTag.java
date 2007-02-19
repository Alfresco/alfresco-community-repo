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
 * @author gavinc
 */
public class PropertyTag extends BaseComponentTag
{
   private String name;
   private String displayLabel;
   private String readOnly;
   private String mode;
   private String converter;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return "org.alfresco.faces.PropertyRenderer";
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.Property";
   }

   /**
    * @param displayLabel Sets the display label
    */
   public void setDisplayLabel(String displayLabel)
   {
      this.displayLabel = displayLabel;
   }

   /**
    * @param name Sets the name
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * @param readOnly Sets whether the property is read only
    */
   public void setReadOnly(String readOnly)
   {
      this.readOnly = readOnly;
   }
   
   /**
    * @param mode The mode, either "edit" or "view"
    */
   public void setMode(String mode)
   {
      this.mode = mode;
   }
   
   /**
    * @param converter Sets the id of the converter
    */
   public void setConverter(String converter)
   {
      this.converter = converter;
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setStringProperty(component, "name", this.name);
      setStringProperty(component, "displayLabel", this.displayLabel);
      setStringProperty(component, "mode", this.mode);
      setStringProperty(component, "converter", this.converter);
      setBooleanProperty(component, "readOnly", this.readOnly);
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#release()
    */
   public void release()
   {
      this.name = null;
      this.displayLabel = null;
      this.mode = null;
      this.converter = null;
      this.readOnly = null;
      
      super.release();
   }
}
