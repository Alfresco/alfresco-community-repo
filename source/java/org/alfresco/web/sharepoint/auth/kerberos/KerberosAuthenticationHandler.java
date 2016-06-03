package org.alfresco.web.sharepoint.auth.kerberos;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.repo.SessionUser;
import org.alfresco.repo.web.auth.WebCredentials;
import org.alfresco.repo.webdav.auth.BaseKerberosAuthenticationFilter;
import org.alfresco.repo.webdav.auth.SharepointConstants;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Kerberos SSO web authentication implementation.
 * </p>
 */
public class KerberosAuthenticationHandler extends BaseKerberosAuthenticationFilter
{
    // Debug logging
    private static Log logger = LogFactory.getLog(KerberosAuthenticationHandler.class);

    @Override
    protected void init() throws ServletException
    {
        setUserAttributeName(SharepointConstants.USER_SESSION_ATTRIBUTE);
        super.init();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseSSOAuthenticationFilter#onValidateFailed(javax.servlet.ServletContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.http.HttpSession)
     */
    @Override
    protected void onValidateFailed(ServletContext sc, HttpServletRequest req, HttpServletResponse res, HttpSession session, WebCredentials credentials)
        throws IOException
    {
        super.onValidateFailed(sc, req, res, session, credentials);
        
        // Restart the login challenge process if validation fails
        restartLoginChallenge(sc, req, res);
    }    

    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseAuthenticationFilter#createUserObject(java.lang.String, java.lang.String, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected SessionUser createUserObject(String userName, String ticket, NodeRef personNode, NodeRef homeSpaceRef)
    {
        // Create a web client user object
        User user = new User( userName, ticket, personNode);
        user.setHomeSpaceId( homeSpaceRef.getId());
        
        return user;
    }
    
    @Override
    protected Log getLogger()
    {
        return logger;
    }
}