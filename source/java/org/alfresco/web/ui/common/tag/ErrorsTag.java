/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.ui.common.tag;

import javax.faces.component.UIComponent;

public class ErrorsTag extends HtmlComponentTag
{
   private String message;
   private String errorClass;
   private String infoClass;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "javax.faces.Messages";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return "org.alfresco.faces.Errors";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setStringProperty(component, "message", this.message);
      setStringProperty(component, "errorClass", this.errorClass);
      setStringProperty(component, "infoClass", this.infoClass);
   }

   /**
    * @param message Sets the message to display
    */
   public void setMessage(String message)
   {
      this.message = message;
   }
   
   /**
    * @param errorClass The CSS class to use for error messages
    */
   public void setErrorClass(String errorClass)
   {
      this.errorClass = errorClass;
   }
   
   /**
    * @param infoClass The CSS class to use for info messages
    */
   public void setInfoClass(String infoClass)
   {
      this.infoClass = infoClass;
   }
}
