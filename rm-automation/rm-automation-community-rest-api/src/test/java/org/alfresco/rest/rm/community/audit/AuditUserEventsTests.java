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

import static org.alfresco.rest.rm.community.model.audit.AuditEvents.CREATE_PERSON;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.audit.AuditEntry;
import org.alfresco.rest.v0.RMAuditAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains the tests that check the user events are audited
 *
 * @author Rodica Sutu
 * @since 2.7
 */
public class AuditUserEventsTests extends BaseRMRestTest
{
    private final String PREFIX = generateTestPrefix(AuditUserEventsTests.class);

    private UserModel createUser;
    @Autowired
    private RMAuditAPI rmAuditAPI;

    /**
     * Given I have created a new user
     * When I view the RM audit
     * Then there is an entry showing that I created a user
     *
     * @throws Exception
     */
    @Test
    @AlfrescoTest(jira = "RM-6223")
    public void createUserEventIsAudited() throws Exception
    {
        String userName = "auditCreateUser" + PREFIX;
        
        createUser = getDataUser().createUser(userName);
        List<AuditEntry> auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getPassword(),
                getAdminUser().getPassword(), 100, CREATE_PERSON.event);

        assertTrue("The list of events is not filtered by " + CREATE_PERSON.event,
                auditEntries.stream().allMatch(auditEntry -> auditEntry.getEvent().equals(CREATE_PERSON.eventDisplayName)));

        assertTrue("The username value for the user created is not audited.",
                auditEntries.stream().filter(auditEntry -> auditEntry.getEvent().equals(CREATE_PERSON.eventDisplayName))
                            .allMatch(auditEntry -> auditEntry.getNodeName().equals(userName)));
    }

    @BeforeClass (alwaysRun = true)
    public void cleanAuditLogs()
    {
        //clean audit logs
        rmAuditAPI.clearAuditLog(getAdminUser().getPassword(), getAdminUser().getPassword());
    }

    @AfterClass (alwaysRun = true)
    public void cleanUp()
    {
        //delete the created user
        getDataUser().deleteUser(createUser);
    }
}
