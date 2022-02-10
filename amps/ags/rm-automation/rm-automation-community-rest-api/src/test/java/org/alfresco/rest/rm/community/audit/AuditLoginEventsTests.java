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

import static org.alfresco.rest.rm.community.model.audit.AuditEvents.LOGIN_SUCCESSFUL;
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.LOGIN_UNSUCCESSFUL;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.audit.AuditEntry;
import org.alfresco.rest.v0.service.RMAuditService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * This class contains the tests that check the login events are audited
 *
 * @author Claudia Agache
 * @since 2.7
 */
@AlfrescoTest (jira = "RM-5234")
public class AuditLoginEventsTests extends BaseRMRestTest
{
    @Autowired
    private RMAuditService rmAuditService;

    /**
     * Given I have tried to login using invalid credentials
     * When I view the RM audit filtered by Login unsuccessful event
     * Then the audit log contains only the entries for the Login unsuccessful event
     */
    @Test
    public void filterByLoginUnsuccessful() throws Exception
    {
        rmAuditService.clearAuditLog();
        restClient.authenticateUser(new UserModel(getAdminUser().getUsername(), "InvalidPassword"));
        restClient.withCoreAPI().getSites();

        STEP("Get the list of audit entries for the login unsuccessful event.");
        List<AuditEntry> auditEntries = rmAuditService.getAuditEntriesFilteredByEvent(getAdminUser(),
                LOGIN_UNSUCCESSFUL);

        STEP("Check the audit log contains only the entries for the login unsuccessful event.");
        assertTrue("The list of events is not filtered by " + LOGIN_UNSUCCESSFUL.event,
                auditEntries.stream().allMatch(auditEntry -> auditEntry.getEvent().equals(LOGIN_UNSUCCESSFUL.eventDisplayName)));
    }
    
    /**
     * Given I have tried to login using valid credentials
     * When I view the RM audit filtered by Login successful event
     * Then the audit log contains only the entries for the Login successful event
     */
    @Test
    public void filterByLoginSuccessful() throws Exception
    {
        restClient.authenticateUser(getAdminUser());
        restClient.withCoreAPI().getSites();

        STEP("Get the list of audit entries for the login successful event.");
        List<AuditEntry> auditEntries = rmAuditService.getAuditEntriesFilteredByEvent(getAdminUser(),
                LOGIN_SUCCESSFUL);

        STEP("Check the audit log contains only the entries for the login successful event.");
        assertTrue("The list of events is not filtered by " + LOGIN_SUCCESSFUL.event,
                auditEntries.stream().allMatch(auditEntry -> auditEntry.getEvent().equals(LOGIN_SUCCESSFUL.eventDisplayName)));
    }
}
