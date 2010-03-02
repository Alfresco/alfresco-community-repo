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
package org.alfresco.web.ui.wcm.tag;

import javax.faces.component.UIComponent;

import org.springframework.extensions.webscripts.ui.common.tag.BaseComponentTag;

/**
 * Tag class that allows the DeploymentServers component to be used on a JSP page.
 * 
 * @author gavinc
 */
public class DeploymentServersTag extends BaseComponentTag
{
   private String value;
   private String currentServer;
   private String inAddMode;
   private String addType;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.DeploymentServers";
   }

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
      
      setStringProperty(component, "value", this.value);
      setStringProperty(component, "currentServer", this.currentServer);
      setStringProperty(component, "addType", this.addType);
      setBooleanProperty(component, "inAddMode", this.inAddMode);
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      this.value = null;
      this.currentServer = null;
      this.addType = null;
      this.inAddMode = null;
   }
   
   /**
    * @param value the value (the list of servers to show)
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * @param currentServer The server being edited or added
    */
   public void setCurrentServer(String currentServer)
   {
      this.currentServer = currentServer;
   }

   /**
    * @param inAddMode Flag to determine whether a new server should be added
    */
   public void setInAddMode(String inAddMode)
   {
      this.inAddMode = inAddMode;
   }

   /**
    * @param addType The type of server receiver to add 
    */
   public void setAddType(String addType)
   {
      this.addType = addType;
   }
}
