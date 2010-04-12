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

package org.alfresco.repo.avm.actions;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
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
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.avm.deploy.DeploymentCallback;
import org.alfresco.service.cmr.avm.deploy.DeploymentEvent;
import org.alfresco.service.cmr.avm.deploy.DeploymentReport;
import org.alfresco.service.cmr.avm.deploy.DeploymentReportCallback;
import org.alfresco.service.cmr.avm.deploy.DeploymentService;
import org.alfresco.service.cmr.avmsync.AVMSyncException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.alfresco.util.RegexNameMatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Deploys a website to a remote server.
 *
 * TODO refactor and add to WCM services (when we support WCM deployment config)
 *
 * @author gavinc
 */
public class AVMDeployWebsiteAction extends ActionExecuterAbstractBase
{
   public static final String NAME = "avm-deploy-website";
   public static final String FILE_SERVER_PREFIX = "\\\\";

   public static final String PARAM_WEBPROJECT = "webproject";
   public static final String PARAM_SERVER = "server";
   public static final String PARAM_ATTEMPT = "attempt";
   public static final String PARAM_CALLBACK = "callback";
   
   public static final String ASYNC_QUEUE_NAME = "deployment";
   
   public static final String LIVE_SUFFIX = "live";

   private int delay = -1;
   private int defaultAlfRmiPort = 50500;
   private int defaultReceiverRmiPort = 44100;
   private String defaultRemoteUsername = "admin";
   private String defaultRemotePassword = "admin";
   private String defaultTargetName = "default";
   private String defaultAdapterName = "default";
   private List<DeploymentCallback> configuredCallbacks;
   private DeploymentService deployService;
   private ContentService contentService;
   private NodeService nodeService;
   private TransactionService transactionService;

   private static Log logger = LogFactory.getLog(AVMDeployWebsiteAction.class);
   private static Log delayDeploymentLogger = LogFactory.getLog("alfresco.deployment.delay");

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
   
   public void setCallbacks(List<DeploymentCallback> callbacks)
   {
      this.configuredCallbacks = callbacks;
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
      paramList.add(new ParameterDefinitionImpl(PARAM_WEBPROJECT, DataTypeDefinition.NODE_REF, true,
               getParamDisplayLabel(PARAM_WEBPROJECT)));
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
      NodeRef websiteRef = (NodeRef)action.getParameterValue(PARAM_WEBPROJECT);
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
      String excludes = (String)serverProps.get(WCMAppModel.PROP_DEPLOYEXCLUDES);
      String targetName = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERTARGET);
      String adapterName = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERADPTERNAME);
      String targetPath = path;

      if (fileServerDeployment == false)
      {
         // if "localhost" is passed as the target server add "live" to the end of the
         // store name, this store will then get created automatically.

         // TODO: Check that the actual host name of the machine hasn't been passed

         if (port == null && (host.equalsIgnoreCase("localhost") || host.equalsIgnoreCase("127.0.0.1")))
         {
            targetPath = storePath[0] + LIVE_SUFFIX + ":" + storePath[1];
         }
      }
      else 
      {
    	  if (adapterName == null) {
    		  adapterName = defaultAdapterName;
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

      // determine the actual source path
      if (sourcePath != null && sourcePath.length() > 0)
      {
         // make sure the path starts with /
         if (sourcePath.startsWith("/") == false)
         {
            sourcePath = "/" + sourcePath;
         }

         // append to the root path
         path = path + sourcePath;
      }

      // determine if a NameMatcher is required (if an exclude is present)
      RegexNameMatcher regexMatcher = null;
      if (excludes != null && excludes.length() > 0)
      {
         regexMatcher = new RegexNameMatcher();
         List<String> patterns = new ArrayList<String>(1);
         patterns.add(excludes);
         regexMatcher.setPatterns(patterns);
      }
      
      // create a list of all the callback objects
      List<DeploymentCallback> callbacks = new ArrayList<DeploymentCallback>();
      if (callback != null)
      {
         // if present add the callback passed as a parameter (usually for UI purposes)
         callbacks.add(callback);
      }
      if (this.configuredCallbacks != null && this.configuredCallbacks.size() > 0)
      {
         // add the configured callbacks
         callbacks.addAll(this.configuredCallbacks);
      }
      
      // take a note of the current date/time
      Date startDate = new Date();

      if (logger.isDebugEnabled())
         logger.debug("Starting deployment of " + actionedUponNodeRef.toString() +
                  " to " + serverUri + " at " + startDate);

//      if (delayDeploymentLogger.isDebugEnabled() && delay > 0)
//      {
//         delayDeploymentLogger.debug("Delaying deployment by " + delay + "s...");
//
//         // add a delay for testing purposes if the delay logger level is debug
//         try { Thread.sleep(1000*delay); } catch (Throwable e) {}
//      }

      // make the deploy call passing in the DeploymentCallback, if present
      Throwable deployError = null;
      
      DeploymentReport report = new DeploymentReport();
      callbacks.add(new DeploymentReportCallback(report));
      
      try
      {
         // overwrite the password before logging
         serverProps.put(WCMAppModel.PROP_DEPLOYSERVERPASSWORD, "*****");
               
         // call the appropriate method to deploy
         if (fileServerDeployment)
         {
            if (logger.isDebugEnabled())
               logger.debug("Performing file server deployment to " + host + ":" + port +
                            " using deploymentserver: " + serverProps);

            this.deployService.deployDifferenceFS(version, 
            		path, 
            		adapterName,
            		host, 
            		port,
                    remoteUsername, 
                    remotePassword, 
                    targetName, 
                    regexMatcher, 
                    true, 
                    false, 
                    false, 
                    callbacks);
         }
         else
         {
            if (logger.isDebugEnabled())
               logger.debug("Performing Alfresco deployment to " + host + ":" + port +
                            " using deploymentserver: " + serverProps);

            this.deployService.deployDifference(version, 
            		path, 
            		host, 
            		port,
            		remoteUsername, 
            		remotePassword, 
            		targetPath, 
            		regexMatcher, 
            		true, 
            		false, 
            		false, 
            		callbacks);
         }
      }
      catch (Throwable err)
      {
         deployError = err;
         logger.error("Deployment Error", deployError);
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
   private NodeRef createDeploymentReportNode(final DeploymentReport report, 
            final NodeRef attempt, 
            final Map<QName, Serializable> serverProps, 
            final int version, 
            final NodeRef websiteRef,
            final Date startDate, 
            final Throwable error)
   {
      logger.debug("createDeploymentReportNode called ");
      NodeRef reportRef = null;

      final String serverUri = calculateServerUri(serverProps);
      final Map<QName, Serializable> reportProps = new HashMap<QName, Serializable>(4, 1.0f);
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
      reportProps.put(WCMAppModel.PROP_DEPLOYEXCLUDESUSED,
               serverProps.get(WCMAppModel.PROP_DEPLOYEXCLUDES));
      reportProps.put(WCMAppModel.PROP_DEPLOYSERVERURLUSED,
               serverProps.get(WCMAppModel.PROP_DEPLOYSERVERURL));

      reportProps.put(WCMAppModel.PROP_DEPLOYSUCCESSFUL, (report != null) && (error == null));
      if (report == null && error != null)
      {
         // add error message as fail reason if appropriate (the reported
         // exception is a wrapper so get the detail from the cause)
         String errorMsg = error.getMessage();
         Throwable cause = error.getCause();
         if (cause != null)
         {
            errorMsg = cause.getMessage();
         }
         
         reportProps.put(WCMAppModel.PROP_DEPLOYFAILEDREASON, errorMsg);
      }
      
      RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
      
      RetryingTransactionCallback<NodeRef> cb = new RetryingTransactionCallback<NodeRef>() 
      {
          public NodeRef execute() throws Throwable
          {
              /**
               * Write out the deployment report
               */
              NodeRef reportRef = nodeService.createNode(attempt,
                          WCMAppModel.ASSOC_DEPLOYMENTREPORTS, WCMAppModel.ASSOC_DEPLOYMENTREPORTS,
                          WCMAppModel.TYPE_DEPLOYMENTREPORT, reportProps).getChildRef();
                 ContentWriter writer = contentService.getWriter(reportRef, ContentModel.PROP_CONTENT, true);
                 writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                 writer.setEncoding("UTF-8");

                 if (report == null)
                 {
                    // There is no report 
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
                    // There is a deployment report 
                    StringBuilder builder = new StringBuilder();
                    for (DeploymentEvent event : report)
                    {
                       builder.append(event.getType());
                       builder.append(" ");
                       builder.append(event.getDestination());
                       builder.append("\r\n");
                    }
                    
                    if(error != null)
                    {
                        builder.append("\r\n");
                        
                        // add the full stack trace of the error as the content
                        StringWriter stack = new StringWriter();
                        PrintWriter stackPrint = new PrintWriter(stack);
                        error.printStackTrace(stackPrint);
                        
                        builder.append(stack.toString());
                    }

                    writer.putContent(builder.toString());
                 }

              return reportRef;
          }
      };
      
      // Run the creation of the deployment report in its own write transaction
      reportRef = tran.doInTransaction(cb, false, true);

      if (logger.isDebugEnabled())
         logger.debug("Created deplyoment report node (" + reportRef + ") for server " +
                  serverUri);

      return reportRef;
   }

   public void setTransactionService(TransactionService transactionService)
   {
       this.transactionService = transactionService;
   }

   public TransactionService getTransactionService()
   {
       return transactionService;
   }
}
