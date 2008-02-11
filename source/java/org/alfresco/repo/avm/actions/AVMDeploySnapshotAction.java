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
   public static final String PARAM_SERVER = "server";
   public static final String PARAM_ATTEMPT = "attempt";
   public static final String PARAM_CALLBACK = "callback";

   private int delay = -1;
   private int defaultAlfRmiPort = 50500;
   private int defaultReceiverRmiPort = 44100;
   private String defaultRemoteUsername = "admin";
   private String defaultRemotePassword = "admin";
   private String defaultTargetName = "default";
   private DeploymentService deployService;
   private ContentService contentService;
   private NodeService nodeService;

   private static Log logger = LogFactory.getLog(AVMDeploySnapshotAction.class);
   private static Log delayDeploymentLogger = LogFactory.getLog("alfresco.deployment.delay");
   private static final String FILE_SERVER_PREFIX = "\\\\";
   
   /**
    * Calculate the URI representation of a server from the given set of properties
    * 
    * @param props Set of properties to calculate URI from 
    */
   public static String calculateServerUri(Map<QName, Serializable> props)
   {
      StringBuilder uri = new StringBuilder();
      
      // prefix the uri if necessary
      String type = (String)props.get(WCMAppModel.PROP_DEPLOYTYPE);
      if (WCMAppModel.CONSTRAINT_FILEDEPLOY.equals(type))
      {
         uri.append(FILE_SERVER_PREFIX);
      }
      
      // append server name
      uri.append((String)props.get(WCMAppModel.PROP_DEPLOYSERVERHOST));
      
      // append port (if present)
      Integer port = (Integer)props.get(WCMAppModel.PROP_DEPLOYSERVERPORT);
      if (port != null)
      {
         uri.append(":");
         uri.append(port.toString());
      }
      
      return uri.toString();
   }
   
   /**
    * Sets the delay to use before starting the deployment
    * 
    * @param delay The delay in seconds
    */
   public void setDelay(int delay)
   {
      this.delay = delay;
   }
   
   /**
    * Sets the default RMI port for Alfresco server deployments
    * 
    * @param defaultAlfrescoRmiPort port number
    */
   public void setDefaultAlfrescoRmiPort(int defaultAlfrescoRmiPort)
   {
      this.defaultAlfRmiPort = defaultAlfrescoRmiPort;
   }

   /**
    * Sets the default RMI port for File system deployments
    * 
    * @param defaultReceiverRmiPort port number
    */
   public void setDefaultReceiverRmiPort(int defaultReceiverRmiPort)
   {
      this.defaultReceiverRmiPort = defaultReceiverRmiPort;
   }

   /**
    * Sets the default remote username to use for deployments
    * 
    * @param defaultRemoteUsername Default remote username
    */
   public void setDefaultRemoteUsername(String defaultRemoteUsername)
   {
      this.defaultRemoteUsername = defaultRemoteUsername;
   }

   /**
    * Sets the default remote password to use for deployments
    * 
    * @param defaultRemotePassword Default remote password
    */
   public void setDefaultRemotePassword(String defaultRemotePassword)
   {
      this.defaultRemotePassword = defaultRemotePassword;
   }

   /**
    * Sets the default target name to use on file system receivers
    * 
    * @param defaultTargetName Default target name
    */
   public void setDefaultTargetName(String defaultTargetName)
   {
      this.defaultTargetName = defaultTargetName;
   }

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
      paramList.add(new ParameterDefinitionImpl(PARAM_SERVER, DataTypeDefinition.NODE_REF, true,
               getParamDisplayLabel(PARAM_SERVER)));
      paramList.add(new ParameterDefinitionImpl(PARAM_ATTEMPT, DataTypeDefinition.NODE_REF, false,
               getParamDisplayLabel(PARAM_ATTEMPT)));
      paramList.add(new ParameterDefinitionImpl(PARAM_CALLBACK, DataTypeDefinition.ANY, false,
               getParamDisplayLabel(PARAM_CALLBACK)));
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
      
      // get the NodeRef representing the server to deploy to
      NodeRef serverRef = (NodeRef)action.getParameterValue(PARAM_SERVER);
      if (this.nodeService.exists(serverRef) == false)
      {
         throw new IllegalStateException("The server NodeRef (" + serverRef + 
                  ") provided does not exist!");
      }
      
      // get the NodeRef representing the deployment attempt this one is part of
      NodeRef attemptRef = (NodeRef)action.getParameterValue(PARAM_ATTEMPT);
      
      // TODO: if attempt reference is null create one now for this deployment, for now throw error
      if (this.nodeService.exists(attemptRef) == false)
      {
         throw new IllegalStateException("The attempt NodeRef (" + serverRef + 
                  ") provided does not exist!");
      }
      
      // get the callback object
      DeploymentCallback callback = (DeploymentCallback)action.getParameterValue(PARAM_CALLBACK);
      
      // get the other data from the deploymentserver object
      Map<QName, Serializable> serverProps = nodeService.getProperties(serverRef);
      String serverUri = calculateServerUri(serverProps);
      String host = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERHOST);
      Integer port = (Integer)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERPORT);
      String remoteUsername = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERUSERNAME);
      String remotePassword = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERPASSWORD);
      boolean fileServerDeployment = WCMAppModel.CONSTRAINT_FILEDEPLOY.equals(
               serverProps.get(WCMAppModel.PROP_DEPLOYTYPE));
      String sourcePath = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSOURCEPATH);
      String targetName = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERTARGET);
      String targetPath = path;
      
      // TODO: determine if we need to deploy a subfolder of the website
      
      if (fileServerDeployment == false)
      {
         // if "localhost" is passed as the target server add "live" to the end of the 
         // store name, this store will then get created automatically.
         
         // TODO: Check that the actual host name of the machine hasn't been passed

         if (port == null && (host.equalsIgnoreCase("localhost") || host.equalsIgnoreCase("127.0.0.1")))
         {
            targetPath = storePath[0] + "live:" + storePath[1];
         }
      }
      
      // get defaults for data not provided in server node
      if (port == null)
      {
         if (fileServerDeployment)
         {
            port = this.defaultReceiverRmiPort;
         }
         else
         {
            port = this.defaultAlfRmiPort;
         }
      }
      
      if (remoteUsername == null || remoteUsername.length() == 0)
      {
         remoteUsername = this.defaultRemoteUsername;
      }
      
      if (remotePassword == null || remotePassword.length() == 0)
      {
         remotePassword = this.defaultRemotePassword;
      }
      
      if (targetName == null || targetName.length() == 0)
      {
         targetName = this.defaultTargetName;
      }
      
      // take a note of the current date/time
      Date startDate = new Date();
      
      if (logger.isDebugEnabled())
         logger.debug("Starting deployment of " + actionedUponNodeRef.toString() + 
                  " to " + serverUri + " at " + startDate);
      
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
         // call the appropriate method to deploy
         if (fileServerDeployment)
         {
            if (logger.isDebugEnabled())
               logger.debug("Performing file server deployment to " + host + ":" + port);
            
            // TODO: Added new NameMatcher parameter to deploy methods. It acts as a filter.
            //       Any matching path names are ignored for deployment purposes.
            report = this.deployService.deployDifferenceFS(version, path, host, port, 
                     remoteUsername, remotePassword, targetName, null, true, false, false, callback);
         }
         else
         {
            if (logger.isDebugEnabled())
               logger.debug("Performing Alfresco deployment to " + host + ":" + port);
            
            // TODO: Added new NameMatcher parameter to deploy methods. It acts as a filter.
            //       Any matching path names are ignored for deployment purposes.
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
            logger.debug("Differences successfully applied to " + serverUri);
      }
      else
      {
         if (logger.isDebugEnabled())
            logger.debug("Failed to apply differences to " + serverUri);
      }
      
      // create the deployment report node
      createDeploymentReportNode(report, attemptRef, serverProps, version, 
               websiteRef, startDate, deployError);
   }

   /**
    * Creates a deployment report node as a child of the given website.
    * 
    * @param report The DeploymentReport result from the deploy, 
    *               will be null if the deploy failed
    * @param attempt NodeRef of the attempt the deploy was part of
    * @param serverProps The properties of the server the deploy was going to
    * @param version The version of the site bebing deployed (the snapshot)
    * @param websiteRef The NodeRef of the folder representing the website
    * @param startDate The date/time the deployment started
    * @param error The error that caused the deployment to fail, null if the 
    *              deployment was successful
    * @return The created deployment report NodeRef
    */
   private NodeRef createDeploymentReportNode(DeploymentReport report, NodeRef attempt,
            Map<QName, Serializable> serverProps, int version, NodeRef websiteRef, 
            Date startDate, Throwable error)
   {
      NodeRef reportRef = null;
      
      String serverUri = calculateServerUri(serverProps);
      Map<QName, Serializable> reportProps = new HashMap<QName, Serializable>(4, 1.0f);
      reportProps.put(WCMAppModel.PROP_DEPLOYSERVER, serverUri);
      reportProps.put(WCMAppModel.PROP_DEPLOYVERSION, version);
      reportProps.put(WCMAppModel.PROP_DEPLOYSTARTTIME, startDate);
      reportProps.put(WCMAppModel.PROP_DEPLOYENDTIME, new Date());
      reportProps.put(WCMAppModel.PROP_DEPLOYSERVERNAMEUSED, 
               serverProps.get(WCMAppModel.PROP_DEPLOYSERVERNAME));
      reportProps.put(WCMAppModel.PROP_DEPLOYSERVERUSERNAMEUSED, 
               serverProps.get(WCMAppModel.PROP_DEPLOYSERVERUSERNAME));
      reportProps.put(WCMAppModel.PROP_DEPLOYSERVERTARGETUSED, 
               serverProps.get(WCMAppModel.PROP_DEPLOYSERVERTARGET));
      reportProps.put(WCMAppModel.PROP_DEPLOYSOURCEPATHUSED, 
               serverProps.get(WCMAppModel.PROP_DEPLOYSOURCEPATH));
      reportProps.put(WCMAppModel.PROP_DEPLOYSERVERURLUSED, 
               serverProps.get(WCMAppModel.PROP_DEPLOYSERVERURL));
      
      reportProps.put(WCMAppModel.PROP_DEPLOYSUCCESSFUL, (report != null));
      if (report == null && error != null)
      {
         // add error message as fail reason if appropriate
         reportProps.put(WCMAppModel.PROP_DEPLOYFAILEDREASON, error.getMessage());
      }
      reportRef = this.nodeService.createNode(attempt, 
               WCMAppModel.ASSOC_DEPLOYMENTREPORTS, WCMAppModel.ASSOC_DEPLOYMENTREPORT, 
               WCMAppModel.TYPE_DEPLOYMENTREPORT, reportProps).getChildRef();
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
         logger.debug("Created deplyoment report node (" + reportRef + ") for server " + 
                  serverUri);
      
      return reportRef;
   }
}
