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

   protected String outcome;
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
      String webProject = parameters.get("webproject");
      if (webProject == null)
      {
         this.webProjectRef = this.avmBrowseBean.getWebsite().getNodeRef();
      }
      else
      {
         this.webProjectRef = new NodeRef(webProject);
      }
      
      // determine outcome required
      String calledFromTaskDialog = parameters.get("calledFromTaskDialog");
      if (calledFromTaskDialog != null && 
          calledFromTaskDialog.equals(Boolean.TRUE.toString()))
      {
         outcome = "dialog:close:myalfresco";
      }
      else
      {
         outcome = "dialog:close:browseWebsite";
      }
      
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
      return this.outcome;
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
   
   // MER experiment
   protected int versionToDeploy;
   protected String[] deployTo;
   protected String store;
   protected String deployMode;
   protected String calledFromTaskDialog;
   protected NodeRef websiteRef;
   
   /**
    * Returns the remote servers to deploy to as an array
    * 
    * @return String array of servers to deploy to
    */
   public String[] getDeployTo()
   {
      return this.deployTo;
   }
   
   /**
    * Sets the list of remote servers to deploy to
    * 
    * @param deployTo String array of servers to deploy to
    */
   public void setDeployTo(String[] deployTo)
   {
      this.deployTo = deployTo;
   }
   
   /**
    * Returns the type of server to deploy to, either 'live' or 'test'.
    * 
    * @return The type of server to deploy to
    */
   public String getDeployMode()
   {
      return this.deployMode;
   }
   
   /**
    * @return The store being deployed
    */
   public String getStore()
   {
      return this.store;
   }
   
   /**
    * @return The version of the snapshot to deploy
    */
   public int getSnapshotVersion()
   {
      return this.versionToDeploy;
   }
}
