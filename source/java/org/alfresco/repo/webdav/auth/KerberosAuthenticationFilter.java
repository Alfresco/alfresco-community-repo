package org.alfresco.repo.webdav.auth;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.repo.web.auth.WebCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * WebDAV Kerberos Authentication Filter Class
 * 
 * @author GKSpencer
 */
public class KerberosAuthenticationFilter extends BaseKerberosAuthenticationFilter
{
    // Debug logging
    private static Log logger = LogFactory.getLog(KerberosAuthenticationFilter.class);

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
     * 
     * @see org.alfresco.repo.webdav.auth.BaseSSOAuthenticationFilter#getLogger()
     */
    @Override
    protected Log getLogger()
    {
        return logger;
    }

    @Override
    protected boolean checkLoginPage(HttpServletRequest req, HttpServletResponse resp)
    {
        return (req.getRequestURI().endsWith("/jsp/login.jsp"));
    }
    
    /**
     * Writes link to login page and refresh tag which cause user
     * to be redirected to the login page.
     *
     * @param context ServletContext
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException
     */
    protected void writeLoginPageLink(ServletContext context, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType(MIME_HTML_TEXT);

        final PrintWriter out = resp.getWriter();
        out.println("<html><head>");
        out.println("<meta http-equiv=\"Refresh\" content=\"0; url=" + req.getContextPath() + "/webdav\">");
        out.println("</head><body><p>Please <a href=\"" + req.getContextPath() + "/webdav\">log in</a>.</p>");
        out.println("</body></html>");
        out.close();
    }
}
