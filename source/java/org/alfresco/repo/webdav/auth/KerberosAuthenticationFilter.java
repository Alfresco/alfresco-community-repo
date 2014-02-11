/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
     * @param resp HttpServletResponse
     * @param httpSess HttpSession
     * @throws IOException
     */
    protected void writeLoginPageLink(ServletContext context, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType(MIME_HTML_TEXT);

        final PrintWriter out = resp.getWriter();
        out.println("<html><head>");
        out.println("<meta http-equiv=\"Refresh\" content=\"0; url=" + req.getContextPath() + "/faces/jsp/login.jsp?_alfRedirect=%2Falfresco%2Fwebdav\">");
        out.println("</head><body><p>Please <a href=\"" + req.getContextPath() + "/faces/jsp/login.jsp?_alfRedirect=%2Falfresco%2Fwebdav\">log in</a>.</p>");
        out.println("</body></html>");
        out.close();
    }
}
