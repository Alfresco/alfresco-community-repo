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

/**
 * Tag to place the UIOutputText component on the page
 * 
 * @author gavinc
 */
public class OutputTextTag extends HtmlComponentTag
{
   private String value;
   private String encodeForJavaScript;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.OutputText";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      // the component is self renderering
      return null;
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringProperty(component, "value", this.value);
      setBooleanProperty(component, "encodeForJavaScript", this.encodeForJavaScript);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.value = null;
      this.encodeForJavaScript = null;
   }
   
   /**
    * Set the value
    *
    * @param value  The text
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * Set the encodeForJavaScript flag
    * 
    * @param encodeForJavaScript true to encode the text for use in JavaScript
    */
   public void setEncodeForJavaScript(String encodeForJavaScript)
   {
      this.encodeForJavaScript = encodeForJavaScript;
   }
}
