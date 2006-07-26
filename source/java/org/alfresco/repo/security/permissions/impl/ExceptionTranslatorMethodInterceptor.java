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
package org.alfresco.repo.security.permissions.impl;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * Interceptor to translate and possibly I18Nize exceptions thrown by service calls. 
 */
public class ExceptionTranslatorMethodInterceptor implements MethodInterceptor
{
    private static final String MSG_ACCESS_DENIED = "permissions.err_access_denied";
    private static final String MSG_READ_ONLY = "permissions.err_read_only";
    
    public ExceptionTranslatorMethodInterceptor()
    {
        super();
    }

    public Object invoke(MethodInvocation mi) throws Throwable
    {
        try
        {
            return mi.proceed();
        }
        catch (net.sf.acegisecurity.AccessDeniedException ade)
        {
            throw new AccessDeniedException(MSG_ACCESS_DENIED, ade);
        }
        catch (InvalidDataAccessApiUsageException e)
        {
            // this usually occurs when the server is in read-only mode
            throw new AccessDeniedException(MSG_READ_ONLY, e);
        }
    }
}
