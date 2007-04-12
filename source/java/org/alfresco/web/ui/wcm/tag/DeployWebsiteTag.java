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
package org.alfresco.web.ui.wcm.tag;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.BaseComponentTag;

/**
 * Tag class that allows the DeployWebsite component to be used on a JSP page.
 * 
 * @author gavinc
 */
public class DeployWebsiteTag extends BaseComponentTag
{
   private String value;
   private String website;
   private String monitor;
   private String monitorIds;
   private String snapshotVersion;
   
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
      setBooleanProperty(component, "monitor", this.monitor);
      setStringProperty(component, "monitorIds", this.monitorIds);
      setIntProperty(component, "snapshotVersion", this.snapshotVersion);
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      this.value = null;
      this.website = null;
      this.monitor = null;
      this.monitorIds = null;
      this.snapshotVersion = null;
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
    * @param snapshotVersion The version of the snapshot to deploy to
    */
   public void setSnapshotVersion(String snapshotVersion)
   {
      this.snapshotVersion = snapshotVersion;
   }
}
