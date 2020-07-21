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
package org.alfresco.repo.admin.patch.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.management.subsystems.ChildApplicationContextManager;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.RepositoryAuthenticationDao;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import java.io.Serializable;
import java.util.Map;

/**
 * Patch to reset the admin user's default password to favour md4 instead of sha254
 *
 * This patch is run per tenant (that's the default for AbstractPatch)
 *
 * @author Gethin James
 */
public class AdminUserPatch extends AbstractPatch implements InitializingBean {

    private static final String MSG_START = "patch.updateAdminUserWhenDefault.start";
    private static final String MSG_RESULT = "patch.updateAdminUserWhenDefault.result";
    private static final String MSG_NO_ACTION ="patch.updateAdminUserWhenDefault.noaction";
    private static final Log logger = LogFactory.getLog(AdminUserPatch.class);

    public static String DEFAULT_SHA = "f378d5d7b947d5c26f478e21819e7ec3a6668c8149b050d086c64447bc40173b";

    private ChildApplicationContextManager authenticationContextManager;
    private RepositoryAuthenticationDao authenticationDao;

    public void setAuthenticationContextManager(ChildApplicationContextManager authenticationContextManager) {
        this.authenticationContextManager = authenticationContextManager;
    }

    @Override
    protected String applyInternal() throws Exception {

        StringBuilder result = new StringBuilder(I18NUtil.getMessage(MSG_START));

        //If there's no RepositoryAuthenticationDao then there's no need for this patch to run
        if (authenticationDao != null)
        {
            final String adminUsername = AuthenticationUtil.getAdminUserName();
            final NodeRef userNodeRef  = authenticationDao.getUserOrNull(adminUsername);

            if (userNodeRef!= null)
            {
                Map<QName, Serializable> userProperties = nodeService.getProperties(userNodeRef);
                String sha256 = (String) userProperties.get(ContentModel.PROP_PASSWORD_SHA256);
                if (DEFAULT_SHA.equals(sha256))
                {
                    // I am not going to disable any behaviours because authenticationDao.onUpdateUserProperties fires
                    // that removes Authentication from the cache

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Removing password sha256 hash for user: " + adminUsername);
                    }

                    // The SHA256 is set to the default (i.e. admin) so i will remove it
                    nodeService.removeProperty(userNodeRef, ContentModel.PROP_PASSWORD_SHA256);

                    result.append(I18NUtil.getMessage(MSG_RESULT,adminUsername));

                }
            }
        }

        return result.toString();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ParameterCheck.mandatory("authenticationContextManager", authenticationContextManager);

        //Attempt to get RepositoryAuthenticationDao from the subsystem
        for(String contextName : authenticationContextManager.getInstanceIds())
        {
            ApplicationContext ctx = authenticationContextManager.getApplicationContext(contextName);
            try
            {
                authenticationDao = (RepositoryAuthenticationDao)
                        ctx.getBean(RepositoryAuthenticationDao.class);
            } catch(NoSuchBeanDefinitionException e) {}
        }

    }
}
