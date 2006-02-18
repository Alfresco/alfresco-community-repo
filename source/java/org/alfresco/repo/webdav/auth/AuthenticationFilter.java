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

package org.alfresco.repo.webdav.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * WebDAV Authentication Filter Class
 * 
 * @author GKSpencer
 */
public class AuthenticationFilter implements Filter
{
    // Authenticated user session object name

    public final static String AUTHENTICATION_USER = "_alfDAVAuthTicket";

    // Servlet context

    private ServletContext m_context;

    // Various services required by NTLM authenticator
    
    private AuthenticationService m_authService;
    private PersonService m_personService;
    private NodeService m_nodeService;
    
    /**
     * Initialize the filter
     * 
     * @param config FitlerConfig
     * @exception ServletException
     */
    public void init(FilterConfig config) throws ServletException
    {
        // Save the context

        m_context = config.getServletContext();

        // Setup the authentication context

        WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(m_context);
        
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        m_nodeService = serviceRegistry.getNodeService();
        m_authService = serviceRegistry.getAuthenticationService();
        m_personService = (PersonService) ctx.getBean("PersonService");   // transactional and permission-checked
    }

    /**
     * Run the authentication filter
     * 
     * @param req ServletRequest
     * @param resp ServletResponse
     * @param chain FilterChain
     * @exception ServletException
     * @exception IOException
     */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
            ServletException
    {
        // Assume it's an HTTP request

        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;

        // Get the user details object from the session

        WebDAVUser user = (WebDAVUser) httpReq.getSession().getAttribute(AUTHENTICATION_USER);

        if (user == null)
        {
            // Get the authorization header
            
            String authHdr = httpReq.getHeader("Authorization");
            
            if ( authHdr != null && authHdr.length() > 5 && authHdr.substring(0,5).equalsIgnoreCase("BASIC"))
            {
                // Basic authentication details present

                String basicAuth = new String(Base64.decodeBase64(authHdr.substring(5).getBytes()));
                
                // Split the username and password
                
                String username = null;
                String password = null;
                
                int pos = basicAuth.indexOf(":");
                if ( pos != -1)
                {
                    username = basicAuth.substring(0, pos);
                    password = basicAuth.substring(pos + 1);
                }
                else
                {
                    username = basicAuth;
                    password = "";
                }
                
                try
                {
                    // Authenticate the user
                    m_authService.authenticate(username, password.toCharArray());
                    
                    // Get the user node and home folder
                    NodeRef personNodeRef = m_personService.getPerson(username);
                    NodeRef homeSpaceRef = (NodeRef) m_nodeService.getProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER);
                    // Setup User object and Home space ID etc.
                    user = new WebDAVUser(username, m_authService.getCurrentTicket(), homeSpaceRef);
                    
                    httpReq.getSession().setAttribute(AUTHENTICATION_USER, user);
                }
                catch ( AuthenticationException ex)
                {
                    // Do nothing, user object will be null
                }
                catch (NoSuchPersonException e)
                {
                    // Do nothing, user object will be null
                }
            }
            
            // Check if the user is authenticated, if not then prompt again
            
            if ( user == null)
            {
                // No user/ticket, force the client to prompt for logon details
    
                httpResp.setHeader("WWW-Authenticate", "BASIC realm=\"Alfresco DAV Server\"");
                httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    
                httpResp.flushBuffer();
                return;
            }
        }
        else
        {
            // Setup the authentication context

            m_authService.validate(user.getTicket());

            // Set the current locale

            // I18NUtil.setLocale(Application.getLanguage(httpRequest.getSession()));
        }

        // Chain other filters

        chain.doFilter(req, resp);
    }

    /**
     * Cleanup filter resources
     */
    public void destroy()
    {
        // Nothing to do
    }
}
