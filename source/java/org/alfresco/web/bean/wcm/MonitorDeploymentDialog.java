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
package org.alfresco.web.bean.wcm;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Monitors the deployment of a web project snapshot to one or more remote servers.
 * 
 * @author gavinc
 */
public class MonitorDeploymentDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -2800892205678915972L;

   protected NodeRef webProjectRef;
   
   protected AVMBrowseBean avmBrowseBean;
   
   private static final Log logger = LogFactory.getLog(MonitorDeploymentDialog.class);
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // setup context for dialog
      this.webProjectRef = this.avmBrowseBean.getWebsite().getNodeRef();
      
      if (logger.isDebugEnabled())
      {
         logger.debug("Initialising dialog to monitor deployment of " + 
                  this.webProjectRef.toString());
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return outcome;
   }
   
   @Override
   protected String getDefaultCancelOutcome()
   {
      return "dialog:close:browseWebsite";
   }

   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "close");
   }
   
   // ------------------------------------------------------------------------------
   // Bean getters and setters
   
   /**
    * @param avmBrowseBean    The AVM BrowseBean to set
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * @return The NodeRef of the web project the deployment reports are being shown for
    */
   public NodeRef getWebProjectRef()
   {
      return this.webProjectRef;
   }
}
