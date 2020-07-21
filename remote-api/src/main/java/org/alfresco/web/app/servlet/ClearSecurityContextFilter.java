/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.web.app.servlet;

import net.sf.acegisecurity.context.Context;
import net.sf.acegisecurity.context.ContextHolder;
import org.alfresco.repo.security.authentication.AlfrescoSecureContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class ClearSecurityContextFilter implements Filter
{
    private Log logger = LogFactory.getLog(getClass());

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException
    {
        try
        {
            chain.doFilter(servletRequest, servletResponse);
        }
        finally
        {
            logClearContextInfo(servletRequest);
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

    private void logClearContextInfo(ServletRequest servletRequest)
    {
        if (!logger.isDebugEnabled())
        {
            return;
        }

        try
        {
            // print information about the request and leaked context
            String identifiedUserName = null;
            final String fullyAuthenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
            final Context context = ContextHolder.getContext();

            if (context instanceof AlfrescoSecureContext)
            {
                AlfrescoSecureContext sc = (AlfrescoSecureContext) context;
                identifiedUserName = AuthenticationUtil.getMaskedUsername(sc.getRealAuthentication());
            }
            if (context == null && fullyAuthenticatedUser == null)
            {
                // nothing unusual to log
                return;
            }

            final String newLine = System.lineSeparator();
            String url = null;
            if (servletRequest instanceof HttpServletRequest)
            {
                url = ((HttpServletRequest) servletRequest).getRequestURL().toString();
            }

            StringBuilder sb = new StringBuilder();
            sb.append("When clearing out the context for request: ");
            sb.append(url);
            sb.append(newLine);

            sb.append("There was some information still present in the security context for this thread: ");
            sb.append(Thread.currentThread().getName());
            sb.append(newLine);

            if (context != null)
            {
                if (identifiedUserName != null)
                {
                    sb.append("Real authenticated user found: " + AuthenticationUtil.maskUsername(identifiedUserName));
                    sb.append(newLine);
                }
                else
                {
                    sb.append("ContextHolder was not null");
                    sb.append(newLine);
                }
            }
            if (fullyAuthenticatedUser != null)
            {
                sb.append("Fully authenticated user found: " + AuthenticationUtil.maskUsername(fullyAuthenticatedUser));
                sb.append(newLine);
            }
            sb.append("Other information about leaking ticket and tenant information may follow in the log, "
                + "if org.alfresco.repo.security.authentication.InMemoryTicketComponentImpl "
                + "and org.alfresco.repo.tenant.TenantContextHolder loggers are set to 'trace'");
            sb.append(newLine);

            logger.debug(sb.toString());
        }
        catch (Exception e)
        {
            logger.debug("Error building proper logging message:" + e.getMessage(), e);
        }
    }

    @Override
    public void init(FilterConfig config) throws ServletException
    {
    }

    @Override
    public void destroy()
    {
    }
}