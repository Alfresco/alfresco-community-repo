/*
 * #%L
 * Alfresco Remote API
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

package org.alfresco.rest.api.tests;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.Sites;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.data.SiteGroup;
import org.alfresco.rest.api.tests.client.data.SiteMember;
import org.alfresco.rest.api.tests.client.data.SiteRole;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TestSiteGroups extends AbstractBaseApiTest
{
    protected AuthorityService authorityService;
    private String groupName = null;
    private PublicApiClient.Paging paging = getPaging(0, 10);
    private PublicApiClient.ListResponse<SiteMember> siteMembers = null;

    @Before
    public void setup() throws Exception
    {
        super.setup();
        authorityService = (AuthorityService) applicationContext.getBean("AuthorityService");
    }

    @Test
    public void shouldCrudSiteGroups() throws Exception
    {
        Sites sitesProxy = publicApiClient.sites();
        try
        {
            groupName = createAuthorityContext(user1);
            setRequestContext(networkOne.getId(), DEFAULT_ADMIN, DEFAULT_ADMIN_PWD);

            TestSite site = TenantUtil.runAsUserTenant(() -> networkOne.createSite(SiteVisibility.PRIVATE), DEFAULT_ADMIN, networkOne.getId());


            SiteGroup response = sitesProxy.addGroup(site.getSiteId(), new SiteGroup(groupName, SiteRole.SiteCollaborator.name()));
            assertEquals(response.getGroup().getId(), groupName);
            assertEquals(response.getRole(), SiteRole.SiteCollaborator.name());

            response = sitesProxy.getGroup(site.getSiteId(), groupName);
            assertEquals(response.getGroup().getId(), groupName);
            assertEquals(response.getRole(), SiteRole.SiteCollaborator.name());

            siteMembers = sitesProxy.getSiteMembers(site.getSiteId(), createParams(paging, null));
            assertEquals(siteMembers.getList().size(), 3);

            Map<String, String> params = new HashMap<>(1);
            params.put("where", "(isMemberOfGroup=false)");
            siteMembers = sitesProxy.getSiteMembers(site.getSiteId(), createParams(paging, params));
            assertFalse(siteMembers.getList().get(0).isMemberOfGroup());
            assertEquals(siteMembers.getList().size(), 1);

            params = new HashMap<>(1);
            params.put("where", "(isMemberOfGroup=true)");
            siteMembers = sitesProxy.getSiteMembers(site.getSiteId(), createParams(paging, params));
            assertEquals(siteMembers.getList().size(), 3);

            PublicApiClient.ListResponse<SiteGroup> groups = sitesProxy.getGroups(site.getSiteId(), createParams(paging, null));
            assertEquals(groups.getList().size(), 1);
            assertEquals(groups.getList().get(0).getRole(), SiteRole.SiteCollaborator.name());

            response = sitesProxy.updateGroup(site.getSiteId(), new SiteGroup(groupName, SiteRole.SiteContributor.name()));
            groups = sitesProxy.getGroups(site.getSiteId(), createParams(paging, null));
            assertEquals(groups.getList().size(), 1);
            assertEquals(groups.getList().get(0).getRole(), SiteRole.SiteContributor.name());

            sitesProxy.deleteGroup(site.getSiteId(), response.getId());
            groups = sitesProxy.getGroups(site.getSiteId(), createParams(paging, null));
            assertEquals(groups.getList().size(), 0);

            siteMembers = sitesProxy.getSiteMembers(site.getSiteId(), createParams(paging, null));
            assertEquals(siteMembers.getList().size(), 1);
        }
        finally
        {
            clearAuthorityContext(groupName);
        }
    }

    @Test
    public void shouldAddGroup()  throws Exception
    {
        Sites sitesProxy = publicApiClient.sites();
        try
        {
            groupName = createAuthorityContext(user1);
            setRequestContext(networkOne.getId(), DEFAULT_ADMIN, DEFAULT_ADMIN_PWD);

            TestSite site = TenantUtil.runAsUserTenant(() -> networkOne.createSite(SiteVisibility.PRIVATE), DEFAULT_ADMIN, networkOne.getId());

            // Should throw 404 error
            try
            {
                sitesProxy.addGroup(site.getSiteId(), new SiteGroup(GUID.generate(), SiteRole.SiteCollaborator.name()));
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
            }

            SiteGroup response = sitesProxy.addGroup(site.getSiteId(), new SiteGroup(groupName, SiteRole.SiteCollaborator.name()));
            assertEquals(response.getGroup().getId(), groupName);
            assertEquals(response.getRole(), SiteRole.SiteCollaborator.name());

            // Should throw 409 error
            try
            {
                sitesProxy.addGroup(site.getSiteId(), new SiteGroup(groupName, SiteRole.SiteCollaborator.name()));
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.SC_CONFLICT, e.getHttpResponse().getStatusCode());
            }

            siteMembers = sitesProxy.getSiteMembers(site.getSiteId(), createParams(paging, null));
            assertEquals(siteMembers.getList().size(), 3);

            PublicApiClient.ListResponse<SiteGroup> groups = sitesProxy.getGroups(site.getSiteId(), createParams(paging, null));
            assertEquals(groups.getList().size(), 1);
            assertEquals(groups.getList().get(0).getRole(), SiteRole.SiteCollaborator.name());

        }
        finally
        {
            clearAuthorityContext(groupName);
        }
    }

    @Test
    public void shouldUpdateGroup() throws Exception
    {
        Sites sitesProxy = publicApiClient.sites();
        try
        {
            groupName = createAuthorityContext(user1);
            setRequestContext(networkOne.getId(), DEFAULT_ADMIN, DEFAULT_ADMIN_PWD);

            TestSite site = TenantUtil.runAsUserTenant(() -> networkOne.createSite(SiteVisibility.PRIVATE), DEFAULT_ADMIN, networkOne.getId());

            // Should throw 400 error
            try
            {
                sitesProxy.updateGroup(site.getSiteId(), new SiteGroup(GUID.generate(), SiteRole.SiteCollaborator.name()));
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
            }

            SiteGroup response = sitesProxy.addGroup(site.getSiteId(), new SiteGroup(groupName, SiteRole.SiteCollaborator.name()));
            assertEquals(response.getGroup().getId(), groupName);
            assertEquals(response.getRole(), SiteRole.SiteCollaborator.name());

            response = sitesProxy.updateGroup(site.getSiteId(), new SiteGroup(groupName, SiteRole.SiteContributor.name()));
            assertEquals(response.getGroup().getId(), groupName);
            assertEquals(response.getRole(), SiteRole.SiteContributor.name());

            siteMembers = sitesProxy.getSiteMembers(site.getSiteId(), createParams(paging, null));
            assertEquals(siteMembers.getList().size(), 3);
            assertEquals(siteMembers.getList().get(1).getRole(), SiteRole.SiteContributor.name());
            assertEquals(siteMembers.getList().get(2).getRole(), SiteRole.SiteContributor.name());

        }
        finally
        {
            clearAuthorityContext(groupName);
        }
    }

    @Test
    public void shouldDeleteGroup() throws Exception
    {
        Sites sitesProxy = publicApiClient.sites();
        try
        {
            groupName = createAuthorityContext(user1);
            setRequestContext(networkOne.getId(), DEFAULT_ADMIN, DEFAULT_ADMIN_PWD);

            TestSite site = TenantUtil.runAsUserTenant(() -> networkOne.createSite(SiteVisibility.PRIVATE), DEFAULT_ADMIN, networkOne.getId());

            // Should throw 400 error
            try
            {
                sitesProxy.updateGroup(site.getSiteId(), new SiteGroup(GUID.generate(), SiteRole.SiteCollaborator.name()));
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
            }

            SiteGroup response = sitesProxy.addGroup(site.getSiteId(), new SiteGroup(groupName, SiteRole.SiteCollaborator.name()));
            assertEquals(response.getGroup().getId(), groupName);
            assertEquals(response.getRole(), SiteRole.SiteCollaborator.name());

            siteMembers = sitesProxy.getSiteMembers(site.getSiteId(), createParams(paging, null));
            assertEquals(siteMembers.getList().size(), 3);

            sitesProxy.deleteGroup(site.getSiteId(), response.getId());

            siteMembers = sitesProxy.getSiteMembers(site.getSiteId(), createParams(paging, null));
            assertEquals(siteMembers.getList().size(), 1);
        }
        finally
        {
            clearAuthorityContext(groupName);
        }
    }

    private String createAuthorityContext(String userName) throws PublicApiException
    {
        String groupName = "Test_GroupA" + GUID.generate();
        AuthenticationUtil.setRunAsUser(userName);

        groupName = authorityService.getName(AuthorityType.GROUP, groupName);

        if (!authorityService.authorityExists(groupName))
        {
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

            groupName = authorityService.createAuthority(AuthorityType.GROUP, groupName);
            authorityService.setAuthorityDisplayName(groupName, "Test Group A");
        }


        authorityService.addAuthority(groupName, user1);
        authorityService.addAuthority(groupName, user2);

        return groupName;
    }

    private void clearAuthorityContext(String groupName)
    {
        if (groupName != null && authorityService.authorityExists(groupName))
        {
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            authorityService.deleteAuthority(groupName, true);
        }
    }

    @Override
    public String getScope()
    {
        return "public";
    }
}
