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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.scripts.jsf;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.BaseComponentTag;

/**
 * JSF tag class for the UIWebScript component.
 * 
 * @author Kevin Roast
 */
public class WebScriptTag extends BaseComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   @Override
   public String getComponentType()
   {
      return "org.alfresco.faces.WebScript";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   @Override
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
      setStringProperty(component, "scriptUrl", this.scriptUrl);
      setStringProperty(component, "context", this.context);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.scriptUrl = null;
      this.context = null;
   }
   
   /**
    * Set the script service Url
    *
    * @param scriptUrl     the script service Url
    */
   public void setScriptUrl(String scriptUrl)
   {
      this.scriptUrl = scriptUrl;
   }
   
   /**
    * Set the script context
    *
    * @param context     the script context
    */
   public void setContext(String context)
   {
      this.context = context;
   }


   /** the script context */
   private String context;

   /** the scriptUrl */
   private String scriptUrl;
}
