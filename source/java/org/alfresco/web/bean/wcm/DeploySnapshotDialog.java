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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.actions.AVMDeploySnapshotAction;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Deploys a web project snapshot to one or more remote servers.
 * 
 * @author gavinc
 */
public class DeploySnapshotDialog extends BaseDialogBean
{
   protected int versionToDeploy;
   protected String[] deployTo;
   protected NodeRef websiteRef;
   protected NodeRef webProjectRef;
   
   protected AVMBrowseBean avmBrowseBean;
   protected ActionService actionService;
   
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
      String stagingStore = AVMUtil.buildSandboxRootPath(this.avmBrowseBean.getStagingStore());
      this.websiteRef = AVMNodeConverter.ToNodeRef(this.versionToDeploy, stagingStore);
      
      if (logger.isDebugEnabled())
         logger.debug("Initialising dialog to deploy version " + this.versionToDeploy + 
                  " of " + this.websiteRef.toString());
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // put all the selected servers to deploy to into a list
      List<String> selectedDeployTo = new ArrayList<String>();
      if (this.deployTo != null)
      {
         for (int x = 0; x < this.deployTo.length; x++)
         {
            selectedDeployTo.add(this.deployTo[x].trim());
         }
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Executing deployment of " + this.websiteRef.toString() + 
                  " to servers: " + selectedDeployTo);
      
      if (selectedDeployTo.size() > 0)
      {
         // TODO: move any existing deployment reports to a node representing the
         //       snapshot, we can then keep a history of deployment attempts on a 
         //       per snapshot basis.
         
         NodeRef webProjectRef = this.avmBrowseBean.getWebsite().getNodeRef();
         List<String> allDeployToServers = (List<String>)nodeService.getProperty(webProjectRef, 
                     WCMAppModel.PROP_DEPLOYTO);
         List<ChildAssociationRef> deployReportRefs = nodeService.getChildAssocs(
                  webProjectRef, WCMAppModel.ASSOC_DEPLOYMENTREPORT, RegexQNamePattern.MATCH_ALL);
         for (ChildAssociationRef ref : deployReportRefs)
         {
            NodeRef report = ref.getChildRef();
            
            String server = (String)this.nodeService.getProperty(report, WCMAppModel.PROP_DEPLOYSERVER);
            int version = (Integer)this.nodeService.getProperty(report, WCMAppModel.PROP_DEPLOYVERSION);
            if ((version == this.versionToDeploy && selectedDeployTo.contains(server)) ||
                (version != this.versionToDeploy) || (allDeployToServers.contains(server) == false))
            {
               // cascade delete will take care of child-child relationships
               this.nodeService.removeChild(webProjectRef, report);
            
               if (logger.isDebugEnabled())
                  logger.debug("Removed exisiting deployment report for server: " + server);
            }
         }
         
         // update the selecteddeployto property with list of servers selected
         this.nodeService.setProperty(webProjectRef, WCMAppModel.PROP_SELECTEDDEPLOYTO, 
                  (Serializable)selectedDeployTo);
         // update the selecteddeployversion property with the selected snapshot version
         this.nodeService.setProperty(webProjectRef, WCMAppModel.PROP_SELECTEDDEPLOYVERSION, 
                  this.versionToDeploy);
         
         // execute a deploy action for each of the selected remote servers asynchronously
         for (String targetServer : selectedDeployTo)
         {
            if (targetServer.length() > 0)
            {
               // create a deployment monitor object, store in the session, 
               // add to avm browse bean and pass to action
               DeploymentMonitor monitor = new DeploymentMonitor(
                        this.websiteRef, targetServer, versionToDeploy);
               context.getExternalContext().getSessionMap().put(monitor.getId(), monitor);
               this.avmBrowseBean.getDeploymentMonitorIds().add(monitor.getId());
               
               // create the action
               Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
               args.put(AVMDeploySnapshotAction.PARAM_WEBSITE, webProjectRef);
               args.put(AVMDeploySnapshotAction.PARAM_TARGET_SERVER, targetServer);
               args.put(AVMDeploySnapshotAction.PARAM_DEFAULT_RMI_PORT, 
                        AVMUtil.getRemoteRMIRegistryPort());
               args.put(AVMDeploySnapshotAction.PARAM_DEFAULT_RECEIVER_RMI_PORT, 
                        AVMUtil.getRemoteReceiverRMIPort());
               args.put(AVMDeploySnapshotAction.PARAM_REMOTE_USERNAME, 
                        AVMUtil.getRemoteDeploymentUsername());
               args.put(AVMDeploySnapshotAction.PARAM_REMOTE_PASSWORD, 
                        AVMUtil.getRemoteDeploymentPassword());
               args.put(AVMDeploySnapshotAction.PARAM_CALLBACK, monitor);
               args.put(AVMDeploySnapshotAction.PARAM_DELAY,
                        AVMUtil.getRemoteDeploymentDelay());
               Action action = this.actionService.createAction(AVMDeploySnapshotAction.NAME, args);
               this.actionService.executeAction(action, this.websiteRef, false, true);
            }
         }
         
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
    * @param avmBrowseBean    The AVM BrowseBean to set
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * @param actionService The actionService to set.
    */
   public void setActionService(ActionService actionService)
   {
      this.actionService = actionService;
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
