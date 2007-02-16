/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.ui.common.tag;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;

/**
 * @author kevinr
 */
public class BreadcrumbTag extends HtmlComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.Breadcrumb";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return "org.alfresco.faces.BreadcrumbRenderer";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setActionProperty((UICommand)component, this.action);
      setActionListenerProperty((UICommand)component, this.actionListener);
      setStringProperty(component, "separator", this.separator);
      setBooleanProperty(component, "showRoot", this.showRoot);
      setBooleanProperty(component, "immediate", this.immediate);
      setStringProperty(component, "value", this.value);
   }

   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.action = null;
      this.actionListener = null;
      this.separator = ">";
      this.showRoot = "true";
      this.immediate = null;
      this.value = null;
   }
   
   /**
    * Set the action
    *
    * @param action     the action
    */
   public void setAction(String action)
   {
      this.action = action;
   }

   /**
    * Set the actionListener
    *
    * @param actionListener     the actionListener
    */
   public void setActionListener(String actionListener)
   {
      this.actionListener = actionListener;
   }

   /**
    * Set the separator
    *
    * @param separator     the separator
    */
   public void setSeparator(String separator)
   {
      this.separator = separator;
   }

   /**
    * Set the show root value
    *
    * @param showRoot     the showRoot
    */
   public void setShowRoot(String showRoot)
   {
      this.showRoot = showRoot;
   }
   
   /**
    * Set if the action event fired is immediate
    *
    * @param immediate     true if the action event fired is immediate
    */
   public void setImmediate(String immediate)
   {
      this.immediate = immediate;
   }
   
   /**
    * Set the value. The value for a breadcrumb is either a '/' separated String path
    * or a List of IBreadcrumb handler instances.
    *
    * @param value     the value
    */
   public void setValue(String value)
   {
      this.value = value;
   }


   /** the value */
   private String value;
   
   /** the action */
   private String action;

   /** the actionListener */
   private String actionListener;

   /** the separator */
   private String separator = ">";

   /** the showRoot value */
   private String showRoot = "true";
   
   /** true if the action event fired is immediate */
   private String immediate;
}
