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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.template.DateCompareMethod;
import org.alfresco.repo.template.HasAspectMethod;
import org.alfresco.repo.template.I18NMessageMethod;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.LoginBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet responsible for streaming content from a template processed against a node directly
 * to the response stream.
 * <p>
 * The URL to the servlet should be generated thus:
 * <pre>/alfresco/template/workspace/SpacesStore/0000-0000-0000-0000
 * or
 * <pre>/alfresco/template/workspace/SpacesStore/0000-0000-0000-0000/workspace/SpacesStore/0000-0000-0000-0000
 * <p>
 * The store protocol, followed by the store ID, followed by the content Node Id used to
 * identify the node to execute the default template for. The second set of elements encode
 * the store and node Id of the template to used if a default is not set or not requested.
 * <p>
 * The URL may be followed by a valid 'ticket' argument for authentication: ?ticket=1234567890
 * <br>
 * And may be followed by a 'mimetype' argument specifying the mimetype to return the result as
 * on the stream. Otherwise it is assumed that HTML is the default response mimetype.
 * 
 * @author Kevin Roast
 */
public class TemplateContentServlet extends HttpServlet
{
   private static final String MIMETYPE_HTML = "text/html";

   private static final long serialVersionUID = -4123407921997235977L;
   
   private static Log logger = LogFactory.getLog(TemplateContentServlet.class);
   
   private static final String DEFAULT_URL  = "/template/{0}/{1}/{2}";
   private static final String TEMPALTE_URL = "/template/{0}/{1}/{2}/{3}/{4}/{5}";
   
   private static final String MSG_ERROR_CONTENT_MISSING = "error_content_missing";
   
   private static final String ARG_TICKET   = "ticket";
   private static final String ARG_MIMETYPE = "mimetype";
   
   /**
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
   {
      try
      {
         String uri = req.getRequestURI();
         
         if (logger.isDebugEnabled())
            logger.debug("Processing URL: " + uri + (req.getQueryString() != null ? ("?" + req.getQueryString()) : ""));
         
         // see if a ticket has been supplied
         String ticket = req.getParameter(ARG_TICKET);
         if (ticket == null || ticket.length() == 0)
         {
            if (AuthenticationHelper.authenticate(getServletContext(), req, res) == false)
            {
               // authentication failed - no point returning the content as we haven't logged in yet
               // so end servlet execution and save the URL so the login page knows what to do later
               req.getSession().setAttribute(LoginBean.LOGIN_REDIRECT_KEY, uri);
               return;
            }
         }
         else
         {
            AuthenticationHelper.authenticate(getServletContext(), req, res, ticket);
         }
         
         StringTokenizer t = new StringTokenizer(uri, "/");
         int tokenCount = t.countTokens();
         if (tokenCount < 5)
         {
            throw new IllegalArgumentException("Download URL did not contain all required args: " + uri); 
         }
         
         t.nextToken();    // skip web app name
         t.nextToken();    // skip servlet name
         
         // get NodeRef to the content
         StoreRef storeRef = new StoreRef(t.nextToken(), t.nextToken());
         NodeRef nodeRef = new NodeRef(storeRef, t.nextToken());
         
         // get NodeRef to the template if supplied
         NodeRef templateRef = null;
         if (tokenCount == 8)
         {
            storeRef = new StoreRef(t.nextToken(), t.nextToken());
            templateRef = new NodeRef(storeRef, t.nextToken());
         }
         
         String mimetype = MIMETYPE_HTML;
         if (req.getParameter(ARG_MIMETYPE) != null)
         {
            mimetype = req.getParameter(ARG_MIMETYPE);
         }
         res.setContentType(mimetype);
         
         // get the services we need to retrieve the content
         WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
         ServiceRegistry serviceRegistry = (ServiceRegistry)context.getBean(ServiceRegistry.SERVICE_REGISTRY);
         NodeService nodeService = serviceRegistry.getNodeService();
         TemplateService templateService = serviceRegistry.getTemplateService();
         
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
            Object model = getModel(serviceRegistry, req.getSession(), nodeRef);
            
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
         }
      }
      catch (Throwable err)
      {
         throw new AlfrescoRuntimeException("Error during download content servlet processing: " + err.getMessage(), err);
      }
   }
   
   private Object getModel(ServiceRegistry services, HttpSession session, NodeRef nodeRef)
   {
      // create FreeMarker default model and merge
      Map root = new HashMap(11, 1.0f);
      
      // supply the CompanyHome space as "companyhome"
      NodeRef companyRootRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId());
      TemplateNode companyRootNode = new TemplateNode(companyRootRef, services, imageResolver);
      root.put("companyhome", companyRootNode);
      
      // supply the users Home Space as "userhome"
      User user = Application.getCurrentUser(session);
      NodeRef userRootRef = new NodeRef(Repository.getStoreRef(), user.getHomeSpaceId());
      TemplateNode userRootNode = new TemplateNode(userRootRef, services, imageResolver);
      root.put("userhome", userRootNode);
      
      // put the current NodeRef in as "space" and "document"
      TemplateNode node = new TemplateNode(nodeRef, services, imageResolver);
      root.put("space", node);
      root.put("document", node);
      
      // supply the current user Node as "person"
      root.put("person", new TemplateNode(user.getPerson(), services, imageResolver));
      
      // current date/time is useful to have and isn't supplied by FreeMarker by default
      root.put("date", new Date());
      
      // add custom method objects
      root.put("hasAspect", new HasAspectMethod());
      root.put("message", new I18NMessageMethod());
      root.put("dateCompare", new DateCompareMethod());
      
      return root;
   }
   
   /** Template Image resolver helper */
   private TemplateImageResolver imageResolver = new TemplateImageResolver()
   {
       public String resolveImagePathForName(String filename, boolean small)
       {
           return Utils.getFileTypeImage(filename, small);
       }
   };
   
   /**
    * Helper to generate a URL to a content node for downloading content from the server.
    * The content is supplied as an HTTP1.1 attachment to the response. This generally means
    * a browser should prompt the user to save the content to specified location.
    * 
    * @param ref     NodeRef of the content node to generate URL for (cannot be null)
    * @param name    File name to return in the URL (cannot be null)
    * 
    * @return URL to download the content from the specified node
    */
   public final static String generateURL(NodeRef ref, String name)
   {
      return MessageFormat.format(DEFAULT_URL, new Object[] {
                ref.getStoreRef().getProtocol(),
                ref.getStoreRef().getIdentifier(),
                ref.getId() } );
   }
}
