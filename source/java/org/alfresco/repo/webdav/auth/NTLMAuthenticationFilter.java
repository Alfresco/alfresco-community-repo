package org.alfresco.repo.webdav.auth;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.repo.web.auth.WebCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * WebDav NTLM Authentication Filter Class
 * 
 * @author GKSpencer
 */
public class NTLMAuthenticationFilter extends BaseNTLMAuthenticationFilter
{
    // Debug logging
    private static Log logger = LogFactory.getLog(NTLMAuthenticationFilter.class);

    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseSSOAuthenticationFilter#onValidateFailed(javax.servlet.ServletContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.http.HttpSession)
     */
    @Override
    protected void onValidateFailed(ServletContext sc, HttpServletRequest req, HttpServletResponse res, HttpSession session, WebCredentials webCredentials)
        throws IOException
    {
        super.onValidateFailed(sc, req, res, session, webCredentials);
        
        // Restart the login challenge process if validation fails
        restartLoginChallenge(sc, req, res);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#getLogger()
     */
    @Override
    final protected Log getLogger()
    {
        return logger;
    }
}
