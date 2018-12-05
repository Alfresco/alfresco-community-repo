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
package org.alfresco.repo.security.authority;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.Serializable;
import java.util.*;

public class AuthorityTypeBehaviour implements NodeServicePolicies.OnUpdatePropertiesPolicy, InitializingBean
{
    private static Log logger = LogFactory.getLog(AuthorityTypeBehaviour.class);

    private static final String USERNAME_FIELD = "userName";
    private static final String INVALID_USERNAME_VALUE = "";

    private PolicyComponent policyComponent;

    private AuthorityService authorityService;


    public AuthorityTypeBehaviour()
    {
        super();
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    public void init()
    {
        // Listen out for updates to persons and authority containers to handle renames
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), ContentModel.TYPE_AUTHORITY, new JavaBehaviour(
                this, "onUpdateProperties"));
    }

    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if (modifyingOwnAccount(before, after))
        {
            return;
        }

        if (!(AuthenticationUtil.isRunAsUserTheSystemUser() || authorityService.hasAdminAuthority()))
        {
            throw new AccessDeniedException("Only users with ROLE_ADMINISTRATOR are allowed to manage users.");
        }
    }

    private boolean modifyingOwnAccount(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        String beforeUsername = findUsernameInProperties(before, USERNAME_FIELD, INVALID_USERNAME_VALUE);
        String afterUsername = findUsernameInProperties(after, USERNAME_FIELD, INVALID_USERNAME_VALUE);
        if (afterUsername.equals(beforeUsername))
        {
            String authenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
            return beforeUsername.equals(authenticatedUser);
        }
        else
        {
            return false;
        }
    }

    private String findUsernameInProperties(Map<QName, Serializable> map, String usernameField, String invalidValue)
    {
        Optional<QName> first = map.keySet().stream().filter(q -> q.getLocalName().equals(usernameField)).findFirst();
        if (first.isPresent())
        {
            return map.get(first.get()).toString();
        }
        return invalidValue;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "authorityService", authorityService);
    };

}
