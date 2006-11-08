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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.repo.component.template.DefaultModelHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

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
 * This servlet only accesses content available to the guest user. If the guest user does not
 * have access to the requested a 401 Forbidden response is returned to the caller.
 * <p>
 * This servlet does not effect the current session, therefore if guest access is required to a 
 * resource this servlet can be used without logging out the current user.
 * 
 * @author gavinc
 */
public class GuestTemplateContentServlet extends BaseTemplateContentServlet
{   
   private static final long serialVersionUID = -2510767849932627519L;

   private static final Log logger = LogFactory.getLog(GuestTemplateContentServlet.class);
   
   private static final String DEFAULT_URL  = "/guestTemplate/{0}/{1}/{2}";
   private static final String TEMPLATE_URL = "/guestTemplate/{0}/{1}/{2}/{3}/{4}/{5}";
   
   @Override
   protected Log getLogger()
   {
      return logger;
   }

   @Override
   protected Map<String, Object> buildModel(ServiceRegistry services, HttpServletRequest req, 
         NodeRef templateRef)
   {
      // setup the guest user to pass to the build model helper method
      AuthenticationService auth = (AuthenticationService)services.getAuthenticationService();
      PersonService personService = (PersonService)services.getPersonService();
      NodeService nodeService = (NodeService)services.getNodeService();
      
      NodeRef guestRef = personService.getPerson(PermissionService.GUEST_AUTHORITY);
      User guestUser = new User(PermissionService.GUEST_AUTHORITY, auth.getCurrentTicket(), guestRef);
      NodeRef guestHomeRef = (NodeRef)nodeService.getProperty(guestRef, ContentModel.PROP_HOMEFOLDER);
      if (nodeService.exists(guestHomeRef) == false)
      {
         throw new InvalidNodeRefException(guestHomeRef);
      }
      guestUser.setHomeSpaceId(guestHomeRef.getId());
      
      // build the default model
      return DefaultModelHelper.buildDefaultModel(services, guestUser, templateRef);
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
         logger.debug("Setting up guest access to URL: " + req.getRequestURI() + 
               ((queryString != null && queryString.length() > 0) ? ("?" + queryString) : ""));
      }
      
      TemplateContentWork tcw = new TemplateContentWork(req, res);
      AuthenticationUtil.runAs(tcw, PermissionService.GUEST_AUTHORITY);
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
   
   /**
    * Class to wrap the call to processTemplateRequest.
    * 
    * @author gavinc
    */
   public class TemplateContentWork implements RunAsWork<Object>
   {
      private HttpServletRequest req = null;
      private HttpServletResponse res = null;
      
      public TemplateContentWork(HttpServletRequest req, HttpServletResponse res)
      {
         this.req = req;
         this.res = res;
      }
      
      public Object doWork() throws Exception
      {
         processTemplateRequest(this.req, this.res, false);
         
         return null;
      }
   }
}
