/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.imap;

import java.lang.reflect.Method;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author arsenyko
 *
 */
public class AlfrescoImapFolderAccessInterceptor implements MethodInterceptor
{
    private Log logger = LogFactory.getLog(AlfrescoImapFolderAccessInterceptor.class);

    private PermissionService permissionService;

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable
    {
        Object[] args = mi.getArguments();
        Method method = mi.getMethod();
        String methodName = method.getName();
        if (logger.isDebugEnabled())
        {
            logger.debug("Check the cache [" + methodName + "]");
        }
        if ("contains".equals(methodName))
        {
            //XXX: Do we need a check for permissions here?
            String mailboxName = (String) args[0];
            if (logger.isDebugEnabled())
            {
                logger.debug("Check the cache [" + methodName + "] for '" + mailboxName + "'");
            }
            boolean containsResult = (Boolean) mi.proceed();
            if (logger.isDebugEnabled())
            {
                logger.debug("The cache " + (containsResult ? "contains" : "does't contain") + " '" + mailboxName + "'");
            }
            return containsResult;
        }
        else if ("get".equals(methodName))
        {
            String mailboxName = (String) args[0];
            if (logger.isDebugEnabled())
            {
                logger.debug("Check the cache [" + methodName + "] for '" + mailboxName + "'");
            }
            AlfrescoImapFolder folder = (AlfrescoImapFolder) mi.proceed();
            if (logger.isDebugEnabled())
            {
                logger.debug("The cache " + (folder != null ? "contains" : "does't contain") + " '" + mailboxName + "'");
            }
            if (folder != null)
            {
                NodeRef nodeRef = folder.getFolderInfo().getNodeRef();
                boolean accessAllowed = permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.ALLOWED
                                     && permissionService.hasPermission(nodeRef, PermissionService.READ_CHILDREN) == AccessStatus.ALLOWED;
                if (logger.isDebugEnabled())
                {
                    logger.debug("Access " + (accessAllowed ? "allowed" : "denied") + " to '" + mailboxName + "' for user '" + AuthenticationUtil.getFullyAuthenticatedUser() +  "'");
                }
                if (!accessAllowed)
                    return null;
            }
            return folder;
        }
        return mi.proceed();
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public PermissionService getPermissionService()
    {
        return permissionService;
    }

}
