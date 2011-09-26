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
package org.alfresco.repo.avm.wf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.config.JNDIConstants;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.actions.AVMDeployWebsiteAction;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.workflow.jbpm.JBPMNode;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.avm.deploy.DeploymentService;
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
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;


/**
 * Deploys the latest snapshot of the staging area the submission was for.
 * 
 * @author Gavin Cornwell
 */
public class AVMDeployHandler extends JBPMSpringActionHandler 
{
    private DeploymentService deploymentService;
    private AVMService avmService;
    private ActionService actionService;
    private NodeService unprotectedNodeService;
    private PermissionService unprotectedPermissionService;
    
    private static final String BEAN_DEPLOYMENT_SERVICE = "deploymentService";
    private static final String BEAN_AVM_SERVICE = "AVMService";
    private static final String BEAN_ACTION_SERVICE = "actionService";
    private static final String BEAN_NODE_SERVICE = "nodeService";
    private static final String BEAN_PERMISSION_SERVICE = "permissionService";

    private static final long serialVersionUID = 5590265401983087178L;
    private static final Log logger = LogFactory.getLog(AVMDeployHandler.class);
    
    /**
     * Initialize service references.
     * @param factory The BeanFactory to get references from.
     */
    @Override
    protected void initialiseHandler(BeanFactory factory) 
    {
        this.deploymentService = (DeploymentService)factory.getBean(BEAN_DEPLOYMENT_SERVICE);
        this.avmService = (AVMService)factory.getBean(BEAN_AVM_SERVICE);
        this.actionService = (ActionService)factory.getBean(BEAN_ACTION_SERVICE);
        this.unprotectedNodeService = (NodeService)factory.getBean(BEAN_NODE_SERVICE);
        this.unprotectedPermissionService = (PermissionService)factory.getBean(BEAN_PERMISSION_SERVICE);
    }

    /**
     * Do the actual work.
     * @param executionContext The context to get stuff from.
     */
    public void execute(ExecutionContext executionContext) throws Exception 
    {
        // determine if the auto deploy needs to be executed
        Boolean autoDeploy = (Boolean)executionContext.getContextInstance().getVariable("wcmwf_autoDeploy");
           
        if (logger.isDebugEnabled())
        {
            String label = (String)executionContext.getContextInstance().getVariable("wcmwf_label");
            long workflowId = executionContext.getProcessInstance().getId();
            
            logger.debug("autoDeploy state for submission (workflowid: jbpm$" + workflowId + 
                     ", label: " + label + ") is: " + autoDeploy);
        }
        
        if (autoDeploy != null && autoDeploy.booleanValue())
        {
            // get the web project node for the submission
            JBPMNode webProjNode = (JBPMNode)executionContext.getContextInstance().getVariable("wcmwf_webproject");
            NodeRef webProjectRef = webProjNode.getNodeRef();

            // get the list of live servers for the project that have the auto deploy flag turned on
            List<NodeRef> servers = deploymentService.findLiveDeploymentServers(webProjectRef);
           
            // if there are servers do the deploy
            if (servers.size() > 0)
            {
                // Get the staging store name
                NodeRef pkg = ((JBPMNode)executionContext.getContextInstance().getVariable("bpm_package")).getNodeRef();
                Pair<Integer, String> pkgPath = AVMNodeConverter.ToAVMVersionPath(pkg);
                String [] workflowStorePath = pkgPath.getSecond().split(":");
                String workflowStoreName = workflowStorePath[0];
                PropertyValue propVal = this.avmService.getStoreProperty(workflowStoreName, 
                         SandboxConstants.PROP_WEBSITE_NAME);
                String store = propVal.getStringValue();
        
                if (logger.isDebugEnabled())
                     logger.debug("Attempting auto deploy to store: " + store);
            
                // retrieve the latest snapshot number for the store
                int snapshotVersionToDeploy = this.avmService.getLatestSnapshotID(store);
                
                // work out the path of the store that needs deploying
                String pathToDeploy = store + ":/" + JNDIConstants.DIR_DEFAULT_WWW + 
                                      '/' +  JNDIConstants.DIR_DEFAULT_APPBASE;
                NodeRef websiteRef = AVMNodeConverter.ToNodeRef(snapshotVersionToDeploy, pathToDeploy);
               
                // create a deploymentattempt node to represent this deployment
                String attemptId = GUID.generate();
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(8, 1.0f);
                props.put(WCMAppModel.PROP_DEPLOYATTEMPTID, attemptId);
                props.put(WCMAppModel.PROP_DEPLOYATTEMPTTYPE, WCMAppModel.CONSTRAINT_LIVESERVER);
                props.put(WCMAppModel.PROP_DEPLOYATTEMPTSTORE, store);
                props.put(WCMAppModel.PROP_DEPLOYATTEMPTVERSION, snapshotVersionToDeploy);
                props.put(WCMAppModel.PROP_DEPLOYATTEMPTTIME, new Date());
                NodeRef attempt = unprotectedNodeService.createNode(webProjectRef, 
                         WCMAppModel.ASSOC_DEPLOYMENTATTEMPT, WCMAppModel.ASSOC_DEPLOYMENTATTEMPT, 
                         WCMAppModel.TYPE_DEPLOYMENTATTEMPT, props).getChildRef();
               
                // allow anyone to add child nodes to the deploymentattempt node
                unprotectedPermissionService.setPermission(attempt, PermissionService.ALL_AUTHORITIES, 
                         PermissionService.ADD_CHILDREN, true);
                
                // iterate round each server and fire off a deplyoment action
                List<String> selectedDeployToNames = new ArrayList<String>();
                for (NodeRef serverRef: servers)
                {
                    if (unprotectedNodeService.exists(serverRef))
                    {
                        // get all properties of the target server
                        Map<QName, Serializable> serverProps = unprotectedNodeService.getProperties(serverRef);
                        
                        String serverUri = AVMDeployWebsiteAction.calculateServerUri(serverProps);
                        String serverName = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERNAME);
                        if (serverName == null || serverName.length() == 0)
                        {
                           serverName = serverUri;
                        }
                        
                        // remember the servers deployed to
                        selectedDeployToNames.add(serverName);
                        
                        if (logger.isDebugEnabled())
                           logger.debug("Auto deploying '" + websiteRef.toString() + "' to server: " + serverName);
                        
                        // create and execute the action asynchronously
                        Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
                        args.put(AVMDeployWebsiteAction.PARAM_WEBPROJECT, webProjectRef);
                        args.put(AVMDeployWebsiteAction.PARAM_SERVER, serverRef);
                        args.put(AVMDeployWebsiteAction.PARAM_ATTEMPT, attempt);
                        Action action = this.actionService.createAction(AVMDeployWebsiteAction.NAME, args);
                        this.actionService.executeAction(action, websiteRef, false, true);
                    }
                }
                
                // now we know the list of selected servers set the property on the attempt node
                unprotectedNodeService.setProperty(attempt, WCMAppModel.PROP_DEPLOYATTEMPTSERVERS, 
                         (Serializable)selectedDeployToNames);
                
                // set the deploymentattempid property on the store this deployment was for
                this.avmService.deleteStoreProperty(store, SandboxConstants.PROP_LAST_DEPLOYMENT_ID);
                this.avmService.setStoreProperty(store, SandboxConstants.PROP_LAST_DEPLOYMENT_ID, 
                         new PropertyValue(DataTypeDefinition.TEXT, attemptId));
            }
        }
    }
}
