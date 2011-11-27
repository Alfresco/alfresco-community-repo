/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.opencmis;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interceptor to manage threads and perform other menial jobs that are common to all
 * calls made to the service.  It also provides detailed logging of values passing
 * in and out of the service.
 * <p/>
 * <b>DEBUG</b> shows authentication and inbound arguments.  <b>TRACE</b> shows full
 * return results as well.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class AlfrescoCmisServiceInterceptor implements MethodInterceptor
{
    private static Log logger = LogFactory.getLog(AlfrescoCmisServiceInterceptor.class);

    public AlfrescoCmisServiceInterceptor()
    {
    }

    @Override
    public synchronized Object invoke(MethodInvocation invocation) throws Throwable
    {
        // Keep note of whether debug is required
        boolean debug = logger.isDebugEnabled();
        boolean trace = logger.isTraceEnabled();
        StringBuilder sb = null;
        if (debug)
        {
            sb = new StringBuilder("\n" +
                        "CMIS invocation:         \n" +
                        "   Method:                 " + invocation.getMethod().getName() + "\n" +
                        "   Arguments:            \n");
            for (Object arg : invocation.getArguments())
            {
                sb.append("      ").append(arg).append("\n");
            }
        }

        Object ret = null;
        AlfrescoCmisService service = (AlfrescoCmisService) invocation.getThis();
        try
        {
            // Wrap with pre- and post-method calls
            try
            {
                sb.append(
                        "   Pre-call authentication: \n" +
                        "      Full auth:           " + AuthenticationUtil.getFullyAuthenticatedUser() + "\n" +
                        "      Effective auth:      " + AuthenticationUtil.getRunAsUser() + "\n");
                service.beforeCall();
                sb.append(
                        "   In-call authentication: \n" +
                        "      Full auth:           " + AuthenticationUtil.getFullyAuthenticatedUser() + "\n" +
                        "      Effective auth:      " + AuthenticationUtil.getRunAsUser() + "\n");
                ret = invocation.proceed();
            }
            finally
            {
                service.afterCall();
                sb.append(
                        "   Post-call authentication: \n" +
                        "      Full auth:           " + AuthenticationUtil.getFullyAuthenticatedUser() + "\n" +
                        "      Effective auth:      " + AuthenticationUtil.getRunAsUser() + "\n");
            }
            if (trace)
            {
                sb.append(
                        "   Returning:              ").append(ret).append("\n");
                logger.debug(sb);
            }
            // Done
            return ret;
        }
        catch (Throwable e)
        {
            if (debug)
            {
                sb.append("   Throwing:             " + e.getMessage());
                logger.debug(sb, e);
            }
            // Rethrow
            throw e;
        }
    }
}
