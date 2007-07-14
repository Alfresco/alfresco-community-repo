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

package org.alfresco.repo.avm.actions;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.avm.deploy.DeploymentCallback;
import org.alfresco.service.cmr.avm.deploy.DeploymentEvent;
import org.alfresco.service.cmr.avm.deploy.DeploymentReport;
import org.alfresco.service.cmr.avm.deploy.DeploymentService;
import org.alfresco.service.cmr.avmsync.AVMSyncException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Deploys a website snapshot to a remote server.
 * 
 * @author gavinc
 */
public class AVMDeploySnapshotAction extends ActionExecuterAbstractBase
{
   public static final String NAME = "avm-deploy-snapshot";

   public static final String PARAM_WEBSITE = "website";
   public static final String PARAM_TARGET_SERVER = "target-server";
   public static final String PARAM_DEFAULT_RMI_PORT = "default-rmi-port";
   public static final String PARAM_DEFAULT_RECEIVER_RMI_PORT = "default-receiver-rmi-port";
   public static final String PARAM_REMOTE_USERNAME = "remote-username";
   public static final String PARAM_REMOTE_PASSWORD = "remote-password";
   public static final String PARAM_CALLBACK = "deploy-callback";
   public static final String PARAM_DELAY = "delay";

   private DeploymentService deployService;
   private ContentService contentService;
   private NodeService nodeService;

   private static Log logger = LogFactory.getLog(AVMDeploySnapshotAction.class);
   private static Log delayDeploymentLogger = LogFactory.getLog("alfresco.deployment.delay");
   private static final String FILE_SERVER_PREFIX = "\\\\";
   
   /**
    * @param service The NodeService instance
    */
   public void setNodeService(NodeService service)
   {
      this.nodeService = service;
   }
   
   /**
    * @param contentService The ContentService instance
    */
   public void setContentService(ContentService contentService)
   {
      this.contentService = contentService;
   }

   /**
    * @param service The AVM Deployment Service instance
    */
   public void setDeploymentService(DeploymentService service)
   {
      deployService = service;
   }

   @Override
   protected void addParameterDefinitions(List<ParameterDefinition> paramList)
   {
      paramList.add(new ParameterDefinitionImpl(PARAM_WEBSITE, DataTypeDefinition.NODE_REF, true,
               getParamDisplayLabel(PARAM_WEBSITE)));
      paramList.add(new ParameterDefinitionImpl(PARAM_TARGET_SERVER, DataTypeDefinition.TEXT, true,
               getParamDisplayLabel(PARAM_TARGET_SERVER)));
      paramList.add(new ParameterDefinitionImpl(PARAM_DEFAULT_RMI_PORT, DataTypeDefinition.INT, true,
               getParamDisplayLabel(PARAM_DEFAULT_RMI_PORT)));
      paramList.add(new ParameterDefinitionImpl(PARAM_DEFAULT_RECEIVER_RMI_PORT, DataTypeDefinition.INT, true,
               getParamDisplayLabel(PARAM_DEFAULT_RECEIVER_RMI_PORT)));
      paramList.add(new ParameterDefinitionImpl(PARAM_REMOTE_USERNAME, DataTypeDefinition.TEXT, true,
               getParamDisplayLabel(PARAM_REMOTE_USERNAME)));
      paramList.add(new ParameterDefinitionImpl(PARAM_REMOTE_PASSWORD, DataTypeDefinition.TEXT, true,
               getParamDisplayLabel(PARAM_REMOTE_PASSWORD)));
      paramList.add(new ParameterDefinitionImpl(PARAM_CALLBACK, DataTypeDefinition.ANY, false,
               getParamDisplayLabel(PARAM_CALLBACK)));
      paramList.add(new ParameterDefinitionImpl(PARAM_DELAY, DataTypeDefinition.INT, false,
               getParamDisplayLabel(PARAM_DELAY)));
   }
   
   @Override
   protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
   {
      // the actionedUponNodeRef is the path of the sandbox to deploy
      Pair<Integer, String> avmVersionPath = AVMNodeConverter.ToAVMVersionPath(actionedUponNodeRef);
      int version = avmVersionPath.getFirst();
      String path = avmVersionPath.getSecond();
      
      // get store name and path parts.
      String [] storePath = path.split(":");
      if (storePath.length != 2)
      {
         throw new AVMSyncException("Malformed source path: " + path);
      }
      
      // get the NodeRef representing the website being deployed
      NodeRef websiteRef = (NodeRef)action.getParameterValue(PARAM_WEBSITE);
      if (this.nodeService.exists(websiteRef) == false)
      {
         throw new IllegalStateException("The website NodeRef (" + websiteRef + 
                  ") provided does not exist!");
      }
      
      // get the callback object
      DeploymentCallback callback = (DeploymentCallback)action.getParameterValue(PARAM_CALLBACK);
      
      // get the remote machine
      String targetServer = (String)action.getParameterValue(PARAM_TARGET_SERVER);
      String remoteUsername = (String)action.getParameterValue(PARAM_REMOTE_USERNAME);
      String remotePassword = (String)action.getParameterValue(PARAM_REMOTE_PASSWORD);
      int defaultAlfRmiPort = (Integer)action.getParameterValue(PARAM_DEFAULT_RMI_PORT);
      int defaultReceiverRmiPort = (Integer)action.getParameterValue(PARAM_DEFAULT_RECEIVER_RMI_PORT);
      int delay = -1;
      if (action.getParameterValue(PARAM_DELAY) != null)
      {
         delay = (Integer)action.getParameterValue(PARAM_DELAY);
      }
      
      // determine whether this is a file server or Alfresco server deployment
      boolean fileServerDeployment = false;
      if (targetServer.startsWith(FILE_SERVER_PREFIX))
      {
         fileServerDeployment = true;
      }
      
      // if "localhost" is passed as the target server add "live" to the end of the 
      // store name, this store will then get created automatically.
      String targetPath = null;
      if (targetServer.equalsIgnoreCase("localhost") || targetServer.equalsIgnoreCase("127.0.0.1"))
      {
         targetPath = storePath[0] + "live:" + storePath[1];
      }
      else
      {
         // TODO: Check that the actual host name of the machine hasn't been passed
         
         targetPath = path;
      }
      
      // take a note of the current date/time
      Date startDate = new Date();
      
      if (logger.isDebugEnabled())
         logger.debug("Starting deployment of " + actionedUponNodeRef.toString() + 
                  " to " + targetServer + " at " + startDate);
      
      if (delayDeploymentLogger.isDebugEnabled() && delay > 0)
      {
         delayDeploymentLogger.debug("Delaying deployment by " + delay + "s...");
         
         // add a delay for testing purposes if the delay logger level is debug 
         try { Thread.sleep(1000*delay); } catch (Throwable e) {}
      }
      
      // make the deploy call passing in the DeploymentCallback, if present
      Throwable deployError = null;
      DeploymentReport report = null;
      try
      {
         String host = targetServer;
         int port = defaultAlfRmiPort;
         if (fileServerDeployment)
         {
            port = defaultReceiverRmiPort;
         }
         
         // check whether there is a port number present, if so, use it
         int idx = targetServer.indexOf(":");
         if (idx != -1)
         {
            host = targetServer.substring(0, idx);
            String strPort = targetServer.substring(idx+1);
            port = Integer.parseInt(strPort);
         }
         
         // TODO: we need to capture username/password for the remote server at some
         //       point, for now we use the configured username/password for all servers
         
         // call the appropriate method to deploy
         if (fileServerDeployment)
         {
            // remove the prefixed \\
            host = targetServer.substring(FILE_SERVER_PREFIX.length());
            
            if (logger.isDebugEnabled())
               logger.debug("Performing file server deployment to " + host + ":" + port);
            
            // TODO Added new NameMatcher parameter to deploy methods. It acts as a filter.
            // Any matching path names are ignored for deployment purposes.
            report = this.deployService.deployDifferenceFS(version, path, host, port, 
                     remoteUsername, remotePassword, "default", null, true, false, false, callback);
         }
         else
         {
            if (logger.isDebugEnabled())
               logger.debug("Performing Alfresco deployment to " + host + ":" + port);
            
            // TODO Added new NameMatcher parameter to deploy methods. It acts as a filter.
            // Any matching path names are ignored for deployment purposes.
            report = this.deployService.deployDifference(version, path, host, port, 
                     remoteUsername, remotePassword, targetPath, null, true, false, false, callback);
         }
      }
      catch (Throwable err)
      {
         deployError = err;
         logger.error(deployError);
         
         // report the error to the callback object
         // TODO: See if this can be incorporated into the DeploymentCallback I/F
         if (callback != null)
         {
            // to avoid a circular dependency use reflection to call the method
            try
            {
               Method method = callback.getClass().getMethod("errorOccurred", new Class[] {Throwable.class});
               if (method != null)
               {
                  method.invoke(callback, new Object[] {err});
               }
            }
            catch (Throwable e)
            {
               logger.warn("Failed to inform deployment monitor of deployment failure", e);
            }
         }
      }

      if (report != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Differences successfully applied to " + targetServer);
      }
      else
      {
         if (logger.isDebugEnabled())
            logger.debug("Failed to apply differences to " + targetServer);
      }
      
      // create the deployment report node
      createDeploymentReportNode(report, targetServer, version, websiteRef, startDate, deployError);
   }

   /**
    * Creates a deployment report node as a child of the given website.
    * 
    * @param report The DeploymentReport result from the deploy, 
    *               will be null if the deploy failed
    * @param targetServer The server the deploy was going to
    * @param version The version of the site bebing deployed (the snapshot)
    * @param websiteRef The NodeRef of the folder representing the website
    * @param startDate The date/time the deployment started
    * @param error The error that caused the deployment to fail, null if the 
    *              deployment was successful
    * @return The created deployment report NodeRef
    */
   private NodeRef createDeploymentReportNode(DeploymentReport report, String targetServer,
            int version, NodeRef websiteRef, Date startDate, Throwable error)
   {
      NodeRef reportRef = null;
      
      // remove illegal chars from the target server name to create the report name
      String reportName = targetServer.replace(':', '_').replace('\\', '_') + 
            " deployment report.txt";
      
      Map<QName, Serializable> props = new HashMap<QName, Serializable>(4, 1.0f);
      props.put(ContentModel.PROP_NAME, reportName);
      props.put(WCMAppModel.PROP_DEPLOYSERVER, targetServer);
      props.put(WCMAppModel.PROP_DEPLOYVERSION, version);
      props.put(WCMAppModel.PROP_DEPLOYSTARTTIME, startDate);
      props.put(WCMAppModel.PROP_DEPLOYENDTIME, new Date());
      props.put(WCMAppModel.PROP_DEPLOYSUCCESSFUL, (report != null));
      if (report == null && error != null)
      {
         // add error message as fail reason if appropriate
         props.put(WCMAppModel.PROP_DEPLOYFAILEDREASON, error.getMessage());
      }
      reportRef = this.nodeService.createNode(websiteRef, 
               WCMAppModel.ASSOC_DEPLOYMENTREPORT, WCMAppModel.ASSOC_DEPLOYMENTREPORT, 
               WCMAppModel.TYPE_DEPLOYMENTREPORT, props).getChildRef();
      ContentWriter writer = contentService.getWriter(reportRef, ContentModel.PROP_CONTENT, true);
      writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
      writer.setEncoding("UTF-8");
      
      if (report == null)
      {
         if (error == null)
         {
            writer.putContent("");
         }
         else
         {
            // add the full stack trace of the error as the content
            StringWriter stack = new StringWriter();
            PrintWriter stackPrint = new PrintWriter(stack);
            error.printStackTrace(stackPrint);
            writer.putContent(stack.toString());
         }
      }
      else
      {
         // TODO: revisit this, is it better to stream to a temp file?
         StringBuilder builder = new StringBuilder();
         for (DeploymentEvent event : report)
         {
            builder.append(event.getType());
            builder.append(" ");
            builder.append(event.getDestination());
            builder.append("\r\n");
         }
         
         writer.putContent(builder.toString());
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Created deplyoment report node (" + reportRef + ") for targetServer " + 
                  targetServer);
      
      return reportRef;
   }
}
