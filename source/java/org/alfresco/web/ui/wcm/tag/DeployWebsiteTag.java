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
 * Tag class that allows the DeployWebsite component to be used on a JSP page.
 * 
 * @author gavinc
 */
public class DeployWebsiteTag extends BaseComponentTag
{
   private String value;
   private String website;
   private String store;
   private String monitor;
   private String monitorIds;
   private String snapshotVersion;
   private String deployMode;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.DeployWebsite";
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
      setStringProperty(component, "website", this.website);
      setStringProperty(component, "store", this.store);
      setBooleanProperty(component, "monitor", this.monitor);
      setStringProperty(component, "monitorIds", this.monitorIds);
      setIntProperty(component, "snapshotVersion", this.snapshotVersion);
      setStringProperty(component, "deployMode", this.deployMode);
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      this.value = null;
      this.website = null;
      this.store = null;
      this.monitor = null;
      this.monitorIds = null;
      this.snapshotVersion = null;
      this.deployMode = null;
   }
   
   /**
    * @param value the value (the list of servers to deploy to)
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   /**
    * @param monitor Flag to indicate whether to select servers or show deployment progress
    */
   public void setMonitor(String monitor)
   {
      this.monitor = monitor;
   }
   
   /**
    * @param monitorIds List of deployment monitor IDs to look for
    */
   public void setMonitorIds(String monitorIds)
   {
      this.monitorIds = monitorIds;
   }

   /**
    * @param website NodeRef of the web project being deployed
    */
   public void setWebsite(String website)
   {
      this.website = website;
   }
   
   /**
    * @param store The store being deployed to
    */
   public void setStore(String store)
   {
      this.store = store;
   }
   
   /**
    * @param snapshotVersion The version of the snapshot to deploy to
    */
   public void setSnapshotVersion(String snapshotVersion)
   {
      this.snapshotVersion = snapshotVersion;
   }
   
   /**
    * @param deployMode The type of server being deployed to
    */
   public void setDeployMode(String deployMode)
   {
      this.deployMode = deployMode;
   }
}
