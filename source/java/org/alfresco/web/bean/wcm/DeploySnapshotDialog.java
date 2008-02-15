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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.actions.AVMDeploySnapshotAction;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.sandbox.SandboxConstants;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Deploys a web project snapshot to one or more remote servers.
 * 
 * @author gavinc
 */
public class DeploySnapshotDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 62702082716235924L;
   
   protected int versionToDeploy;
   protected String[] deployTo;
   protected String stagingStore;
   protected NodeRef websiteRef;
   protected NodeRef webProjectRef;
   
   protected AVMBrowseBean avmBrowseBean;
   transient private AVMService avmService;
   transient private ActionService actionService;
   
   private static final Log logger = LogFactory.getLog(DeploySnapshotDialog.class);
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // setup context for dialog
      this.deployTo = null;
      this.versionToDeploy = Integer.parseInt(parameters.get("version"));
      this.avmBrowseBean.getDeploymentMonitorIds().clear();
      this.webProjectRef = this.avmBrowseBean.getWebsite().getNodeRef();
      this.stagingStore = this.avmBrowseBean.getStagingStore();
      String storeRoot = AVMUtil.buildSandboxRootPath(this.stagingStore);
      this.websiteRef = AVMNodeConverter.ToNodeRef(this.versionToDeploy, storeRoot);
      
      if (logger.isDebugEnabled())
         logger.debug("Initialising dialog to deploy version " + this.versionToDeploy + 
                  " of " + this.websiteRef.toString());
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      if (logger.isDebugEnabled())
         logger.debug("Requesting deployment of: " + this.websiteRef.toString());
      
      if (this.deployTo != null && this.deployTo.length > 0)
      {
         NodeRef webProjectRef = this.avmBrowseBean.getWebsite().getNodeRef();
         List<String> selectedDeployToNames = new ArrayList<String>();
         
         // create a deploymentattempt node to represent this deployment
         String attemptId = GUID.generate();
         Map<QName, Serializable> props = new HashMap<QName, Serializable>(8, 1.0f);
         props.put(WCMAppModel.PROP_DEPLOYATTEMPTID, attemptId);
         props.put(WCMAppModel.PROP_DEPLOYATTEMPTTYPE, WCMAppModel.CONSTRAINT_LIVESERVER);
         props.put(WCMAppModel.PROP_DEPLOYATTEMPTSTORE, this.stagingStore);
         props.put(WCMAppModel.PROP_DEPLOYATTEMPTVERSION, this.versionToDeploy);
         props.put(WCMAppModel.PROP_DEPLOYATTEMPTTIME, new Date());
         NodeRef attempt = getNodeService().createNode(webProjectRef, 
               WCMAppModel.ASSOC_DEPLOYMENTATTEMPT, WCMAppModel.ASSOC_DEPLOYMENTATTEMPT, 
               WCMAppModel.TYPE_DEPLOYMENTATTEMPT, props).getChildRef();
         
         // execute a deploy action for each of the selected remote servers asynchronously
         for (String targetServer : this.deployTo)
         {
            if (targetServer.length() > 0)
            {
               NodeRef serverRef = new NodeRef(targetServer);
               if (getNodeService().exists(serverRef))
               {
                  // get all properties of the target server
                  Map<QName, Serializable> serverProps = getNodeService().getProperties(serverRef);
                  
                  String serverUri = AVMDeploySnapshotAction.calculateServerUri(serverProps);
                  String serverName = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERNAME);
                  if (serverName == null || serverName.length() == 0)
                  {
                     serverName = serverUri;
                  }
                  
                  if (logger.isDebugEnabled())
                     logger.debug("Issuing deployment request for: " + serverName);
                  
                  // remember the servers deployed to
                  selectedDeployToNames.add(serverName);
                  
                  // create a deployment monitor object, store in the session, 
                  // add to avm browse bean and pass to action
                  DeploymentMonitor monitor = new DeploymentMonitor(
                           this.websiteRef, serverRef, versionToDeploy, serverName, attemptId);
                  context.getExternalContext().getSessionMap().put(monitor.getId(), monitor);
                  this.avmBrowseBean.getDeploymentMonitorIds().add(monitor.getId());
                  
                  // create and execute the action asynchronously
                  Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
                  args.put(AVMDeploySnapshotAction.PARAM_WEBSITE, webProjectRef);
                  args.put(AVMDeploySnapshotAction.PARAM_SERVER, serverRef);
                  args.put(AVMDeploySnapshotAction.PARAM_ATTEMPT, attempt);
                  args.put(AVMDeploySnapshotAction.PARAM_CALLBACK, monitor);
                  Action action = getActionService().createAction(AVMDeploySnapshotAction.NAME, args);
                  getActionService().executeAction(action, this.websiteRef, false, true);
               }
               else if (logger.isWarnEnabled())
               {
                  logger.warn("target server '" + targetServer + "' was ignored as it no longer exists!");
               }
            }
         }
         
         // now we know the list of selected servers set the property on the attempt node
         getNodeService().setProperty(attempt, WCMAppModel.PROP_DEPLOYATTEMPTSERVERS, 
                  (Serializable)selectedDeployToNames);
         
         // set the deploymentattempid property on the store this deployment was for
         getAvmService().deleteStoreProperty(this.stagingStore, SandboxConstants.PROP_LAST_DEPLOYMENT_ID);
         getAvmService().setStoreProperty(this.stagingStore, SandboxConstants.PROP_LAST_DEPLOYMENT_ID, 
                  new PropertyValue(DataTypeDefinition.TEXT, attemptId));
         
         // close this dialog and immediately open the monitorDeployment dialog
         return "dialog:monitorDeployment";
      }
      else
      {
         return outcome;
      }
   }
   
   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }
   
   @Override
   public String getCancelButtonLabel()
   {
      return super.getCancelButtonLabel();
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean getters and setters
   
   /**
    * @param avmBrowseBean The AVM BrowseBean to set
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * @param avmService The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   protected AVMService getAvmService()
   {
      if (avmService == null)
      {
         avmService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMService();
      }
      return avmService;
   }
   
   /**
    * @param actionService The actionService to set.
    */
   public void setActionService(ActionService actionService)
   {
      this.actionService = actionService;
   }
   
   protected ActionService getActionService()
   {
      if (actionService == null)
      {
         actionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getActionService();
      }
      return actionService;
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
    * Returns the remote servers to deploy to as an array
    * 
    * @return String array of servers to deploy to
    */
   public String[] getDeployTo()
   {
      return this.deployTo;
   }
   
   /**
    * @return The NodeRef of the web project the deployment reports are being shown for
    */
   public NodeRef getWebProjectRef()
   {
      return this.webProjectRef;
   }
   
   /**
    * @return The version of the snapshot to deploy
    */
   public int getSnapshotVersion()
   {
      return this.versionToDeploy;
   }
}
