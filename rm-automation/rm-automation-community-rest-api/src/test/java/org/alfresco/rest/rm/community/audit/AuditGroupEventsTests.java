/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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

import static org.alfresco.rest.rm.community.model.audit.AuditEvents.ADD_TO_USER_GROUP;
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.CREATE_USER_GROUP;
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.DELETE_USER_GROUP;
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.REMOVE_FROM_USER_GROUP;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.audit.AuditEntry;
import org.alfresco.rest.v0.RMAuditAPI;
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
    private RMAuditAPI rmAuditAPI;

    private GroupModel testGroup;
    private UserModel testUser;

    @BeforeClass (alwaysRun = true)
    public void cleanAuditLogs()
    {
        //clean audit logs
        rmAuditAPI.clearAuditLog(getAdminUser().getPassword(), getAdminUser().getPassword());
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

        STEP("Get the list of audit entries for the create group event.");
        List<AuditEntry> auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getPassword(),
                getAdminUser().getPassword(), 100, CREATE_USER_GROUP.event);

        STEP("Check the audit log contains only the entries for the created group.");
        assertTrue("The list of events is not filtered by " + CREATE_USER_GROUP.event,
                auditEntries.stream().allMatch(auditEntry -> auditEntry.getEvent().equals(CREATE_USER_GROUP.eventDisplayName)));

        assertTrue("The group name for the new group created is not audited.",
                auditEntries.stream().filter(auditEntry -> auditEntry.getEvent().equals(CREATE_USER_GROUP.eventDisplayName))
                            .anyMatch(auditEntry -> auditEntry.getNodeName().equals(testGroup.getGroupIdentifier())));
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

        STEP("Get the list of audit entries for the add user to group event.");
        List<AuditEntry> auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getPassword(),
                getAdminUser().getPassword(), 100, ADD_TO_USER_GROUP.event);

        STEP("Check the audit log contains only the entries for the add user to group event.");
        assertTrue("The list of events is not filtered by " + ADD_TO_USER_GROUP.event,
                auditEntries.stream().allMatch(auditEntry -> auditEntry.getEvent().equals(ADD_TO_USER_GROUP.eventDisplayName)));

        assertTrue("The username and destination group are not audited.",
                auditEntries.stream().filter(auditEntry -> auditEntry.getEvent().equals(ADD_TO_USER_GROUP.eventDisplayName))
                            .anyMatch(auditEntry -> auditEntry.getNodeName().equals(testGroup.getGroupIdentifier())
                                    && auditEntry.getChangedValues().contains(ImmutableMap.of("new", testUser.getUsername(), "previous", "", "name", "User Name"))
                                    && auditEntry.getChangedValues().contains(ImmutableMap.of("new", testGroup.getGroupIdentifier(), "previous", "", "name", "Parent Group"))));
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

        STEP("Get the list of audit entries for the add user to group event.");
        List<AuditEntry> auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getPassword(),
                getAdminUser().getPassword(), 100, REMOVE_FROM_USER_GROUP.event);

        STEP("Check the audit log contains only the entries for the remove user from group event.");
        assertTrue("The list of events is not filtered by " + REMOVE_FROM_USER_GROUP.event,
                auditEntries.stream().allMatch(auditEntry -> auditEntry.getEvent().equals(REMOVE_FROM_USER_GROUP.eventDisplayName)));

        assertTrue("The username and previous parent group are not audited.",
                auditEntries.stream().filter(auditEntry -> auditEntry.getEvent().equals(REMOVE_FROM_USER_GROUP.eventDisplayName))
                            .anyMatch(auditEntry -> auditEntry.getNodeName().equals(testGroup.getGroupIdentifier())
                                    && auditEntry.getChangedValues().contains(ImmutableMap.of("new", "", "previous", testUser.getUsername(), "name", "User Name"))
                                    && auditEntry.getChangedValues().contains(ImmutableMap.of("new", "","previous", testGroup.getGroupIdentifier(), "name", "Parent Group"))));
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

        STEP("Get the list of audit entries for the delete group event.");
        List<AuditEntry> auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getPassword(),
                getAdminUser().getPassword(), 100, DELETE_USER_GROUP.event);

        STEP("Check the audit log contains only the entries for the created group.");
        assertTrue("The list of events is not filtered by " + DELETE_USER_GROUP.event,
                auditEntries.stream().allMatch(auditEntry -> auditEntry.getEvent().equals(DELETE_USER_GROUP.eventDisplayName)));

        assertTrue("The group name for the deleted group is not audited.",
                auditEntries.stream().filter(auditEntry -> auditEntry.getEvent().equals(DELETE_USER_GROUP.eventDisplayName))
                            .anyMatch(auditEntry -> auditEntry.getNodeName().equals(testGroup.getGroupIdentifier())
                                    && auditEntry.getChangedValues().contains(ImmutableMap.of("new", "", "previous", testGroup.getGroupIdentifier(), "name", "authorityDisplayName"))));
    }
}
