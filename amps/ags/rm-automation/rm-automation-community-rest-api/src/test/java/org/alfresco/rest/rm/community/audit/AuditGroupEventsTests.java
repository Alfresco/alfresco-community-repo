/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rm.community.audit;

import static java.util.Arrays.asList;

import static org.alfresco.rest.rm.community.model.audit.AuditEvents.ADD_TO_USER_GROUP;
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.CREATE_USER_GROUP;
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.DELETE_USER_GROUP;
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.REMOVE_FROM_USER_GROUP;
import static org.alfresco.utility.report.log.Step.STEP;

import java.util.Collections;

import com.google.common.collect.ImmutableMap;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.v0.service.RMAuditService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.model.GroupModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains the tests that check the group events are audited
 *
 * @author Claudia Agache
 * @since 2.7
 */
@AlfrescoTest (jira = "RM-5236")
public class AuditGroupEventsTests extends BaseRMRestTest
{
    @Autowired
    private RMAuditService rmAuditService;
    private GroupModel testGroup;
    private UserModel testUser;

    @BeforeClass (alwaysRun = true)
    public void cleanAuditLogs()
    {
        rmAuditService.clearAuditLog();
    }

    /**
     * Given I have created a new group
     * When I view the RM audit
     * Then there is an entry showing that I created a group
     */
    @Test
    public void createGroupEventIsAudited()
    {
        testGroup = dataGroup.createRandomGroup();

        STEP("Check the audit log contains the entry for the created group.");
        rmAuditService.checkAuditLogForEvent(getAdminUser(), CREATE_USER_GROUP, getAdminUser(), testGroup.getGroupIdentifier(),
                Collections.singletonList(ImmutableMap.of("new", testGroup.getGroupIdentifier(), "previous", "",
                        "name", "authorityDisplayName")));
    }

    /**
     * Given I have added a user to a group
     * When I view the RM audit
     * Then there is an entry showing that I have added a user to a group
     */
    @Test
    public void addUserToGroupEventIsAudited()
    {
        testGroup = dataGroup.createRandomGroup();
        testUser = getDataUser().createRandomTestUser();
        dataGroup.usingUser(testUser).addUserToGroup(testGroup);

        STEP("Check the audit log contains the entry for the add user to group event.");
        rmAuditService.checkAuditLogForEvent(getAdminUser(), ADD_TO_USER_GROUP, getAdminUser(), testGroup.getGroupIdentifier(),
                asList(ImmutableMap.of("new", testUser.getUsername(), "previous", "", "name", "User Name"),
                        ImmutableMap.of("new", testGroup.getGroupIdentifier(), "previous", "", "name", "Parent Group")));
    }

    /**
     * Given I have removed a user from a group
     * When I view the RM audit
     * Then there is an entry showing that I have removed a user from a group
     */
    @Test
    public void removeUserFromGroupEventIsAudited()
    {
        testGroup = dataGroup.createRandomGroup();
        testUser = getDataUser().createRandomTestUser();
        dataGroup.usingUser(testUser).addUserToGroup(testGroup);
        dataGroup.removeUserFromGroup(testGroup, testUser);

        STEP("Check the audit log contains the entry for the remove user from group event.");
        rmAuditService.checkAuditLogForEvent(getAdminUser(), REMOVE_FROM_USER_GROUP, getAdminUser(), testGroup.getGroupIdentifier(),
                asList(ImmutableMap.of("new", "", "previous", testUser.getUsername(), "name", "User Name"),
                        ImmutableMap.of("new", "","previous", testGroup.getGroupIdentifier(), "name", "Parent Group")));
    }

    /**
     * Given I have deleted a group
     * When I view the RM audit
     * Then there is an entry showing that I have deleted a group
     */
    @Test
    public void deleteGroupEventIsAudited()
    {
        testGroup = dataGroup.createRandomGroup();
        dataGroup.deleteGroup(testGroup);

        STEP("Check the audit log contains the entry for the delete group event.");
        rmAuditService.checkAuditLogForEvent(getAdminUser(), DELETE_USER_GROUP, getAdminUser(), testGroup.getGroupIdentifier(),
                Collections.singletonList(ImmutableMap.of("new", "", "previous", testGroup.getGroupIdentifier(),
                        "name", "authorityDisplayName")));
    }
}
