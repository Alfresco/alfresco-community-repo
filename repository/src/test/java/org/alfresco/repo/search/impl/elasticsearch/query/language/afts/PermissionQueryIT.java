/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import java.util.Date;

import org.alfresco.repo.search.impl.elasticsearch.permission.GlobalReaders;
import org.alfresco.repo.search.impl.elasticsearch.query.BasePermissionQueryIT;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;

public class PermissionQueryIT extends BasePermissionQueryIT
{
    @Override
    public void whenSearchAsUser()
    {
        NodeRef myTaxi = AuthenticationUtil.runAs(() -> indexDocument("my taxi"), MAIN_CREATOR);
        assertContainsOnly(AuthenticationUtil.runAs(() -> aftsSearch("taxi"), TEST_USER), myTaxi);
    }

    @Override
    public void whenSearchAsUserBelongingsToAGroup()
    {
        AuthenticationUtil.runAsSystem(() -> {
            createUser(USER_WITH_GROUPS);
            safeCreateAuthority("GROUP_ABC");
            safeCreateAuthority("GROUP_DEF");

            addAuthorityIfNotPresent(USER_WITH_GROUPS, "GROUP_ABC");
            addAuthorityIfNotPresent(USER_WITH_GROUPS, "GROUP_DEF");
            return null;
        });

        assertContainsOnly(AuthenticationUtil.runAs(() -> aftsSearch("taxi"), USER_WITH_GROUPS), yellowTaxi, taxiYellow);
    }

    @Override
    public void whenSearchAsOwner()
    {
        // TEST_USER can view this document because he is the owner
        AuthenticationUtil.runAs(() -> {
            NodeRef myTaxi = indexDocument("my taxi", "my taxi", new Date(), "GROUP_C");
            assertContainsOnly(aftsSearch("taxi"), myTaxi);
            return null;
        }, TEST_USER);
    }

    @Override
    public void whenSearchAsOwnerWithRoleOwner()
    {
        GlobalReaders.getReaders().remove(PermissionService.OWNER_AUTHORITY);
        try
        {
            AuthenticationUtil.runAs(() -> {
                NodeRef myTaxi = indexDocument("my taxi", "my taxi", new Date(), PermissionService.OWNER_AUTHORITY,
                        "GROUP_C");
                NodeRef notInResult = indexDocument("another taxi", "another taxi", new Date(), "GROUP_C");
                // if GlobalReaders hasn't the OWNER_AUTHORITY the owner cannot search for documents he owns if the documents hasn't ROLE_OWNER
                assertContainsOnly(aftsSearch("taxi"), myTaxi);
                return null;
            }, TEST_USER);
        }
        finally
        {
            // restore global readers
            GlobalReaders.getReaders().add(PermissionService.OWNER_AUTHORITY);
        }

    }

    @Override
    public void whenSearchAsAdmin()
    {
        // an admin can view all documents, even if he hasn't any permission on them
        assertContainsOnly(AuthenticationUtil.runAs(() -> aftsSearch("taxi"), "admin"), yellowTaxi, taxiYellow);
    }

    @Override
    public void whenSearchAsSystemUser()
    {
        // a system user can view all documents, even if he hasn't any permission on them
        assertContainsOnly(AuthenticationUtil.runAsSystem(() -> aftsSearch("taxi")), yellowTaxi, taxiYellow);
    }
}
