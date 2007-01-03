/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
