/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.webdav.auth;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

    @Override
    public String getLoginPageLink()
    {
        return loginPageLink;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseSSOAuthenticationFilter#onValidateFailed(jakarta.servlet.ServletContext, jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, jakarta.servlet.http.HttpSession)
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
    @Override
    protected void writeLoginPageLink(ServletContext context, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType(MIME_HTML_TEXT);

        try (PrintWriter out = resp.getWriter())
        {
            out.println("<html><head>");
            // Removed the auto refresh to avoid refresh loop, MNT-16931
            // Removed the link to the login page, MNT-20200
            out.println("</head><body><p>Login failed. Please try again.</p>");
            out.println("</body></html>");
        }
    }
}
