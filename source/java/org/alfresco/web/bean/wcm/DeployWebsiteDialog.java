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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.actions.AVMDeployWebsiteAction;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.wcm.sandbox.SandboxConstants;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Deploys a website to one or more remote servers.
 * 
 * @author gavinc
 */
public class DeployWebsiteDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 62702082716235924L;
   
   protected int versionToDeploy;
   protected String[] deployTo;
   protected String store;
   protected String deployMode;
   protected String calledFromTaskDialog;
   protected NodeRef websiteRef;
   protected NodeRef webProjectRef;
   protected boolean updateTestServer;
   
   protected AVMBrowseBean avmBrowseBean;
   transient private AVMService avmService;
   transient private ActionService actionService;
   
   private static final Log logger = LogFactory.getLog(DeployWebsiteDialog.class);
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // setup context for dialog
      this.deployTo = null;
      String ver = parameters.get("version");
      if (ver != null && ver.length() > 0)
      {
         this.versionToDeploy = Integer.parseInt(ver);
      }
      else
      {
         this.versionToDeploy = -1;
      }
      
      // get the store
      this.store = parameters.get("store");
      String storeRoot = AVMUtil.buildSandboxRootPath(this.store);
      this.websiteRef = AVMNodeConverter.ToNodeRef(this.versionToDeploy, storeRoot);
      
      // get the web project noderef
      String webProject = parameters.get("webproject");
      if (webProject == null)
      {
         this.webProjectRef = this.avmBrowseBean.getWebsite().getNodeRef();
      }
      else
      {
         this.webProjectRef = new NodeRef(webProject);
      }
      
      this.deployMode = (this.versionToDeploy == -1) ? 
               WCMAppModel.CONSTRAINT_TESTSERVER : WCMAppModel.CONSTRAINT_LIVESERVER;
      
      this.updateTestServer = false;
      String updateTestServerParam = parameters.get("updateTestServer");
      if (updateTestServerParam != null)
      {
         this.updateTestServer = Boolean.parseBoolean(updateTestServerParam);
      }
      
      this.calledFromTaskDialog = parameters.get("calledFromTaskDialog");
      this.avmBrowseBean.getDeploymentMonitorIds().clear();
      
      if (logger.isDebugEnabled())
         logger.debug("Initialising dialog to deploy: " + this.websiteRef.toString());
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      if (logger.isDebugEnabled())
         logger.debug("Requesting deployment of: " + this.websiteRef.toString());
      
      if (this.deployTo != null && this.deployTo.length > 0)
      {
         // get the unprotected NodeService and PermissionService for this dialog otherwise
         // 'AddChildren' permission would be required on the web project node for all users
         WebApplicationContext wac = FacesContextUtils.getRequiredWebApplicationContext(context);
         NodeService unprotectedNodeService = (NodeService)wac.getBean("nodeService");
         PermissionService unprotectedPermissionService = (PermissionService)wac.getBean("permissionService");
         
         List<String> selectedDeployToNames = new ArrayList<String>();
         
         // create a deploymentattempt node to represent this deployment
         String attemptId = GUID.generate();
         Map<QName, Serializable> props = new HashMap<QName, Serializable>(8, 1.0f);
         props.put(WCMAppModel.PROP_DEPLOYATTEMPTID, attemptId);
         props.put(WCMAppModel.PROP_DEPLOYATTEMPTTYPE, this.deployMode);
         props.put(WCMAppModel.PROP_DEPLOYATTEMPTSTORE, this.store);
         props.put(WCMAppModel.PROP_DEPLOYATTEMPTVERSION, this.versionToDeploy);
         props.put(WCMAppModel.PROP_DEPLOYATTEMPTTIME, new Date());
         NodeRef attempt = unprotectedNodeService.createNode(this.webProjectRef, 
               WCMAppModel.ASSOC_DEPLOYMENTATTEMPT, WCMAppModel.ASSOC_DEPLOYMENTATTEMPT, 
               WCMAppModel.TYPE_DEPLOYMENTATTEMPT, props).getChildRef();
         
         // allow anyone to add child nodes to the deploymentattempt node
         unprotectedPermissionService.setPermission(attempt, PermissionService.ALL_AUTHORITIES, 
                  PermissionService.ADD_CHILDREN, true);
         
         // execute a deploy action for each of the selected remote servers asynchronously
         for (String targetServer : this.deployTo)
         {
            if (targetServer.length() > 0)
            {
               NodeRef serverRef = new NodeRef(targetServer);
               if (unprotectedNodeService.exists(serverRef))
               {
                  // get all properties of the target server
                  Map<QName, Serializable> serverProps = unprotectedNodeService.getProperties(serverRef);
                  
                  String url = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERURL);
                  String serverUri = AVMDeployWebsiteAction.calculateServerUri(serverProps);
                  String serverName = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERNAME);
                  if (serverName == null || serverName.length() == 0)
                  {
                     serverName = serverUri;
                  }
                  
                  // if this is a test server deployment we need to allocate the
                  // test server to the current sandbox so it can re-use it and
                  // more importantly, no one else can. Before doing that however,
                  // we need to make sure no one else has taken the server since
                  // we selected it.
                  if (WCMAppModel.CONSTRAINT_TESTSERVER.equals(this.deployMode) &&
                      this.updateTestServer == false)
                  {
                     String allocatedTo = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERALLOCATEDTO);
                     if (allocatedTo != null)
                     {
                        throw new AlfrescoRuntimeException("testserver.taken", new Object[] {serverName});
                     }
                     else
                     {
                        unprotectedNodeService.setProperty(serverRef, WCMAppModel.PROP_DEPLOYSERVERALLOCATEDTO, 
                                 this.store);
                     }
                  }
                  
                  if (logger.isDebugEnabled())
                     logger.debug("Issuing deployment request for: " + serverName);
                  
                  // remember the servers deployed to
                  selectedDeployToNames.add(serverName);
                  
                  // create a deployment monitor object, store in the session, 
                  // add to avm browse bean and pass to action
                  DeploymentMonitor monitor = new DeploymentMonitor(
                           this.websiteRef, serverRef, versionToDeploy, serverName, attemptId, url);
                  context.getExternalContext().getSessionMap().put(monitor.getId(), monitor);
                  this.avmBrowseBean.getDeploymentMonitorIds().add(monitor.getId());
                  
                  // create and execute the action asynchronously
                  Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
                  args.put(AVMDeployWebsiteAction.PARAM_WEBPROJECT, webProjectRef);
                  args.put(AVMDeployWebsiteAction.PARAM_SERVER, serverRef);
                  args.put(AVMDeployWebsiteAction.PARAM_ATTEMPT, attempt);
                  args.put(AVMDeployWebsiteAction.PARAM_CALLBACK, monitor);
                  Action action = getActionService().createAction(AVMDeployWebsiteAction.NAME, args);
                  getActionService().executeAction(action, this.websiteRef, false, true);
               }
               else if (logger.isWarnEnabled())
               {
                  logger.warn("target server '" + targetServer + "' was ignored as it no longer exists!");
               }
            }
         }
         
         // now we know the list of selected servers set the property on the attempt node
         unprotectedNodeService.setProperty(attempt, WCMAppModel.PROP_DEPLOYATTEMPTSERVERS, 
                  (Serializable)selectedDeployToNames);
         
         // set the deploymentattempid property on the store this deployment was for
         getAvmService().deleteStoreProperty(this.store, SandboxConstants.PROP_LAST_DEPLOYMENT_ID);
         getAvmService().setStoreProperty(this.store, SandboxConstants.PROP_LAST_DEPLOYMENT_ID, 
                  new PropertyValue(DataTypeDefinition.TEXT, attemptId));
         
         // close this dialog and immediately open the monitorDeployment dialog
         Map<String, String> params = new HashMap<String, String>(2);
         params.put("webproject", this.webProjectRef.toString());
         params.put("calledFromTaskDialog", this.calledFromTaskDialog);
         Application.getDialogManager().setupParameters(params);
         return "dialog:monitorDeployment";
      }
      else
      {
         if (logger.isWarnEnabled())
            logger.warn("Deployment of '" + this.websiteRef.toString() + "' skipped as no servers were selected");
         
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
   
   @Override
   public String getContainerDescription()
   {
      String desc = null;
      
      FacesContext context = FacesContext.getCurrentInstance();
      ResourceBundle bundle = Application.getBundle(context);
      
      if (WCMAppModel.CONSTRAINT_LIVESERVER.equals(this.deployMode))
      {
         desc = bundle.getString("deploy_snapshot_desc");
      }
      else
      {
         if (this.updateTestServer)
         {
            desc = bundle.getString("redeploy_sandbox_desc");
         }
         else
         {
            desc = bundle.getString("deploy_sandbox_desc");
         }
      }
      
      return desc;
   }

   @Override
   public String getContainerTitle()
   {
      String title = null;
      
      FacesContext context = FacesContext.getCurrentInstance();
      ResourceBundle bundle = Application.getBundle(context);
      
      if (WCMAppModel.CONSTRAINT_LIVESERVER.equals(this.deployMode))
      {
         title = bundle.getString("deploy_snapshot_title");
      }
      else
      {
         if (this.updateTestServer)
         {
            title = bundle.getString("redeploy_sandbox_title");
         }
         else
         {
            title = bundle.getString("deploy_sandbox_title");
         }
      }
      
      return title;
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
    * Returns the type of server to deploy to, either 'live' or 'test'.
    * 
    * @return The type of server to deploy to
    */
   public String getDeployMode()
   {
      return this.deployMode;
   }
   
   /**
    * @return The NodeRef of the web project the deployment reports are being shown for
    */
   public NodeRef getWebProjectRef()
   {
      return this.webProjectRef;
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
