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
import org.alfresco.util.GUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.*;

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
    private GroupMember groupMemberA = null;
    private GroupMember groupMemberB = null;

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

            groupMemberA = new GroupMember();
            groupMemberA.setId(groupAAuthorityName);

            groupMemberB = new GroupMember();
            groupMemberB.setId(groupBAuthorityName);
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
            assertEquals("GROUP_EVERYONE", it.next().getId());
        }

        // Add the user to a couple more groups and list them.
        {
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            authorityService.addAuthority(groupA.getId(), personAlice.getId());
            authorityService.addAuthority(groupB.getId(), personAlice.getId());

            ListResponse<Group> groups = groupsProxy.getGroupsByPersonId(personAlice.getId(), null, "Couldn't get user's groups", 200);
            assertEquals(4L, (long) groups.getPaging().getTotalItems());
            Iterator<Group> it = groups.getList().iterator();
            assertEquals("GROUP_EVERYONE", it.next().getId());
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
            assertEquals("GROUP_EVERYONE", it.next().getId());
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
            assertEquals("GROUP_EVERYONE", it.next().getId());
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

        // Id clashes with an existing group.
        {
            setRequestContext(networkOne.getId(), networkAdmin, DEFAULT_ADMIN_PWD);

            Group group = generateGroup();

            groupsProxy.createGroup(group, null, HttpServletResponse.SC_CREATED);
            groupsProxy.createGroup(group, null, HttpServletResponse.SC_CONFLICT);
        }
    }

    private Group generateGroup()
    {
        Group group = new Group();
        group.setId("TST" + GUID.generate());

        return group;
    }
}
