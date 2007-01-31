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
import java.text.MessageFormat;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
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
 * And/or also followed by the "?guest=true" argument to force guest access login for the URL. If the 
 * guest=true parameter is used the current session will be logged out and the guest user logged in. 
 * Therefore upon completion of this request the current user will be "guest".
 * <p>
 * If the user attempting the request is not authorised to access the requested node the login page
 * will be redirected to.
 * 
 * @author Kevin Roast
 */
public class TemplateContentServlet extends BaseTemplateContentServlet
{   
   private static final long serialVersionUID = -2510767849932627519L;

   private static final Log logger = LogFactory.getLog(TemplateContentServlet.class);
   
   private static final String DEFAULT_URL  = "/template/{0}/{1}/{2}";
   private static final String TEMPLATE_URL = "/template/{0}/{1}/{2}/{3}/{4}/{5}";
   
   @Override
   protected Log getLogger()
   {
      return logger;
   }  

   @Override
   protected Map<String, Object> buildModel(ServiceRegistry services, HttpServletRequest req, 
         NodeRef templateRef)
   {
      return DefaultModelHelper.buildDefaultModel(services, 
            Application.getCurrentUser(req.getSession()), templateRef, this.imageResolver);
   }
   
   /**
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   protected void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
   {
      if (logger.isDebugEnabled())
      {
         String queryString = req.getQueryString();
         logger.debug("Authenticating request to URL: " + req.getRequestURI() + 
               ((queryString != null && queryString.length() > 0) ? ("?" + queryString) : ""));
      }
      
      AuthenticationStatus status = servletAuthenticate(req, res);
      if (status == AuthenticationStatus.Failure)
      {
         return;
      }
      
      processTemplateRequest(req, res, true);
   }
   
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
