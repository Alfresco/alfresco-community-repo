/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.api;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * API Service Logger
 * 
 * @author davidc
 */
public class ServiceLogger implements MethodInterceptor
{
    // Logger
    private static final Log logger = LogFactory.getLog(ServiceLogger.class);

    /* (non-Javadoc)
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation invocation)
        throws Throwable
    {
        Object retVal = null;
        
        if (logger.isDebugEnabled())
        {
            APIService service = (APIService)invocation.getThis();
            String user = AuthenticationUtil.getCurrentUserName();
            String locale = I18NUtil.getLocale().toString();
            logger.debug("Invoking service "  + service.getName() + (user == null ? " (unauthenticated)" : " (authenticated as " + user + ")" + " (" + locale + ")"));
            long start = System.currentTimeMillis();
            retVal = invocation.proceed();
            long end = System.currentTimeMillis();
            logger.debug("Service " + service.getName() + " executed in " + (end - start) + "ms");
        }
        else
        {
            retVal = invocation.proceed();
        }

        return retVal;
    }

}
