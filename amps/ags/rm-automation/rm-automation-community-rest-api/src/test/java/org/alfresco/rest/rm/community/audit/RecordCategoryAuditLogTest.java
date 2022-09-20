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

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.records.CreateElectronicRecordsTests;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.v0.RMAuditAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.test.AlfrescoTest;
import org.apache.neethi.All;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.*;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.CREATED;

public class RecordCategoryAuditLogTest extends BaseRMRestTest {


    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;

    @Autowired
    private RMAuditAPI rmAuditAPI;

    private RecordCategory rootCategory;
    private String All;
    private final String TEST_PREFIX = generateTestPrefix(RecordCategoryAuditLogTest.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";

    @BeforeClass(alwaysRun = true)
    public void recordCategoryAuditLogSetup() {
        STEP("Create RM Site");
        createRMSiteIfNotExists();

        STEP("Create RM Admin user");
        rmRolesAndActionsAPI.createUserAndAssignToRole(getAdminUser().getUsername(), getAdminUser().getPassword(), RM_ADMIN,
            getAdminUser().getPassword(),
            "Administrator");


    }

    @Test
    @AlfrescoTest(jira = "RM-2768")
    public void recordCategoryAudit() throws Exception {

        STEP("Create root level category");
        rootCategory = createRootCategory(getRandomName("Category"));

        STEP("newly created record category contains 3 events: object creation, inherited permissions set to false and metadata update");
        rmAuditAPI.getRMAuditLog(dataUser.getAdminUser().getUsername(), dataUser.getAdminUser().getPassword(),3, All);


    }


}
