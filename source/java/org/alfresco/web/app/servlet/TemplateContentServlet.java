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
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.repo.component.template.DefaultModelHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet responsible for streaming content from a template processed against a node directly
 * to the response stream.
 * <p>
 * The URL to the servlet should be generated thus:
 * <pre>/alfresco/template/workspace/SpacesStore/0000-0000-0000-0000</pre>
 * or
 * <pre>/alfresco/template/workspace/SpacesStore/0000-0000-0000-0000/workspace/SpacesStore/0000-0000-0000-0000</pre>
 * or
 * <pre>/alfresco/template?templatePath=/Company%20Home/Data%20Dictionary/Presentation%20Templates/doc_info.ftl&contextPath=/Company%20Home/mydoc.txt</pre>
 * <p>
 * The store protocol, followed by the store ID, followed by the content Node Id used to
 * identify the node to execute the default template for. The second set of elements encode
 * the store and node Id of the template to used if a default is not set or not requested. Instead
 * of using NodeRef references to the template and context, path arguments can be used. The URL args
 * of 'templatePath' and 'contextPath' can be used instead to specify name based encoded Paths to the
 * template and its context.
 * <p>
 * The URL may be followed by a 'mimetype' argument specifying the mimetype to return the result as
 * on the stream. Otherwise it is assumed that HTML is the default response mimetype.
 * <p>
 * Like most Alfresco servlets, the URL may be followed by a valid 'ticket' argument for authentication:
 * ?ticket=1234567890
 * <p>
 * And/or also followed by the "?guest=true" argument to force guest access login for the URL. 
 * 
 * @author Kevin Roast
 */
public class TemplateContentServlet extends BaseServlet
{
   private static final String MIMETYPE_HTML = "text/html";

   private static final long serialVersionUID = -4123407921997235977L;
   
   private static Log logger = LogFactory.getLog(TemplateContentServlet.class);
   
   private static final String DEFAULT_URL  = "/template/{0}/{1}/{2}";
   private static final String TEMPLATE_URL = "/template/{0}/{1}/{2}/{3}/{4}/{5}";
   
   private static final String MSG_ERROR_CONTENT_MISSING = "error_content_missing";
   
   private static final String ARG_MIMETYPE = "mimetype";
   private static final String ARG_TEMPLATE_PATH = "templatePath";
   private static final String ARG_CONTEXT_PATH  = "contextPath";
   
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
      
      uri = uri.substring(req.getContextPath().length());
      StringTokenizer t = new StringTokenizer(uri, "/");
      int tokenCount = t.countTokens();
      
      t.nextToken();    // skip servlet name
      
      NodeRef nodeRef = null;
      NodeRef templateRef = null;
      
      String contentPath = req.getParameter(ARG_CONTEXT_PATH);
      if (contentPath != null && contentPath.length() != 0)
      {
         // process the name based path to resolve the NodeRef
         PathRefInfo pathInfo = resolveNamePath(getServletContext(), contentPath); 
         
         nodeRef = pathInfo.NodeRef;
      }
      else if (tokenCount > 3)
      {
         // get NodeRef to the content from the URL elements
         StoreRef storeRef = new StoreRef(t.nextToken(), t.nextToken());
         nodeRef = new NodeRef(storeRef, t.nextToken());
      }
      
      // get NodeRef to the template if supplied
      String templatePath = req.getParameter(ARG_TEMPLATE_PATH);
      if (templatePath != null && templatePath.length() != 0)
      {
         // process the name based path to resolve the NodeRef
         PathRefInfo pathInfo = resolveNamePath(getServletContext(), templatePath); 
         
         templateRef = pathInfo.NodeRef;
      }
      else if (tokenCount == 7)
      {
         StoreRef storeRef = new StoreRef(t.nextToken(), t.nextToken());
         templateRef = new NodeRef(storeRef, t.nextToken());
      }
      
      // if no context is specified, use the template itself
      // TODO: should this default to something else?
      if (nodeRef == null && templateRef != null)
      {
         nodeRef = templateRef;
      }
      
      if (nodeRef == null)
      {
         throw new TemplateException("Not enough arguments supplied in URL.");
      }
      
      // get the services we need to retrieve the content
      ServiceRegistry serviceRegistry = getServiceRegistry(getServletContext());
      NodeService nodeService = serviceRegistry.getNodeService();
      TemplateService templateService = serviceRegistry.getTemplateService();
      PermissionService permissionService = serviceRegistry.getPermissionService();
      
      // check that the user has at least READ access on any nodes - else redirect to the login page
      if (permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.DENIED ||
          (templateRef != null && permissionService.hasPermission(templateRef, PermissionService.READ) == AccessStatus.DENIED))
      {
         redirectToLoginPage(req, res, getServletContext());
         return;
      }
      
      String mimetype = MIMETYPE_HTML;
      if (req.getParameter(ARG_MIMETYPE) != null)
      {
         mimetype = req.getParameter(ARG_MIMETYPE);
      }
      res.setContentType(mimetype);
      
      try
      {
         UserTransaction txn = null;
         try
         {
            txn = serviceRegistry.getTransactionService().getUserTransaction(true);
            txn.begin();
            
            // if template not supplied, then use the default against the node
            if (templateRef == null)
            {
               if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPLATABLE))
               {
                  templateRef = (NodeRef)nodeService.getProperty(nodeRef, ContentModel.PROP_TEMPLATE);
               }
               if (templateRef == null)
               {
                  throw new TemplateException("Template reference not set against node or not supplied in URL.");
               }
            }
            
            // create the model - put the supplied noderef in as space/document as appropriate
            Object model = getModel(serviceRegistry, req, templateRef, nodeRef);
            
            // process the template against the node content directly to the response output stream
            // assuming the repo is capable of streaming in chunks, this should allow large files
            // to be streamed directly to the browser response stream.
            try
            {
               templateService.processTemplate(
                     null,
                     templateRef.toString(),
                     model,
                     res.getWriter());
               
               // commit the transaction
               txn.commit();
            }
            catch (SocketException e)
            {
               if (e.getMessage().contains("ClientAbortException"))
               {
                  // the client cut the connection - our mission was accomplished apart from a little error message
                  logger.error("Client aborted stream read:\n   node: " + nodeRef + "\n   template: " + templateRef);
                  try { if (txn != null) {txn.rollback();} } catch (Exception tex) {}
               }
               else
               {
                  throw e;
               }
            }
         }
         catch (Throwable txnErr)
         {
            try { if (txn != null) {txn.rollback();} } catch (Exception tex) {}
            throw txnErr;
         }
      }
      catch (Throwable err)
      {
         throw new AlfrescoRuntimeException("Error during template servlet processing: " + err.getMessage(), err);
      }
   }
   
   /**
    * Build the model that to process the template against.
    * <p>
    * The model includes the usual template root objects such as 'companyhome', 'userhome',
    * 'person' and also includes the node specified on the servlet URL as 'space' and 'document'
    *  
    * @param services      ServiceRegistry required for TemplateNode construction
    * @param req           Http request - for accessing Session and url args
    * @param templateRef   NodeRef of the template itself 
    * @param nodeRef       NodeRef of the space/document to process template against
    * 
    * @return an object model ready for executing template against
    */
   private Object getModel(ServiceRegistry services, HttpServletRequest req, NodeRef templateRef, NodeRef nodeRef)
   {
      // build FreeMarker default model and merge
      Map root = DefaultModelHelper.buildDefaultModel(services, Application.getCurrentUser(req.getSession())); 
      
      // put the current NodeRef in as "space" and "document"
      TemplateNode node = new TemplateNode(nodeRef, services, this.imageResolver);
      root.put("space", node);
      root.put("document", node);
      root.put("template", new TemplateNode(templateRef, services, this.imageResolver));
      
      // add URL arguments as a map called 'args' to the root of the model
      Map<String, String> args = new HashMap<String, String>(8, 1.0f);
      Enumeration names = req.getParameterNames();
      while (names.hasMoreElements())
      {
         String name = (String)names.nextElement();
         args.put(name, req.getParameter(name));
      }
      root.put("args", args);
      
      return root;
   }
   
   /** Template Image resolver helper */
   private TemplateImageResolver imageResolver = new TemplateImageResolver()
   {
      public String resolveImagePathForName(String filename, boolean small)
      {
         return Utils.getFileTypeImage(getServletContext(), filename, small);
      }
   };
   
   /**
    * Helper to generate a URL to process a template against a node.
    * <p>
    * The result of the template is supplied returned as the response.
    * 
    * @param nodeRef       NodeRef of the content node to generate URL for (cannot be null)
    * @param templateRef   NodeRef of the template to process against, or null to use default
    * 
    * @return URL to process the template
    */
   public final static String generateURL(NodeRef nodeRef, NodeRef templateRef)
   {
      if (templateRef == null)
      {
         return MessageFormat.format(DEFAULT_URL, new Object[] {
                   nodeRef.getStoreRef().getProtocol(),
                   nodeRef.getStoreRef().getIdentifier(),
                   nodeRef.getId() } );
      }
      else
      {
         return MessageFormat.format(TEMPLATE_URL, new Object[] {
                   nodeRef.getStoreRef().getProtocol(),
                   nodeRef.getStoreRef().getIdentifier(),
                   nodeRef.getId(),
                   templateRef.getStoreRef().getProtocol(),
                   templateRef.getStoreRef().getIdentifier(),
                   templateRef.getId()} );
      }
   }
}
