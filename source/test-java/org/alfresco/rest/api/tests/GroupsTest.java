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
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.Groups;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Group;
import org.alfresco.rest.api.tests.client.data.GroupMember;
import org.alfresco.rest.api.tests.client.data.Person;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.GUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static org.junit.Assert.*;

/**
 * V1 REST API tests for managing Groups
 *
 * @author cturlica
 */
public class GroupsTest extends AbstractSingleNetworkSiteTest
{
    private static final String MEMBER_TYPE_GROUP = "GROUP";
    private static final String MEMBER_TYPE_PERSON = "PERSON";
    private static final String GROUP_EVERYONE = "GROUP_EVERYONE";

    protected AuthorityService authorityService;

    private String rootGroupName = null;
    private Group rootGroup = null;
    private Group groupA = null;
    private Group groupB = null;
    private GroupMember groupMemberA = null;
    private GroupMember groupMemberB = null;
    private GroupMember personMember = null;

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
            testGetGroupsWithZoneFilter();
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
        {
            // paging
            Paging paging = getPaging(0, Integer.MAX_VALUE);

            Map<String, String> otherParams = new HashMap<>();
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_ID, null);

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

    private void testGetGroupsWithZoneFilter() throws Exception
    {
        // Filter by zone
        {
            Paging paging = getPaging(0, Integer.MAX_VALUE);
            Map<String, String> otherParams = new HashMap<>();
            otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_ZONES);

            otherParams.put("where", "(zones in ('APP.DEFAULT'))");

            ListResponse<Group> response = getGroups(paging, otherParams);
            List<Group> groups = response.getList();

            assertFalse(groups.isEmpty());
            // All groups should contain the selected zone.
            groups.forEach(group -> assertTrue(group.getZones().contains("APP.DEFAULT")));
        }

        // Filter by zone - custom zones, "include" them in the response.
        {
            Paging paging = getPaging(0, Integer.MAX_VALUE);
            Map<String, String> otherParams = new HashMap<>();
            // Ensure predictable result ordering
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, true);
            otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_ZONES);

            otherParams.put("where", "(zones in ('APITEST.MYZONE'))");

            ListResponse<Group> response = getGroups(paging, otherParams);
            List<Group> groups = response.getList();

            // We know exactly which groups are in the selected zone.
            assertEquals(3, groups.size());
            assertEquals(rootGroup, groups.get(0));
            assertEquals(groupA, groups.get(1));
            assertEquals(groupB, groups.get(2));
            groups.forEach(group -> assertTrue(group.getZones().contains("APITEST.MYZONE")));

            otherParams.put("where", "(zones in ('APITEST.ANOTHER'))");
            response = getGroups(paging, otherParams);
            groups = response.getList();

            // We know exactly which groups are in the selected zone.
            assertEquals(1, groups.size());
            assertEquals(groupA, groups.get(0));
            assertTrue(groups.get(0).getZones().contains("APITEST.MYZONE"));
            assertTrue(groups.get(0).getZones().contains("APITEST.ANOTHER"));
        }

        // Filter without "include"-ing zones
        {
            Paging paging = getPaging(0, Integer.MAX_VALUE);
            Map<String, String> otherParams = new HashMap<>();
            // Ensure predictable result ordering
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, true);

            otherParams.put("where", "(zones in ('APITEST.MYZONE'))");

            assertFalse(otherParams.containsKey("include"));
            ListResponse<Group> response = getGroups(paging, otherParams);
            List<Group> groups = response.getList();

            // We know exactly which groups are in the selected zone.
            assertEquals(3, groups.size());
            assertEquals(rootGroup, groups.get(0));
            assertEquals(groupA, groups.get(1));
            assertEquals(groupB, groups.get(2));
            // We haven't included the zones info.
            groups.forEach(group -> assertNull(group.getZones()));
        }

        // Filter zones while using where isRoot=true
        // (this causes a different query path to be used)
        {
            Paging paging = getPaging(0, Integer.MAX_VALUE);
            Map<String, String> otherParams = new HashMap<>();
            // Ensure predictable result ordering
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, true);
            otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_ZONES);

            otherParams.put("where", "(isRoot=true AND zones in ('APITEST.MYZONE'))");

            ListResponse<Group> response = getGroups(paging, otherParams);
            List<Group> groups = response.getList();

            assertEquals(1, groups.size());
            assertEquals(rootGroup, groups.get(0));
            assertTrue(groups.get(0).getZones().contains("APITEST.MYZONE"));

            // Zone that doesn't exist.
            otherParams.put("where", "(isRoot=true AND zones in ('I.DO.NOT.EXIST'))");
            response = getGroups(paging, otherParams, "Incorrect response", 200);
            groups = response.getList();
            assertTrue(groups.isEmpty());
        }

        // Filter zones while using where isRoot=false
        {
            Paging paging = getPaging(0, Integer.MAX_VALUE);
            Map<String, String> otherParams = new HashMap<>();
            // Ensure predictable result ordering
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, true);

            otherParams.put("where", "(isRoot=false AND zones in ('APITEST.MYZONE'))");

            ListResponse<Group> response = getGroups(paging, otherParams);
            List<Group> groups = response.getList();

            assertEquals(2, groups.size());
            assertEquals(groupA, groups.get(0));
            assertEquals(groupB, groups.get(1));
            // We haven't included the zones info.
            groups.forEach(group -> assertNull(group.getZones()));

            // Zone that doesn't exist.
            otherParams.put("where", "(isRoot=false AND zones in ('I.DO.NOT.EXIST'))");
            response = getGroups(paging, otherParams, "Incorrect response", 200);
            groups = response.getList();
            assertTrue(groups.isEmpty());
        }

        // -ve test: invalid zones clause
        {
            Paging paging = getPaging(0, Integer.MAX_VALUE);
            Map<String, String> otherParams = new HashMap<>();
            otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_ZONES);

            // Empty zone list
            otherParams.put("where", "(zones in ())");
            getGroups(paging, otherParams, "Incorrect response", 400);

            // Empty zone name
            otherParams.put("where", "(zones in (''))");
            getGroups(paging, otherParams, "Incorrect response", 400);

            // Too many zones
            otherParams.put("where", "(zones in ('APP.DEFAULT', 'APITEST.MYZONE'))");
            getGroups(paging, otherParams, "Incorrect response", 400);

            // "A series of unfortunate errors"
            otherParams.put("where", "(zones in ('', 'APP.DEFAULT', '', 'APITEST.MYZONE'))");
            getGroups(paging, otherParams, "Incorrect response", 400);

            // OR operator not currently supported
            otherParams.put("where", "(isRoot=true OR zones in ('APP.DEFAULT'))");
            getGroups(paging, otherParams, "Incorrect response", 400);
        }
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
    private void createAuthorityContext(String userName) throws PublicApiException
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
            authorityService.addAuthorityToZones(rootGroupName, zoneSet("APITEST.MYZONE"));

            String groupBAuthorityName = authorityService.createAuthority(AuthorityType.GROUP, "Test_GroupB" + GUID.generate());
            authorityService.addAuthority(rootGroupName, groupBAuthorityName);
            authorityService.addAuthorityToZones(groupBAuthorityName, zoneSet("APITEST.MYZONE"));

            String groupAAuthorityName = authorityService.createAuthority(AuthorityType.GROUP, "Test_GroupA" + GUID.generate());
            authorityService.addAuthority(rootGroupName, groupAAuthorityName);
            authorityService.addAuthorityToZones(groupAAuthorityName, zoneSet("APITEST.MYZONE", "APITEST.ANOTHER"));

            authorityService.addAuthority(groupAAuthorityName, user1);
            authorityService.addAuthority(groupBAuthorityName, user2);

            rootGroup = new Group();
            rootGroup.setId(rootGroupName);

            groupA = new Group();
            groupA.setId(groupAAuthorityName);

            groupB = new Group();
            groupB.setId(groupBAuthorityName);

            groupMemberA = new GroupMember();
            groupMemberA.setId(groupAAuthorityName);
            groupMemberA.setMemberType(AuthorityType.GROUP.toString());

            groupMemberB = new GroupMember();
            groupMemberB.setId(groupBAuthorityName);
            groupMemberB.setMemberType(AuthorityType.GROUP.toString());
        }

        {
            publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), networkAdmin, "admin"));
            Person personAlice = new Person();
            String aliceId = "alice-" + UUID.randomUUID() + "@" + networkOne.getId();
            personAlice.setUserName(aliceId);
            personAlice.setId(aliceId);
            personAlice.setFirstName("Alice");
            personAlice.setEmail("alison.smith@example.com");
            personAlice.setPassword("password");
            personAlice.setEnabled(true);
            PublicApiClient.People people = publicApiClient.people();
            people.create(personAlice);
            personMember = new GroupMember();
            personMember.setId(personAlice.getId());
            personMember.setMemberType(MEMBER_TYPE_PERSON);
        }
    }

    private Set<String> zoneSet(String... zones)
    {
        Set<String> zoneSet = new HashSet<>(zones.length);
        zoneSet.addAll(Arrays.asList(zones));
        return zoneSet;
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

    private void validateGroupDefaultFields(Group group, boolean ignoreOptionallyIncluded)
    {
        assertNotNull(group);
        assertNotNull(group.getId());
        assertNotNull(group.getDisplayName());
        assertNotNull(group.getIsRoot());

        if (!ignoreOptionallyIncluded)
        {
            // Optionally included.
            assertNull(group.getParentIds());
            assertNull(group.getZones());
        }
    }

    private void validateGroupDefaultFields(Group group)
    {
        validateGroupDefaultFields(group, false);
    }

    private ListResponse<GroupMember> getGroupMembers(String groupId, final PublicApiClient.Paging paging, Map<String, String> otherParams, String errorMessage, int expectedStatus) throws Exception
    {
        final Groups groupsProxy = publicApiClient.groups();
        return groupsProxy.getGroupMembers(groupId, createParams(paging, otherParams), errorMessage, expectedStatus);
    }

    private ListResponse<Group> getGroupsByPersonId(String userId, final PublicApiClient.Paging paging,
                                                    Map<String, String> otherParams) throws Exception
    {
        return getGroupsByPersonId(
                userId,
                paging,
                otherParams,
                HttpServletResponse.SC_OK);
    }

    private ListResponse<Group> getGroupsByPersonId(
            String userId,
            final PublicApiClient.Paging paging,
            Map<String, String> otherParams,
            int expectedStatus) throws Exception
    {
        final Groups groupsProxy = publicApiClient.groups();
        return groupsProxy.getGroupsByPersonId(
                userId,
                createParams(paging, otherParams),
                "Incorrect response when getting groups for user ID " + userId,
                expectedStatus);
    }

    private ListResponse<GroupMember> getGroupMembers(String groupId, final PublicApiClient.Paging paging, Map<String, String> otherParams) throws Exception
    {
        return getGroupMembers(groupId, paging, otherParams, "Failed to get group members", HttpServletResponse.SC_OK);
    }

    @Test
    public void testGetGroupMembers() throws Exception
    {
        try
        {
            createAuthorityContext(user1);

            setRequestContext(user1);

            testGetGroupMembersByGroupId();
            testGetGroupMembersSorting();
            testGetGroupMembersSkipPaging();
            testGetGroupsByMemberType();
        }
        finally
        {
            clearAuthorityContext();
        }
    }

    @Test
    public void testGetGroupsByUserId() throws Exception
    {
        try
        {
            createAuthorityContext(user1);
            canGetGroupsForUserId();
        }
        finally
        {
            clearAuthorityContext();
        }
    }

    private void canGetGroupsForUserId() throws Exception
    {
        Person personAlice;
        {
            publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), networkAdmin, "admin"));
            personAlice = new Person();
            String aliceId = "alice-" + UUID.randomUUID() + "@" + networkOne.getId();
            personAlice.setUserName(aliceId);
            personAlice.setId(aliceId);
            personAlice.setFirstName("Alice");
            personAlice.setEmail("alison.smith@example.com");
            personAlice.setPassword("password");
            personAlice.setEnabled(true);
            PublicApiClient.People people = publicApiClient.people();
            people.create(personAlice);
        }

        Groups groupsProxy = publicApiClient.groups();

        // As admin
        setRequestContext(networkOne.getId(), networkAdmin, "admin");

        // New user has only the one default group.
        {
            ListResponse<Group> groups = groupsProxy.getGroupsByPersonId(personAlice.getId(), null, "Couldn't get user's groups", 200);
            assertEquals(1L, (long) groups.getPaging().getTotalItems());
            Iterator<Group> it = groups.getList().iterator();
            assertEquals(GROUP_EVERYONE, it.next().getId());
        }

        // Add the user to a couple more groups and list them.
        {
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            authorityService.addAuthority(groupA.getId(), personAlice.getId());
            authorityService.addAuthority(groupB.getId(), personAlice.getId());

            ListResponse<Group> groups = groupsProxy.getGroupsByPersonId(personAlice.getId(), null, "Couldn't get user's groups", 200);
            assertEquals(4L, (long) groups.getPaging().getTotalItems());
            Iterator<Group> it = groups.getList().iterator();
            assertEquals(GROUP_EVERYONE, it.next().getId());
            assertEquals(rootGroupName, it.next().getId());
            assertEquals(groupA, it.next());
            assertEquals(groupB, it.next());
        }

        // Get network admin's groups by explicit ID.
        {
            ListResponse<Group> groups = groupsProxy.getGroupsByPersonId(networkAdmin, null, "Couldn't get user's groups", 200);
            assertEquals(6L, (long) groups.getPaging().getTotalItems());
        }

        // test -me- alias (as network admin)
        {
            ListResponse<Group> groups = groupsProxy.getGroupsByPersonId("-me-", null, "Couldn't get user's groups", 200);
            assertEquals(6L, (long) groups.getPaging().getCount());
            Iterator<Group> it = groups.getList().iterator();
            assertEquals("GROUP_ALFRESCO_ADMINISTRATORS", it.next().getId());
        }

        // Filter by isRoot
        {
            Map<String, String> params = new HashMap<>();

            params.put("where", "(isRoot=true)");
            ListResponse<Group> response = groupsProxy.getGroupsByPersonId("-me-", params, "Couldn't get user's groups", 200);
            List<Group> groups = response.getList();
            assertFalse(groups.isEmpty());
            // All groups should be root groups.
            groups.forEach(group -> assertTrue(group.getIsRoot()));

            params.put("where", "(isRoot=false)");
            response = groupsProxy.getGroupsByPersonId("-me-", params, "Couldn't get user's groups", 200);
            groups = response.getList();
            assertFalse(groups.isEmpty());
            // All groups should be non-root groups.
            groups.forEach(group -> assertFalse(group.getIsRoot()));
        }

        // -ve test: attempt to get groups for non-existent person
        {
            groupsProxy.getGroupsByPersonId("i-do-not-exist", null, "Incorrect response", 404);
        }

        // As Alice
        setRequestContext(networkOne.getId(), personAlice.getId(), "password");

        // test -me- alias as Alice
        {
            ListResponse<Group> groups = groupsProxy.getGroupsByPersonId("-me-", null, "Couldn't get user's groups", 200);
            assertEquals(4L, (long) groups.getPaging().getCount());
            Iterator<Group> it = groups.getList().iterator();
            assertEquals(GROUP_EVERYONE, it.next().getId());
            assertEquals(rootGroupName, it.next().getId());
            assertEquals(groupA, it.next());
            assertEquals(groupB, it.next());
        }

        // +ve: check skip count.
        {
            // Sort params
            Map<String, String> otherParams = new HashMap<>();
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, false);

            // Paging and list groups

            int skipCount = 0;
            int maxItems = 4;
            Paging paging = getPaging(skipCount, maxItems);

            ListResponse<Group> resp = getGroupsByPersonId(personAlice.getId(), paging, otherParams);

            // Paging and list groups with skip count.

            skipCount = 2;
            maxItems = 2;
            paging = getPaging(skipCount, maxItems);

            ListResponse<Group> sublistResponse = getGroupsByPersonId(personAlice.getId(), paging, otherParams);

            List<Group> expectedSublist = sublist(resp.getList(), skipCount, maxItems);
            checkList(expectedSublist, sublistResponse.getPaging(), sublistResponse);
        }

        // -ve: check skip count.
        {
            getGroupsByPersonId(personAlice.getId(), getPaging(-1, null), null, HttpServletResponse.SC_BAD_REQUEST);
        }

        // orderBy=sortColumn should be the same to orderBy=sortColumn ASC
        {
            // paging
            Paging paging = getPaging(0, Integer.MAX_VALUE);

            Map<String, String> otherParams = new HashMap<>();

            // Default order.
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, null);

            ListResponse<Group> resp = getGroupsByPersonId(personAlice.getId(), paging, otherParams);
            List<Group> groups = resp.getList();
            assertTrue("groups order not valid", groups.indexOf(groupA) < groups.indexOf(groupB));

            // Ascending order.
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, true);
            ListResponse<Group> respOrderAsc = getGroupsByPersonId(personAlice.getId(), paging, otherParams);
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
            ListResponse<Group> respCannedQuery = getGroupsByPersonId(personAlice.getId(), paging, otherParams);

            // Get and sort groups using postprocessing.
            otherParams.put("where", "(isRoot=true)");
            ListResponse<Group> respPostProcess = getGroupsByPersonId(personAlice.getId(), paging, otherParams);

            List<Group> expected = respCannedQuery.getList();
            expected.retainAll(respPostProcess.getList());

            // If this assertion fails, then the tests aren't providing any value - change them!
            assertTrue("List doesn't contain enough items for test to be conclusive.", expected.size() > 0);
            checkList(expected, respPostProcess.getPaging(), respPostProcess);
        }

        {
            // paging
            Paging paging = getPaging(0, Integer.MAX_VALUE);

            Map<String, String> otherParams = new HashMap<>();
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_ID, null);

            // Get and sort groups using canned query.
            ListResponse<Group> respCannedQuery = getGroupsByPersonId(personAlice.getId(), paging, otherParams);

            // Get and sort groups using postprocessing.
            otherParams.put("where", "(isRoot=true)");
            ListResponse<Group> respPostProcess = getGroupsByPersonId(personAlice.getId(), paging, otherParams);

            List<Group> expected = respCannedQuery.getList();
            expected.retainAll(respPostProcess.getList());

            // If this assertion fails, then the tests aren't providing any value - change them!
            assertTrue("List doesn't contain enough items for test to be conclusive.", expected.size() > 0);
            checkList(expected, respPostProcess.getPaging(), respPostProcess);
        }

        // Sort by id.
        {
            // paging
            Paging paging = getPaging(0, Integer.MAX_VALUE);

            Map<String, String> otherParams = new HashMap<>();
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_ID, false);

            // list sites
            ListResponse<Group> resp = getGroupsByPersonId(personAlice.getId(), paging, otherParams);

            List<Group> groups = resp.getList();
            assertTrue("groups order not valid", groups.indexOf(groupB) < groups.indexOf(groupA));
        }

        // Multiple sort fields not allowed.
        {
            // paging
            Paging paging = getPaging(0, Integer.MAX_VALUE);
            Map<String, String> otherParams = new HashMap<>();
            otherParams.put("orderBy", org.alfresco.rest.api.Groups.PARAM_ID + " ASC," + org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME + " ASC");

            getGroupsByPersonId(personAlice.getId(), paging, otherParams, HttpServletResponse.SC_BAD_REQUEST);
        }

        // Check include parent ids.
        {
            Paging paging = getPaging(0, Integer.MAX_VALUE);
            Map<String, String> otherParams = new HashMap<>();
            otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_PARENT_IDS);

            ListResponse<Group> resp = getGroupsByPersonId(personAlice.getId(), paging, otherParams);

            assertEquals(4, resp.getList().size());

            Iterator<Group> it = resp.getList().iterator();

            Group group = it.next();
            assertEquals(PermissionService.ALL_AUTHORITIES, group.getId());
            assertEquals(0, group.getParentIds().size());

            group = it.next();
            assertEquals(rootGroup.getId(), group.getId());
            assertEquals(0, group.getParentIds().size());

            group = it.next();
            assertEquals(groupA.getId(), group.getId());
            assertEquals(1, group.getParentIds().size());
            assertTrue(group.getParentIds().contains(rootGroup.getId()));

            group = it.next();
            assertEquals(groupB.getId(), group.getId());
            assertEquals(1, group.getParentIds().size());
            assertTrue(group.getParentIds().contains(rootGroup.getId()));
        }

        // Filter by zone, use the -me- alias.
        {
            Map<String, String> params = new HashMap<>();
            params.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_ZONES);

            params.put("where", "(zones in ('APP.DEFAULT'))");

            // Use the -me- alias
            ListResponse<Group> response = groupsProxy.
                    getGroupsByPersonId("-me-", params, "Couldn't get user's groups", 200);
            List<Group> groups = response.getList();

            assertFalse(groups.isEmpty());
            // All groups should contain the selected zone.
            groups.forEach(group -> assertTrue(group.getZones().contains("APP.DEFAULT")));
        }

        // Filter by zone, use the -me- alias.
        {
            Map<String, String> params = new HashMap<>();
            params.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_ZONES);

            params.put("where", "(zones in ('APITEST.MYZONE'))");

            // Use the -me- alias
            ListResponse<Group> response = groupsProxy.
                    getGroupsByPersonId("-me-", params, "Couldn't get user's groups", 200);
            List<Group> groups = response.getList();

            assertEquals(3, groups.size());
            // All groups should contain the selected zone.
            groups.forEach(group -> assertTrue(group.getZones().contains("APITEST.MYZONE")));
        }

        // Filter by zone - use the person's ID, without "include"-ing zones
        {
            Map<String, String> params = new HashMap<>();
            params.put("where", "(zones in ('APITEST.ANOTHER'))");

            ListResponse<Group> response = groupsProxy.
                    getGroupsByPersonId(personAlice.getId(), params, "Couldn't get user's groups", 200);
            List<Group> groups = response.getList();

            assertEquals(1, groups.size());
            // We haven't included the zone info
            groups.forEach(group -> assertNull(group.getZones()));
        }

        // Filter zones while using where isRoot=true
        // (this causes a different query path to be used)
        {
            Map<String, String> otherParams = new HashMap<>();
            // Ensure predictable result ordering
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, true);
            otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_ZONES);

            otherParams.put("where", "(isRoot=true AND zones in ('APITEST.MYZONE'))");

            ListResponse<Group> response = groupsProxy.
                    getGroupsByPersonId("-me-", otherParams, "Unexpected response", 200);
            List<Group> groups = response.getList();

            assertEquals(1, groups.size());
            assertEquals(rootGroup, groups.get(0));
            assertTrue(groups.get(0).getZones().contains("APITEST.MYZONE"));

            // Zone that doesn't exist.
            otherParams.put("where", "(isRoot=true AND zones in ('I.DO.NOT.EXIST'))");
            response = groupsProxy.
                    getGroupsByPersonId("-me-", otherParams, "Unexpected response", 200);
            groups = response.getList();
            assertTrue(groups.isEmpty());
        }

        // Filter zones while using where isRoot=false
        {
            Map<String, String> otherParams = new HashMap<>();
            // Ensure predictable result ordering
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, true);

            otherParams.put("where", "(isRoot=false AND zones in ('APITEST.MYZONE'))");

            ListResponse<Group> response = groupsProxy.
                    getGroupsByPersonId("-me-", otherParams, "Unexpected response", 200);
            List<Group> groups = response.getList();

            assertEquals(2, groups.size());
            assertEquals(groupA, groups.get(0));
            assertEquals(groupB, groups.get(1));
            // We haven't included the zones info.
            groups.forEach(group -> assertNull(group.getZones()));

            // Zone that doesn't exist.
            otherParams.put("where", "(isRoot=false AND zones in ('I.DO.NOT.EXIST'))");
            response = groupsProxy.
                    getGroupsByPersonId("-me-", otherParams, "Unexpected response", 200);
            groups = response.getList();
            assertTrue(groups.isEmpty());
        }

        // -ve test: invalid zones clause
        {
            Paging paging = getPaging(0, Integer.MAX_VALUE);
            Map<String, String> otherParams = new HashMap<>();
            otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_ZONES);

            // Empty zone list
            otherParams.put("where", "(zones in ())");
            groupsProxy.getGroupsByPersonId(personAlice.getId(), otherParams, "Incorrect response", 400);

            // Empty zone name
            otherParams.put("where", "(zones in (''))");
            groupsProxy.getGroupsByPersonId(personAlice.getId(), otherParams, "Incorrect response", 400);

            // Too many zones
            otherParams.put("where", "(zones in ('APP.DEFAULT', 'APITEST.MYZONE'))");
            groupsProxy.getGroupsByPersonId(personAlice.getId(), otherParams, "Incorrect response", 400);

            // "A series of unfortunate errors"
            otherParams.put("where", "(zones in ('', 'APP.DEFAULT', '', 'APITEST.MYZONE'))");
            groupsProxy.getGroupsByPersonId(personAlice.getId(), otherParams, "Incorrect response", 400);

            // OR operator not currently supported
            otherParams.put("where", "(isRoot=true OR zones in ('APP.DEFAULT'))");
            groupsProxy.getGroupsByPersonId(personAlice.getId(), otherParams, "Incorrect response", 400);
        }
    }

    private void testGetGroupMembersByGroupId() throws Exception
    {
        Paging paging = getPaging(0, 4);

        getGroupMembers("", paging, null, "", HttpServletResponse.SC_BAD_REQUEST);
        getGroupMembers("invalidGroupId", paging, null, "", HttpServletResponse.SC_NOT_FOUND);
    }

    private void testGetGroupMembersSorting() throws Exception
    {
        // orderBy=sortColumn should be the same to orderBy=sortColumn ASC
        {
            // paging
            Paging paging = getPaging(0, Integer.MAX_VALUE);

            Map<String, String> otherParams = new HashMap<>();

            // Default order.
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, null);

            ListResponse<GroupMember> resp = getGroupMembers(rootGroupName, paging, otherParams);
            List<GroupMember> groupMembers = resp.getList();
            assertTrue("group members order not valid", groupMembers.indexOf(groupMemberA) < groupMembers.indexOf(groupMemberB));

            // Ascending order
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, true);

            ListResponse<GroupMember> respOrderAsc = getGroupMembers(rootGroupName, paging, otherParams);

            checkList(respOrderAsc.getList(), resp.getPaging(), resp);
        }

        // Sort by displayName.
        {
            // paging
            Paging paging = getPaging(0, Integer.MAX_VALUE);

            Map<String, String> otherParams = new HashMap<>();

            // Default order.
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, true);

            ListResponse<GroupMember> resp = getGroupMembers(rootGroupName, paging, otherParams);
            List<GroupMember> groupMembers = resp.getList();
            assertTrue("group members order not valid", groupMembers.indexOf(groupMemberA) < groupMembers.indexOf(groupMemberB));
        }

        // Sort by id.
        {
            // paging
            Paging paging = getPaging(0, Integer.MAX_VALUE);

            Map<String, String> otherParams = new HashMap<>();
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_ID, false);

            // list sites
            ListResponse<GroupMember> resp = getGroupMembers(rootGroupName, paging, otherParams);

            List<GroupMember> groupMembers = resp.getList();
            assertTrue("group members order not valid", groupMembers.indexOf(groupMemberB) < groupMembers.indexOf(groupMemberA));
        }

        // Multiple sort fields not allowed.
        {
            // paging
            Paging paging = getPaging(0, Integer.MAX_VALUE);
            Map<String, String> otherParams = new HashMap<>();
            otherParams.put("orderBy", org.alfresco.rest.api.Groups.PARAM_ID + " ASC," + org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME + " ASC");

            getGroupMembers(rootGroupName, paging, otherParams, "", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void testGetGroupMembersSkipPaging() throws Exception
    {
        // +ve: check skip count.
        {
            // Sort params
            Map<String, String> otherParams = new HashMap<>();
            addOrderBy(otherParams, org.alfresco.rest.api.Groups.PARAM_DISPLAY_NAME, false);

            // Paging and list groups

            int skipCount = 0;
            int maxItems = 2;
            Paging paging = getPaging(skipCount, maxItems);

            ListResponse<GroupMember> resp = getGroupMembers(rootGroupName, paging, otherParams);

            // Paging and list groups with skip count.

            skipCount = 1;
            maxItems = 1;
            paging = getPaging(skipCount, maxItems);

            ListResponse<GroupMember> sublistResponse = getGroupMembers(rootGroupName, paging, otherParams);

            List<GroupMember> expectedSublist = sublist(resp.getList(), skipCount, maxItems);
            checkList(expectedSublist, sublistResponse.getPaging(), sublistResponse);
        }

        // -ve: check skip count.
        {
            getGroupMembers(rootGroupName, getPaging(-1, null), null, "", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void testGetGroupsByMemberType() throws Exception
    {
        testGetGroupsByMemberType(rootGroupName, org.alfresco.rest.api.Groups.PARAM_MEMBER_TYPE_GROUP);
        testGetGroupsByMemberType(groupB.getId(), org.alfresco.rest.api.Groups.PARAM_MEMBER_TYPE_PERSON);

        // Invalid member type
        {
            Map<String, String> otherParams = new HashMap<>();
            otherParams.put("where", "(memberType=invalidMemberType)");

            getGroupMembers(rootGroupName, getPaging(0, 4), otherParams, "", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void testGetGroupsByMemberType(String groupId, String memberType) throws Exception
    {
        // Sort params
        Map<String, String> otherParams = new HashMap<>();
        otherParams.put("where", "(memberType=" + memberType + ")");

        // Paging
        Paging paging = getPaging(0, 4);

        ListResponse<GroupMember> resp = getGroupMembers(groupId, paging, otherParams);
        resp.getList().forEach(groupMember -> {
            validateGroupMemberDefaultFields(groupMember);
            assertEquals("memberType was expected to be " + memberType, memberType, groupMember.getMemberType());
        });
    }

    private void validateGroupMemberDefaultFields(GroupMember groupMember)
    {
        assertNotNull(groupMember);
        assertNotNull(groupMember.getId());
        assertNotNull(groupMember.getDisplayName());
        assertNotNull(groupMember.getMemberType());
    }

    @Test
    public void testGetGroup() throws Exception
    {
        final Groups groupsProxy = publicApiClient.groups();
        try
        {
            createAuthorityContext(user1);

            setRequestContext(user1);

            // Check invalid group id.
            {
                groupsProxy.getGroup("invalidGroupId", HttpServletResponse.SC_NOT_FOUND);
            }

            {
                Group group = groupsProxy.getGroup(groupA.getId());
                validateGroupDefaultFields(group);
            }

            {
                Map<String, String> otherParams = new HashMap<>();
                otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_PARENT_IDS);

                Group group = groupsProxy.getGroup(groupA.getId(), otherParams, HttpServletResponse.SC_OK);
                validateGroupDefaultFields(group, true);
                assertNotNull(group.getParentIds());
                assertNull(group.getZones());
            }

            {
                Map<String, String> otherParams = new HashMap<>();
                otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_ZONES);

                Group group = groupsProxy.getGroup(groupA.getId(), otherParams, HttpServletResponse.SC_OK);
                validateGroupDefaultFields(group, true);
                assertNull(group.getParentIds());
                assertNotNull(group.getZones());
            }
        }
        finally
        {
            clearAuthorityContext();
        }
    }

    @Test
    public void testCreateGroup() throws Exception
    {
        final Groups groupsProxy = publicApiClient.groups();

        // User without admin rights can't create a group.
        {
            setRequestContext(user1);

            Group group = generateGroup();
            groupsProxy.createGroup(group, null, HttpServletResponse.SC_FORBIDDEN);
        }

        // Invalid auth.
        {
            setRequestContext(networkOne.getId(), GUID.generate(), "password");
            groupsProxy.createGroup(generateGroup(), null, HttpServletResponse.SC_UNAUTHORIZED);
        }

        // Create group and subgroup.
        {
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

            Map<String, String> otherParams = new HashMap<>();
            otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_PARENT_IDS);

            Group group = generateGroup();

            Group createdGroup01 = groupsProxy.createGroup(group, null, HttpServletResponse.SC_CREATED);

            assertNotNull(createdGroup01);
            assertNotNull(createdGroup01.getId());
            assertTrue(createdGroup01.getIsRoot());
            assertNull(createdGroup01.getParentIds());

            Set<String> subGroup01Parents = new HashSet<>();
            subGroup01Parents.add(createdGroup01.getId());

            Group subGroup01 = generateGroup();
            subGroup01.setParentIds(subGroup01Parents);

            Group createdSubGroup01 = groupsProxy.createGroup(subGroup01, otherParams, HttpServletResponse.SC_CREATED);
            assertNotNull(createdSubGroup01);
            assertNotNull(createdSubGroup01.getId());
            assertFalse(createdSubGroup01.getIsRoot());
            assertNotNull(createdSubGroup01.getParentIds());
            assertEquals(subGroup01Parents, createdSubGroup01.getParentIds());
        }

        // Group id is missing.
        {
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

            Group group = new Group();
            groupsProxy.createGroup(group, null, HttpServletResponse.SC_BAD_REQUEST);
        }

        // Create group with an id that contains "/" should return an error.
        {
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

            Group group = new Group();
            group.setId("/test/");
            groupsProxy.createGroup(group, null, HttpServletResponse.SC_BAD_REQUEST);
        }

        // Id clashes with an existing group.
        {
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

            Group group = generateGroup();

            groupsProxy.createGroup(group, null, HttpServletResponse.SC_CREATED);
            groupsProxy.createGroup(group, null, HttpServletResponse.SC_CONFLICT);
        }

        // Create subgroup with invalid parent.
        {
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

            Map<String, String> otherParams = new HashMap<>();
            otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_PARENT_IDS);

            Set<String> subGroupParents = new HashSet<>();
            subGroupParents.add("invalidId");

            Group subGroup = generateGroup();
            subGroup.setParentIds(subGroupParents);

            groupsProxy.createGroup(subGroup, otherParams, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Test
    public void testCreateGroupMembers() throws PublicApiException
    {
        final Groups groupsProxy = publicApiClient.groups();

        try
        {
            createAuthorityContext(user1);

            // +ve tests
            // Create a group membership (for a existing person and a sub-group)
            // within a group groupId
            {
                // Add person as groupB member
                groupsProxy.createGroupMember(groupB.getId(), personMember, HttpServletResponse.SC_CREATED);
                // Add group as groupB sub-group
                groupsProxy.createGroupMember(groupB.getId(), groupMemberA, HttpServletResponse.SC_CREATED);
            }

            // If the added sub-group was previously a root group then it
            // becomes a non-root group since it now has a parent.
            {
                // create a group without parent
                Group groupRoot = generateGroup();
                Group groupRootCreated = groupsProxy.createGroup(groupRoot, null, HttpServletResponse.SC_CREATED);
                assertTrue("Group was expected to be root.", groupRootCreated.getIsRoot());
                GroupMember groupMember = new GroupMember();
                groupMember.setId(groupRootCreated.getId());
                groupMember.setMemberType(MEMBER_TYPE_GROUP);

                groupsProxy.createGroupMember(groupB.getId(), groupMember, HttpServletResponse.SC_CREATED);
                Group subGroup = groupsProxy.getGroup(groupMember.getId());
                assertFalse("Group was expected to be sub-group.", subGroup.getIsRoot());
            }

            // -ve tests
            // Id clashes with an existing group member
            {
                //Add a group member that has been already added
                groupsProxy.createGroupMember(groupB.getId(), groupMemberA, HttpServletResponse.SC_CONFLICT);
            }

            // Person or group with given id does not exists
            {
                GroupMember invalidIdGroupMember = new GroupMember();
                invalidIdGroupMember.setId("invalidPersonId-" + GUID.generate());
                invalidIdGroupMember.setMemberType(MEMBER_TYPE_PERSON);
                groupsProxy.createGroupMember(groupA.getId(), invalidIdGroupMember, HttpServletResponse.SC_NOT_FOUND);
                invalidIdGroupMember.setMemberType(MEMBER_TYPE_GROUP);
                groupsProxy.createGroupMember(groupA.getId(), invalidIdGroupMember, HttpServletResponse.SC_NOT_FOUND);
            }

            // Invalid group Id
            {
                groupsProxy.createGroupMember("invalidGroupId", groupMemberA, HttpServletResponse.SC_NOT_FOUND);
            }

            // Invalid group member
            {
                GroupMember invalidGroupMember = new GroupMember();
                groupsProxy.createGroupMember(groupA.getId(), invalidGroupMember, HttpServletResponse.SC_BAD_REQUEST);

                // Member type still missing
                invalidGroupMember.setId("Test_" + GUID.generate());
                groupsProxy.createGroupMember(groupA.getId(), invalidGroupMember, HttpServletResponse.SC_BAD_REQUEST);
                // invalid member type
                invalidGroupMember.setMemberType("invalidMemberType");
                groupsProxy.createGroupMember(groupA.getId(), invalidGroupMember, HttpServletResponse.SC_BAD_REQUEST);
            }

            // Validation tests
            {
                // Add group as groupB sub-group with member id null
                personMember.setId(null);
                groupsProxy.createGroupMember(groupB.getId(), personMember, HttpServletResponse.SC_BAD_REQUEST);
                // Add group as groupB sub-group with member display name null
                personMember.setDisplayName(null);
                groupsProxy.createGroupMember(groupB.getId(), personMember, HttpServletResponse.SC_BAD_REQUEST);
                // Add group as groupB sub-group with member type null
                personMember.setMemberType(null);
                groupsProxy.createGroupMember(groupB.getId(), personMember, HttpServletResponse.SC_BAD_REQUEST);
            }

            // Add group member with a different type from the existing one
            {
                // Add person as groupB member with member type GROUP
                personMember.setMemberType(MEMBER_TYPE_GROUP);
                groupsProxy.createGroupMember(groupB.getId(), personMember, HttpServletResponse.SC_BAD_REQUEST);
                // Add group as groupB sub-group with member type PERSON
                groupMemberA.setMemberType(MEMBER_TYPE_PERSON);
                groupsProxy.createGroupMember(groupB.getId(), groupMemberA, HttpServletResponse.SC_BAD_REQUEST);
            }

            // User does not have admin permission to create a group membership
            {
                setRequestContext(user1);
                groupsProxy.createGroupMember(groupB.getId(), groupMemberB, HttpServletResponse.SC_FORBIDDEN);
            }
            //Authentication failed
            {
                setRequestContext(networkOne.getId(), GUID.generate(), "password");
                groupsProxy.createGroupMember(groupB.getId(), groupMemberB, HttpServletResponse.SC_UNAUTHORIZED);
            }

        }
        finally
        {
            clearAuthorityContext();
        }
    }

    @Test
    public void testUpdateGroup() throws Exception
    {
        final Groups groupsProxy = publicApiClient.groups();

        Map<String, String> otherParams = new HashMap<>();
        otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_PARENT_IDS);

        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

        Group group = groupsProxy.createGroup(generateGroup(), null, HttpServletResponse.SC_CREATED);

        Set<String> subGroupParents = new HashSet<>();
        subGroupParents.add(group.getId());

        Group generatedSubGroup = generateGroup();
        generatedSubGroup.setParentIds(subGroupParents);

        Group subGroup = groupsProxy.createGroup(generatedSubGroup, otherParams, HttpServletResponse.SC_CREATED);

        // User without admin rights can't update a group.
        {
            setRequestContext(user1);
            groupsProxy.updateGroup(group.getId(), new Group(), null, HttpServletResponse.SC_FORBIDDEN);
        }

        // Invalid auth.
        {
            setRequestContext(networkOne.getId(), GUID.generate(), "password");
            groupsProxy.updateGroup(group.getId(), new Group(), null, HttpServletResponse.SC_UNAUTHORIZED);
        }

        // Update group and subgroup.
        {
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);


            String displayName = "newDisplayName";

            Group mySubGroup = new Group();
            mySubGroup.setDisplayName(displayName);

            Group updateGroup = groupsProxy.updateGroup(subGroup.getId(), mySubGroup, otherParams, HttpServletResponse.SC_OK);

            // Validate default response and additional information (parentIds).
            assertNotNull(updateGroup);
            assertNotNull(updateGroup.getId());
            assertFalse(updateGroup.getIsRoot());
            assertNotNull(updateGroup.getParentIds());

            // Check that only display name changed.
            assertEquals(displayName, updateGroup.getDisplayName());

            // Check that nothing else changed.
            assertEquals(subGroup.getId(), updateGroup.getId());
            assertEquals(subGroup.getIsRoot(), updateGroup.getIsRoot());
            assertEquals(subGroup.getParentIds(), updateGroup.getParentIds());
        }

        // Group id doesn't exist.
        {
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

            groupsProxy.updateGroup("invalidId", group, null, HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Test
    public void testDeleteGroup() throws Exception
    {
        final Groups groupsProxy = publicApiClient.groups();

        Map<String, String> otherParams = new HashMap<>();
        otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_PARENT_IDS);

        setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

        Group group = groupsProxy.createGroup(generateGroup(), otherParams, HttpServletResponse.SC_CREATED);

        // User without admin rights can't delete a group.
        {
            setRequestContext(user1);

            groupsProxy.deleteGroup(group.getId(), false, HttpServletResponse.SC_FORBIDDEN);
        }

        // Invalid auth.
        {
            setRequestContext(networkOne.getId(), GUID.generate(), "password");
            groupsProxy.deleteGroup("invalidId", false, HttpServletResponse.SC_UNAUTHORIZED);
        }

        // Group id doesn't exist.
        {
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

            groupsProxy.deleteGroup("GROUP_invalidId", false, HttpServletResponse.SC_NOT_FOUND);
        }

        // Trying to modify a fixed authority.
        {
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

            groupsProxy.deleteGroup(GROUP_EVERYONE, false, HttpServletResponse.SC_CONFLICT);
        }

        // Trying to delete a person.
        {
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

            groupsProxy.deleteGroup(user1, false, HttpServletResponse.SC_BAD_REQUEST);
        }

        {
            Group groupLevel1 = groupsProxy.createGroup(generateGroup(), otherParams, HttpServletResponse.SC_CREATED);
            Group groupLevel2 = groupsProxy.createGroup(generateSubGroup(groupLevel1), otherParams, HttpServletResponse.SC_CREATED);
            Group groupLevel3 = groupsProxy.createGroup(generateSubGroup(groupLevel2), otherParams, HttpServletResponse.SC_CREATED);

            // Delete the primary root (no cascade)
            groupsProxy.deleteGroup(groupLevel1.getId(), false, HttpServletResponse.SC_NO_CONTENT);
            groupsProxy.getGroup(groupLevel1.getId(), HttpServletResponse.SC_NOT_FOUND);

            // Check that second level group is now root.
            groupLevel2 = groupsProxy.getGroup(groupLevel2.getId(), HttpServletResponse.SC_OK);
            assertTrue(groupLevel2.getIsRoot());

            // Check that third level group wasn't deleted.
            groupsProxy.getGroup(groupLevel3.getId(), HttpServletResponse.SC_OK);

            // Delete new root with cascade.
            groupsProxy.deleteGroup(groupLevel2.getId(), true, HttpServletResponse.SC_NO_CONTENT);

            // Check that delete with cascade worked.
            groupsProxy.getGroup(groupLevel2.getId(), HttpServletResponse.SC_NOT_FOUND);
            groupsProxy.getGroup(groupLevel3.getId(), HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Test
    public void testDeleteGroupMembership() throws Exception
    {
        final Groups groupsProxy = publicApiClient.groups();

        try
        {
            createAuthorityContext(user1);

            {
                Map<String, String> otherParams = new HashMap<>();
                otherParams.put("include", org.alfresco.rest.api.Groups.PARAM_INCLUDE_PARENT_IDS);
                Group createdTestGroup = groupsProxy.createGroup(generateGroup(), null, HttpServletResponse.SC_CREATED);
                // Add new created group to groupA
                GroupMember groupMember = new GroupMember();
                groupMember.setId(createdTestGroup.getId());
                groupMember.setMemberType(MEMBER_TYPE_GROUP);
                groupsProxy.createGroupMember(groupA.getId(), groupMember, HttpServletResponse.SC_CREATED);

                // If a removed sub-group no longer has any parent groups then
                // it becomes a root group.
                assertFalse(groupsProxy.getGroup(groupMember.getId(), otherParams, HttpServletResponse.SC_OK).getParentIds().isEmpty());
                groupsProxy.deleteGroupMembership(groupA.getId(), groupMember.getId(), HttpServletResponse.SC_NO_CONTENT);
                assertTrue(groupsProxy.getGroup(groupMember.getId(), otherParams, HttpServletResponse.SC_OK).getParentIds().isEmpty());
            }

            {
                // Add new a person as a member of groupA
                groupsProxy.createGroupMember(groupA.getId(), personMember, HttpServletResponse.SC_CREATED);
                ListResponse<Group> groups = groupsProxy.getGroupsByPersonId(personMember.getId(), null, "Cannot retrieve user groups", 200);
                assertEquals(3L, (long) groups.getPaging().getTotalItems());
                Iterator<Group> it = groups.getList().iterator();
                assertEquals(GROUP_EVERYONE, it.next().getId());
                assertEquals(rootGroupName, it.next().getId());
                assertEquals(groupA, it.next());
                groupsProxy.deleteGroupMembership(groupA.getId(), personMember.getId(), HttpServletResponse.SC_NO_CONTENT);
                groups = groupsProxy.getGroupsByPersonId(personMember.getId(), null, "Cannot retrieve user groups", 200);
                assertEquals(1L, (long) groups.getPaging().getTotalItems());
                it = groups.getList().iterator();
                assertEquals(GROUP_EVERYONE, it.next().getId());
            }

            // -ve tests
            // Group id or group member id do not exist.
            {
                groupsProxy.deleteGroupMembership("invalidGroupId", groupMemberA.getId(), HttpServletResponse.SC_NOT_FOUND);
                groupsProxy.deleteGroupMembership(groupA.getId(), "invalidGroupMemberId", HttpServletResponse.SC_NOT_FOUND);
            }

            // Authentication failed
            {
                setRequestContext(networkOne.getId(), GUID.generate(), "password");
                groupsProxy.deleteGroupMembership(groupA.getId(), groupMemberA.getId(), HttpServletResponse.SC_UNAUTHORIZED);
            }

            // User does not have permission to delete a group membership
            {
                setRequestContext(user1);
                groupsProxy.deleteGroupMembership(groupA.getId(), groupMemberA.getId(), HttpServletResponse.SC_FORBIDDEN);
            }
        }
        finally
        {
            clearAuthorityContext();
        }
    }

    private Group generateGroup()
    {
        Group group = new Group();
        group.setId("TST" + GUID.generate());

        return group;
    }

    private Group generateSubGroup(Group parentGroup)
    {

        Set<String> subGroupParents = new HashSet<>();
        if (parentGroup.getParentIds() != null && !parentGroup.getParentIds().isEmpty())
        {
            subGroupParents.addAll(parentGroup.getParentIds());
        }
        subGroupParents.add(parentGroup.getId());

        Group subGroup = generateGroup();
        subGroup.setParentIds(subGroupParents);

        return subGroup;
    }
}
