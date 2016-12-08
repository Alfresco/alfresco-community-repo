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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.Groups;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.data.Group;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.util.GUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * V1 REST API tests for managing Groups
 *
 * @author cturlica
 */
public class GroupsTest extends AbstractSingleNetworkSiteTest
{
    protected AuthorityService authorityService;

    private String rootGroupName = null;
    private Group groupA = null;
    private Group groupB = null;

    @Before
    public void setup() throws Exception
    {
        super.setup();

        authorityService = (AuthorityService) applicationContext.getBean("AuthorityService");
    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    @Test
    public void testGetGroups() throws Exception
    {
        try
        {
            createAuthorityContext(user1);

            setRequestContext(user1);

            testGetGroupsSorting();
            testGetGroupsWithInclude();
            testGetGroupsSkipPaging();
            testGetGroupsByIsRoot(true);
            testGetGroupsByIsRoot(false);
        }
        finally
        {
            clearAuthorityContext();
        }
    }

    private void testGetGroupsSkipPaging() throws Exception
    {
        // +ve: check skip count.
        {
            // Sort params
            Map<String, String> otherParams = new HashMap<>();
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, false);

            // Paging and list groups

            int skipCount = 0;
            int maxItems = 4;
            Paging paging = getPaging(skipCount, maxItems);

            ListResponse<Group> resp = getGroups(paging, otherParams);

            // Paging and list groups with skip count.

            skipCount = 2;
            maxItems = 2;
            paging = getPaging(skipCount, maxItems);

            ListResponse<Group> sublistResponse = getGroups(paging, otherParams);

            List<Group> expectedSublist = sublist(resp.getList(), skipCount, maxItems);
            checkList(expectedSublist, sublistResponse.getPaging(), sublistResponse);
        }

        // -ve: check skip count.
        {
            getGroups(getPaging(-1, null), null, "", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void testGetGroupsSorting() throws Exception
    {
        // orderBy=sortColumn should be the same to orderBy=sortColumn ASC
        {
            // paging
            Paging paging = getPaging(0, Integer.MAX_VALUE);

            Map<String, String> otherParams = new HashMap<>();

            // Default order.
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, null);

            ListResponse<Group> resp = getGroups(paging, otherParams);
            List<Group> groups = resp.getList();
            assertTrue("groups order not valid", groups.indexOf(groupA) < groups.indexOf(groupB));

            // Ascending order.
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, true);

            ListResponse<Group> respOrderAsc = getGroups(paging, otherParams);

            checkList(respOrderAsc.getList(), resp.getPaging(), resp);
        }

        // Sorting should be the same regardless of implementation (canned query
        // or postprocessing).
        {
            // paging
            Paging paging = getPaging(0, Integer.MAX_VALUE);

            Map<String, String> otherParams = new HashMap<>();
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, null);

            // Get and sort groups using canned query.
            ListResponse<Group> respCannedQuery = getGroups(paging, otherParams);

            // Get and sort groups using postprocessing.
            otherParams.put("where", "(isRoot=true)");
            ListResponse<Group> respPostProcess = getGroups(paging, otherParams);

            List<Group> expected = respCannedQuery.getList();
            expected.retainAll(respPostProcess.getList());

            checkList(expected, respPostProcess.getPaging(), respPostProcess);
        }

        // Sort by displayName.
        {
            // paging
            Paging paging = getPaging(0, Integer.MAX_VALUE);

            Map<String, String> otherParams = new HashMap<>();

            // Default order.
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, true);

            ListResponse<Group> resp = getGroups(paging, otherParams);
            List<Group> groups = resp.getList();
            assertTrue("groups order not valid", groups.indexOf(groupA) < groups.indexOf(groupB));
        }

        // Sort by id.
        {
            // paging
            Paging paging = getPaging(0, Integer.MAX_VALUE);

            Map<String, String> otherParams = new HashMap<>();
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_ID, false);

            // list sites
            ListResponse<Group> resp = getGroups(paging, otherParams);

            List<Group> groups = resp.getList();
            assertTrue("groups order not valid", groups.indexOf(groupB) < groups.indexOf(groupA));
        }

        // Multiple sort fields not allowed.
        {
            // paging
            Paging paging = getPaging(0, Integer.MAX_VALUE);
            Map<String, String> otherParams = new HashMap<>();
            otherParams.put("orderBy", org.alfresco.rest.api.Groups.PARAM_ID + " ASC," + org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME + " ASC");

            getGroups(paging, otherParams, "", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void testGetGroupsWithInclude() throws Exception
    {
        // paging
        int maxItems = 2;
        Paging paging = getPaging(0, maxItems);

        Map<String, String> otherParams = new HashMap<>();

        // Validate that by default optionally fields aren't returned.
        {
            // list sites
            ListResponse<Group> resp = getGroups(paging, null);

            // check results
            assertNotNull(resp);
            assertNotNull(resp.getList());
            assertFalse(resp.getList().isEmpty());

            assertEquals(maxItems, resp.getList().size());

            resp.getList().forEach(group -> validateGroupDefaultFields(group));
        }

        // Check include parent ids.
        {
            otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_PARENT_IDS);

            // list sites
            ListResponse<Group> resp = getGroups(paging, otherParams);

            // check results
            assertEquals(maxItems, resp.getList().size());

            resp.getList().forEach(group ->
            {
                assertNotNull(group);

                assertNotNull(group.getParentIds());
                assertFalse(group.getParentIds().isEmpty());
            });
        }

        // Check include zones.
        {
            otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_ZONES);

            // list sites
            ListResponse<Group> resp = getGroups(paging, otherParams);

            // check results
            assertEquals(maxItems, resp.getList().size());

            resp.getList().forEach(group ->
            {
                assertNotNull(group);

                assertNotNull(group.getZones());
                assertFalse(group.getZones().isEmpty());
            });
        }
    }

    private void testGetGroupsByIsRoot(boolean isRoot) throws Exception
    {
        // Sort params
        Map<String, String> otherParams = new HashMap<>();
        otherParams.put("where", "(isRoot=" + isRoot + ")");

        // Paging
        Paging paging = getPaging(0, 4);

        ListResponse<Group> resp = getGroups(paging, otherParams);
        resp.getList().forEach(group -> {
            validateGroupDefaultFields(group);
            assertEquals("isRoot was expected to be " + isRoot, isRoot, group.getIsRoot());
        });
    }

    private ListResponse<Group> getGroups(final PublicApiClient.Paging paging, Map<String, String> otherParams, String errorMessage, int expectedStatus) throws Exception
    {
        final Groups groupsProxy = publicApiClient.groups();
        return groupsProxy.getGroups(createParams(paging, otherParams), errorMessage, expectedStatus);
    }

    private ListResponse<Group> getGroups(final PublicApiClient.Paging paging, Map<String, String> otherParams) throws Exception
    {
        return getGroups(paging, otherParams, "Failed to get groups", HttpServletResponse.SC_OK);
    }

    private void addOrderBy(Map<String, String> otherParams, String sortColumn, Boolean asc)
    {
        otherParams.put("orderBy", sortColumn + (asc != null ? " " + (asc ? SortColumn.ASCENDING : SortColumn.DESCENDING) : ""));
    }

    /**
     * Creates authority context.
     *
     * @param userName
     *            The user to run as.
     */
    private void createAuthorityContext(String userName)
    {
        String groupName = "Group_ROOT" + GUID.generate();

        AuthenticationUtil.setRunAsUser(userName);
        if (rootGroupName == null)
        {
            rootGroupName = authorityService.getName(AuthorityType.GROUP, groupName);
        }

        if (!authorityService.authorityExists(rootGroupName))
        {
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

            rootGroupName = authorityService.createAuthority(AuthorityType.GROUP, groupName);

            String groupBAuthorityName = authorityService.createAuthority(AuthorityType.GROUP, "Test_GroupB" + GUID.generate());
            authorityService.addAuthority(rootGroupName, groupBAuthorityName);

            String groupAAuthorityName = authorityService.createAuthority(AuthorityType.GROUP, "Test_GroupA" + GUID.generate());
            authorityService.addAuthority(rootGroupName, groupAAuthorityName);

            authorityService.addAuthority(groupAAuthorityName, user1);
            authorityService.addAuthority(groupBAuthorityName, user2);

            groupA = new Group();
            groupA.setId(groupAAuthorityName);

            groupB = new Group();
            groupB.setId(groupBAuthorityName);
        }
    }

    /**
     * Clears authority context: removes root group and all child groups.
     */
    private void clearAuthorityContext()
    {
        if (authorityService.authorityExists(rootGroupName))
        {
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            authorityService.deleteAuthority(rootGroupName, true);
        }
    }

    private void validateGroupDefaultFields(Group group)
    {
        assertNotNull(group);
        assertNotNull(group.getId());
        assertNotNull(group.getDisplayName());
        assertNotNull(group.getIsRoot());

        // Optionally included.
        assertNull(group.getParentIds());
        assertNull(group.getZones());
    }
}
