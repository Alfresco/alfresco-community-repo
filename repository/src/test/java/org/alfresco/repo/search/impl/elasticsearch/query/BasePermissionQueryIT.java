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

package org.alfresco.repo.search.impl.elasticsearch.query;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;

public abstract class BasePermissionQueryIT extends ElasticsearchBaseQueryIT
{

    public static final String TEST_USER = "one";
    public static final String MAIN_CREATOR = "two";
    public static final String USER_WITH_GROUPS = "three";
    protected NodeRef yellowTaxi;
    protected NodeRef taxiYellow;

    @Before
    public void initDocuments()
    {
        AuthenticationUtil.runAsSystem(() -> createUser(MAIN_CREATOR));
        AuthenticationUtil.runAs(() -> {
            // Below the basic documents created from MAIN_CREATOR. Only the owner (MAIN_CREATOR), the admin and who belongs to groups can view these documents.
            yellowTaxi = indexDocument("document name one", "yellow taxi test", new Date(), "GROUP_ABC");
            taxiYellow = indexDocument("name document two", "taxi yellow test another", new Date(), "GROUP_DEF");
            return null;
        }, MAIN_CREATOR);

    }

    @Test
    public abstract void whenSearchAsUser();

    @Test
    public abstract void whenSearchAsUserBelongingsToAGroup();

    @Test
    public abstract void whenSearchAsOwner();

    @Test
    public abstract void whenSearchAsOwnerWithRoleOwner();

    @Test
    public abstract void whenSearchAsAdmin();

    @Test
    public abstract void whenSearchAsSystemUser();

}
