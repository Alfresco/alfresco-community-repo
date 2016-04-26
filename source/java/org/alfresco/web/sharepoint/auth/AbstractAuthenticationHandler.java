package org.alfresco.web.sharepoint.auth;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.webdav.auth.AuthenticationDriver;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Abstract implementation of web authentication.</p>
 * 
 * @author PavelYur
 *
 */
public abstract class AbstractAuthenticationHandler implements AuthenticationDriver, ActivateableBean
{
    private final static String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";

    protected Log logger = LogFactory.getLog(getClass());
    protected AuthenticationService authenticationService;
    protected PersonService personService;
    private boolean isActive = true;

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setActive(boolean isActive)
    {
        this.isActive = isActive;
    }

    public boolean isActive()
    {
        return this.isActive;
    }

    /**
     * Returns the <i>value</i> of 'WWW-Authenticate' http header that determine what type of authentication to use by
     * client.
     * 
     * @return value
     */
    public abstract String getWWWAuthenticate();

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.SharepointAuthenticationHandler#restartLoginChallenge(javax.servlet.ServletContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void restartLoginChallenge(ServletContext context, HttpServletRequest request, HttpServletResponse response)
    {
        if (logger.isDebugEnabled())
            logger.debug("Force the client to prompt for logon details");

        response.setHeader(HEADER_WWW_AUTHENTICATE, getWWWAuthenticate());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}