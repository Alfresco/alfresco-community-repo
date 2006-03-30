/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.WorkflowUtil;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet responsible for executing workflow commands upon a node.
 * <p>
 * The URL to the servlet should be generated thus:
 * <pre>/alfresco/workflow/command/workspace/SpacesStore/0000-0000-0000-0000
 * <p>
 * The 'command' identifies the workflow action to execute upon the node (e.g. "approve" or "reject").
 * The store protocol, followed by the store ID, followed by the content Node Id used to
 * identify the node to execute the workflow action upon.
 * <p>
 * A 'return-page' URL argument can be specified as the redirect page to navigate too after processing.
 * <p>
 * Like most Alfresco servlets, the URL may be followed by a valid 'ticket' argument for authentication:
 * ?ticket=1234567890
 * <p>
 * And/or also followed by the "?guest=true" argument to force guest access login for the URL. 
 * 
 * @author Kevin Roast
 */
public class WorkflowActionServlet extends BaseServlet
{
   private static final long serialVersionUID = -3111407921997365999L;
   
   private static Log logger = LogFactory.getLog(WorkflowActionServlet.class);
   
   private static CommandFactory commandfactory = CommandFactory.getInstance();
   
   public static final String ARG_RETURNPAGE = "return-page";
   
   public static final String CMD_APPROVE = "approve";
   public static final String CMD_REJECT  = "reject";
   
   private static final String DEFAULT_URL  = "/workflow/{0}/{1}/{2}/{3}";
   private static final String RETURN_URL  = "/workflow/{0}/{1}/{2}/{3}?" + ARG_RETURNPAGE + "={4}";
   
   static
   {
      // register the available Workflow commands
      commandfactory.registerCommand(CMD_APPROVE, ApproveCommand.class);
      commandfactory.registerCommand(CMD_REJECT, RejectCommand.class);
   }
   
   
   /**
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
   {
      String uri = req.getRequestURI();
      
      if (logger.isDebugEnabled())
         logger.debug("Processing URL: " + uri + (req.getQueryString() != null ? ("?" + req.getQueryString()) : ""));
      
      AuthenticationStatus status = servletAuthenticate(req, res);
      if (status == AuthenticationStatus.Failure)
      {
         return;
      }
      
      StringTokenizer t = new StringTokenizer(uri, "/");
      int tokenCount = t.countTokens();
      if (tokenCount < 5)
      {
         throw new IllegalArgumentException("Workflow Servlet URL did not contain all required args: " + uri); 
      }
      
      t.nextToken();    // skip web app name
      t.nextToken();    // skip servlet name
      
      // get the command to perform e.g. "approve"
      String command = t.nextToken();
      
      // get NodeRef to the node with the workflow attached to it
      StoreRef storeRef = new StoreRef(t.nextToken(), t.nextToken());
      NodeRef nodeRef = new NodeRef(storeRef, t.nextToken());
      
      // get the services we need to execute the workflow command
      ServiceRegistry serviceRegistry = getServiceRegistry(getServletContext());
      PermissionService permissionService = serviceRegistry.getPermissionService();
      
      // check that the user has at least READ access on the node - else redirect to the login page
      if (permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.DENIED)
      {
         redirectToLoginPage(req, res, getServletContext());
         return;
      }
      
      try
      {
         UserTransaction txn = null;
         try
         {
            txn = serviceRegistry.getTransactionService().getUserTransaction();
            txn.begin();
            
            // find the workflow command from the registered list of commands
            // the CommandFactory supplies use with an instance of the command to use
            Map<String, Object> properties = new HashMap<String, Object>(1, 1.0f);
            properties.put("target", nodeRef);
            Command cmd = commandfactory.createCommand(command, properties);
            if (cmd == null)
            {
               throw new AlfrescoRuntimeException("Unknown workflow command specified: " + command);
            }
            cmd.execute(serviceRegistry);
            
            // commit the transaction
            txn.commit();
         }
         catch (Throwable txnErr)
         {
            try { if (txn != null) {txn.rollback();} } catch (Exception tex) {}
            throw txnErr;
         }
      }
      catch (Throwable err)
      {
         // TODO: could show error status output here instead of throwing an exception?
         throw new AlfrescoRuntimeException("Error during workflow servlet processing: " + err.getMessage(), err);
      }
      
      String returnPage = req.getParameter(ARG_RETURNPAGE);
      if (returnPage != null && returnPage.length() != 0)
      {
         if (logger.isDebugEnabled())
            logger.debug("Redirecting to specified return page: " + returnPage);
         
         res.sendRedirect(returnPage);
      }
      else
      {
         if (logger.isDebugEnabled())
            logger.debug("No return page specified, displaying status output.");
         
         res.setContentType("text/html");
         PrintWriter out = res.getWriter();
         out.print("Workflow command: '");
         out.print(command);
         out.print("' executed against node: ");
         out.println(nodeRef.toString());
         out.close();
      }
   }
   
   /**
    * Helper to generate a URL to process a workflow action against a node.
    * <p>
    * The result of the workflow action is supplied returned as the response.
    * 
    * @param nodeRef       NodeRef of the node to generate URL for (cannot be null)
    * @param action        Workflow action (See constants) to execute on the node
    * 
    * @return URL to process the workflow action
    */
   public final static String generateURL(NodeRef nodeRef, String action)
   {
      return MessageFormat.format(DEFAULT_URL, new Object[] {
                action,
                nodeRef.getStoreRef().getProtocol(),
                nodeRef.getStoreRef().getIdentifier(),
                nodeRef.getId() } );
   }
   
   /**
    * Helper to generate a URL to process a workflow action against a node.
    * <p>
    * The result of the workflow action is supplied returned as the response.
    * 
    * @param nodeRef       NodeRef of the node to generate URL for (cannot be null)
    * @param action        Workflow action (See constants) to execute on the node
    * @param returnPage    Return page URL to redirect after success (e.g. /alfresco/navigate/browse)
    * 
    * @return URL to process the workflow action
    */
   public final static String generateURL(NodeRef nodeRef, String action, String returnPage)
   {
      try
      {
         return MessageFormat.format(RETURN_URL, new Object[] {
                   action,
                   nodeRef.getStoreRef().getProtocol(),
                   nodeRef.getStoreRef().getIdentifier(),
                   nodeRef.getId(),
                   Utils.replace(URLEncoder.encode(returnPage, "UTF-8"), "+", "%20")} );
      }
      catch (UnsupportedEncodingException uee)
      {
         throw new AlfrescoRuntimeException("Failed to encode workflow URL for node: " + nodeRef, uee);
      }
   }
   
   
   /**
    * Simple command pattern interface
    */
   interface Command
   {
      /**
       * Execute the command
       * 
       * @param serviceRegistry     The ServiceRegistry instance
       */
      void execute(ServiceRegistry serviceRegistry);
      
      /**
       * @param properties bag of named properties for the command
       */
      void setProperties(Map<String, Object> properties);
   }
   
   
   /**
    * Base command class
    */
   static abstract class BaseCommand implements Command
   {
      Map<String, Object> properties = Collections.<String, Object>emptyMap();
      
      /**
       * @see org.alfresco.web.app.servlet.WorkflowActionServlet.Command#setProperties(java.util.Map)
       */
      public void setProperties(Map<String, Object> properties)
      {
         if (properties != null)
         {
            this.properties = properties;
         }
      }
   }
   
   
   /**
    * Approve Workflow command implementation
    */
   static final class ApproveCommand extends BaseCommand
   {
      static final String PROP_TARGET = "target";
      
      /**
       * @see org.alfresco.web.app.servlet.WorkflowActionServlet.Command#execute(org.alfresco.service.ServiceRegistry)
       */
      public void execute(ServiceRegistry serviceRegistry)
      {
         NodeRef nodeRef = (NodeRef)this.properties.get(PROP_TARGET);
         if (nodeRef == null)
         {
            throw new IllegalArgumentException(
                  "Unable to execute ApproveCommand - mandatory parameter not supplied: " + PROP_TARGET);
         }
         
         WorkflowUtil.approve(nodeRef, serviceRegistry.getNodeService(), serviceRegistry.getCopyService());
      }
   }
   
   
   /**
    * Reject Workflow command implementation
    */
   static final class RejectCommand extends BaseCommand
   {
      static final String PROP_TARGET = "target";
      
      /**
       * @see org.alfresco.web.app.servlet.WorkflowActionServlet.Command#execute(org.alfresco.service.ServiceRegistry)
       */
      public void execute(ServiceRegistry serviceRegistry)
      {
         NodeRef nodeRef = (NodeRef)this.properties.get(PROP_TARGET);
         if (nodeRef == null)
         {
            throw new IllegalArgumentException(
                  "Unable to execute RejectCommand - mandatory parameter not supplied: " + PROP_TARGET);
         }
         
         WorkflowUtil.reject(nodeRef, serviceRegistry.getNodeService(), serviceRegistry.getCopyService());
      }
   }
   
   
   /**
    * Command Factory helper
    */
   static final class CommandFactory
   {
      private static CommandFactory instance = new CommandFactory();
      
      private static Map<String, Class> registry = new HashMap<String, Class>(4, 1.0f);
      
      /**
       * Private constructor - protect the singleton instance
       */
      private CommandFactory()
      {
      }
      
      /**
       * @return the singleton CommandFactory instance
       */
      static CommandFactory getInstance()
      {
         return instance;
      }
      
      /**
       * Register a command name against an implementation
       * 
       * @param name       Unique name of the command
       * @param clazz      Class implementation of the command
       */
      void registerCommand(String name, Class clazz)
      {
         registry.put(name, clazz);
      }
      
      /**
       * Create a command instance of the specified command name
       * 
       * @param name       Name of the command to create (must be registered)
       * 
       * @return the Command instance or null if not found
       */
      Command createCommand(String name)
      {
         return createCommand(name, null);
      }
      
      /**
       * Create a command instance of the specified command name
       * 
       * @param name       Name of the command to create (must be registered)
       * @param properties Bag of name/value properties to pass to command upon creation 
       * 
       * @return the Command instance or null if not found
       */
      Command createCommand(String name, Map<String, Object> properties)
      {
         Command result = null;
         
         // lookup command by name in the registry
         Class clazz = registry.get(name);
         if (clazz != null)
         {
            try
            {
               Object obj = clazz.newInstance();
               if (obj instanceof Command)
               {
                  result = (Command)obj;
                  if (properties != null)
                  {
                     result.setProperties(properties);
                  }
               }
            }
            catch (Throwable err)
            {
               // return default if this occurs
               logger.warn("Unable to create workflow command instance '" + name +
                     "' with classname '" + clazz.getName() + "' due to error: " + err.getMessage());
            }
         }
         
         return result;
      }
   }
}
