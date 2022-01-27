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

import static org.alfresco.rest.rm.community.model.audit.AuditEvents.CREATE_PERSON;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;

import java.util.Collections;

import com.google.common.collect.ImmutableMap;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.v0.service.RMAuditService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
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
    private RMAuditService rmAuditService;

    /**
     * Given I have created a new user
     * When I view the RM audit
     * Then there is an entry showing that I created a user
     */
    @Test
    @AlfrescoTest(jira = "RM-6223")
    public void createUserEventIsAudited()
    {
        rmAuditService.clearAuditLog();
        STEP("Create a new user.");
        String userName = "auditCreateUser" + PREFIX;
        createUser = getDataUser().createUser(userName);

        STEP("Check the audit log contains the entry for the created user event.");
        rmAuditService.checkAuditLogForEvent(getAdminUser(), CREATE_PERSON, getAdminUser(), userName,
                Collections.singletonList(ImmutableMap.of("new", userName, "previous", "", "name", "User Name")));
    }

    @AfterClass (alwaysRun = true)
    public void cleanUp()
    {
        //delete the created user
        getDataUser().deleteUser(createUser);
    }
}
