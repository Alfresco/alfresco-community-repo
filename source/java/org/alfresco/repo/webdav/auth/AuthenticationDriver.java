package org.alfresco.repo.webdav.auth;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A general interface for servlet-based authentication. Allows code to be shared by Web Client, WebDAV and Sharepoint
 * authentication classes.
 * 
 * @author dward
 */
public interface AuthenticationDriver
{
    public static final String AUTHENTICATION_USER = "_alfAuthTicket";
    
    /**
     * Authenticate user based on information in http request such as Authorization header or cached session
     * information.
     * 
     * @param context
     *            the context
     * @param request
     *            http request
     * @param response
     *            http response
     * @return <code>true</code> if authentication was successful
     * @throws IOException
     * @throws ServletException
     */
    public boolean authenticateRequest(ServletContext context, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException;

    /**
     * Send a status 401 response that will restart the log in handshake.
     * 
     * @param context
     *            the context
     * @param request
     *            http request
     * @param response
     *            http response
     * @throws IOException
     */
    public void restartLoginChallenge(ServletContext context, HttpServletRequest request, HttpServletResponse response)
            throws IOException;
}