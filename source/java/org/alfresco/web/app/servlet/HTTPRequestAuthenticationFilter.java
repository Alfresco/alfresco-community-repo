/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

import org.alfresco.config.ConfigService;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.LoginBean;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.config.LanguagesConfigElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Generic HTTP request filter for setting the authenticated user when used with authentication systems such as
 * SiteMinder, Novell IChains and CAS.
 * 
 * @author Andy Hind
 */
public class HTTPRequestAuthenticationFilter extends AbstractAuthenticationFilter implements Filter
{
    private static final String LOCALE = "locale";

    public static final String MESSAGE_BUNDLE = "alfresco.messages.webclient";

    private static Log logger = LogFactory.getLog(HTTPRequestAuthenticationFilter.class);

    private ServletContext context;

    private String loginPage;

    private AuthenticationComponent authComponent;

    private AuthenticationService authService;

    private TransactionService transactionService;

    private PersonService personService;

    private NodeService nodeService;

    private List<String> m_languages;

    private String httpServletRequestAuthHeaderName;

    // By default match everything if this is not set
    private String authPatternString = null;

    private Pattern authPattern = null;

    /**
     * Define the HTTP header that contains the user name
     * 
     * @param httpServletRequestAuthHeaderName
     */
    public HTTPRequestAuthenticationFilter(String httpServletRequestAuthHeaderName)
    {
        this(httpServletRequestAuthHeaderName, null);
    }

    /**
     * Define the header that contains the user name and how to extract the user name.
     * 
     * @param httpServletRequestAuthHeaderName
     * @param authPatternString
     */
    public HTTPRequestAuthenticationFilter(String httpServletRequestAuthHeaderName, String authPatternString)
    {
        super();
        assert (httpServletRequestAuthHeaderName != null);
        this.httpServletRequestAuthHeaderName = httpServletRequestAuthHeaderName;
        this.authPatternString = authPatternString;
        if (this.authPatternString != null)
        {
            try
            {
                authPattern = Pattern.compile(this.authPatternString);
            }
            catch (PatternSyntaxException e)
            {
                logger.warn("Invalid pattern: " + this.authPatternString, e);
                authPattern = null;
            }
        }
    }

    public void destroy()
    {
        // Nothing to do
    }

    /**
     * Run the filter
     * 
     * @param sreq
     *            ServletRequest
     * @param sresp
     *            ServletResponse
     * @param chain
     *            FilterChain
     * @exception IOException
     * @exception ServletException
     */
    public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain chain) throws IOException,
            ServletException
    {
        // Get the HTTP request/response/session

        HttpServletRequest req = (HttpServletRequest) sreq;
        HttpServletResponse resp = (HttpServletResponse) sresp;

        HttpSession httpSess = req.getSession(true);

        // Check for the auth header

        String authHdr = req.getHeader(httpServletRequestAuthHeaderName);
        if (logger.isDebugEnabled())
        {
            if (authHdr == null)
            {
                logger.debug("Header not found: " + httpServletRequestAuthHeaderName);
            }
            else
            {
                logger.debug("Header is <" + authHdr + ">");
            }
        }

        // Throw an error if we have an unknown authentication

        if ((authHdr == null) || (authHdr.length() < 1))
        {
            resp.sendRedirect(req.getContextPath() + "/jsp/noaccess.jsp");
            return;
        }

        // Get the user

        String userName = "";
        if (authPattern != null)
        {
            Matcher matcher = authPattern.matcher(authHdr);
            if (matcher.matches())
            {
                userName = matcher.group();
                if ((userName == null) || (userName.length() < 1))
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Extracted null or empty user name from pattern "
                                + authPatternString + " against " + authHdr);
                    }
                    resp.sendRedirect(req.getContextPath() + "/jsp/noaccess.jsp");
                    return;
                }
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("no pattern match for " + authPatternString + " against " + authHdr);
                }
                resp.sendRedirect(req.getContextPath() + "/jsp/noaccess.jsp");
                return;
            }
        }
        else
        {
            userName = authHdr;
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("User = " + userName);
        }

        // See if there is a user in the session and test if it matches

        User user = (User) httpSess.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);

        if (user != null)
        {
            try
            {
                // Debug

                if (logger.isDebugEnabled())
                    logger.debug("User " + user.getUserName() + " validate ticket");

                // Validate the user ticket

                if (user.getUserName().equals(userName))
                {

                    // Set the current locale
                    authComponent.setCurrentUser(user.getUserName());
                    I18NUtil.setLocale(Application.getLanguage(httpSess));
                    chain.doFilter(sreq, sresp);
                    return;
                }
                else
                {
                    // No match
                    setAuthenticatedUser(req, httpSess, userName);
                }
            }
            catch (AuthenticationException ex)
            {
                if (logger.isErrorEnabled())
                    logger.error("Failed to validate user " + user.getUserName(), ex);
            }
        }

        setAuthenticatedUser(req, httpSess, userName);

        // Redirect the login page as it is never seen as we always login by name
        if (req.getRequestURI().endsWith(getLoginPage()) == true)
        {
            if (logger.isDebugEnabled())
                logger.debug("Login page requested, chaining ...");

            resp.sendRedirect(req.getContextPath() + "/faces/jsp/browse/browse.jsp");
            return;
        }
        else
        {
            chain.doFilter(sreq, sresp);
            return;
        }
    }

    /**
     * Set the authenticated user. It does not check that the user exists at the moment.
     * 
     * @param req
     * @param httpSess
     * @param userName
     */
    private void setAuthenticatedUser(HttpServletRequest req, HttpSession httpSess, String userName)
    {
        // Set the authentication
        authComponent.setCurrentUser(userName);

        // Set up the user information
        UserTransaction tx = transactionService.getUserTransaction();
        NodeRef homeSpaceRef = null;
        User user;
        try
        {
            tx.begin();
            user = new User(userName, authService.getCurrentTicket(), personService.getPerson(userName));
            homeSpaceRef = (NodeRef) nodeService.getProperty(personService.getPerson(userName),
                    ContentModel.PROP_HOMEFOLDER);
            user.setHomeSpaceId(homeSpaceRef.getId());
            tx.commit();
        }
        catch (Throwable ex)
        {
            logger.error(ex);

            try
            {
                tx.rollback();
            }
            catch (Exception ex2)
            {
                logger.error("Failed to rollback transaction", ex2);
            }

            if (ex instanceof RuntimeException)
            {
                throw (RuntimeException) ex;
            }
            else
            {
                throw new RuntimeException("Failed to set authenticated user", ex);
            }
        }

        // Store the user

        httpSess.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);
        httpSess.setAttribute(LoginBean.LOGIN_EXTERNAL_AUTH, Boolean.TRUE);

        // Set the current locale from the Accept-Lanaguage header if available

        Locale userLocale = parseAcceptLanguageHeader(req, m_languages);

        if (userLocale != null)
        {
            httpSess.setAttribute(LOCALE, userLocale);
            httpSess.removeAttribute(MESSAGE_BUNDLE);
        }

        // Set the locale using the session

        I18NUtil.setLocale(Application.getLanguage(httpSess));
    }

    public void init(FilterConfig config) throws ServletException
    {
        this.context = config.getServletContext();
        WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();

        authComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        authService = (AuthenticationService) ctx.getBean("authenticationService");
        personService = (PersonService) ctx.getBean("personService");

        // Get a list of the available locales

        ConfigService configServiceService = (ConfigService) ctx.getBean("webClientConfigService");
        LanguagesConfigElement configElement = (LanguagesConfigElement) configServiceService.getConfig("Languages")
                .getConfigElement(LanguagesConfigElement.CONFIG_ELEMENT_ID);

        m_languages = configElement.getLanguages();

        authPattern = Pattern.compile(authPatternString);
    }

    /**
     * Return the login page address
     * 
     * @return String
     */
    private String getLoginPage()
    {
        if (loginPage == null)
        {
            loginPage = Application.getLoginPage(context);
        }

        return loginPage;
    }

}
