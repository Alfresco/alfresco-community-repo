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

package org.alfresco.rest.rm.community.records;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.RMSiteUtil.FILE_PLAN_PATH;
import static org.alfresco.utility.Utility.buildPath;
import static org.alfresco.utility.Utility.removeLastSlash;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.util.Map;

import org.alfresco.rest.core.v0.BaseAPI.RMProperty;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.custom.CustomDefinitions;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.CustomDefinitionsAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.test.AlfrescoTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.*;
import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;

import org.alfresco.rest.v0.HoldsAPI;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.alfresco.rest.rm.community.base.TestData.HOLD_DESCRIPTION;
import static org.alfresco.rest.rm.community.base.TestData.HOLD_REASON;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.AssertJUnit.assertFalse;

public class AddRelationshipToHoldRecord extends BaseRMRestTest
{
    private static final String HOLD = "HOLD" + generateTestPrefix(AddRelationshipToHoldRecord.class);
    private static final String Relationship = "Relationship" + generateTestPrefix(AddRelationshipToHoldRecord.class);
    private final String PREFIX = generateTestPrefix(AddRelationshipToHoldRecord.class);
    private SiteModel testSite;
    private String holdNodeRef;
    private FileModel documentHeld, contentToAddToHold, contentAddToHoldNoPermission;
    private RecordCategory recordCategory;
    private RecordCategoryChild recordFolder;
    private static File updatedFile;


    @Autowired
    private HoldsAPI holdsAPI;
    @Autowired
    private org.alfresco.rest.v0.RecordsAPI recordsAPI;
    @Autowired
    private CustomDefinitionsAPI customDefinitionsAPI;

    @Test
    public void AddRelationshipToHoldRecord()
    {
        STEP("Create record category, record folder and records.");
        recordCategory = createRootCategory(getRandomName("recordCategory"));
        recordFolder = createRecordFolder(recordCategory.getId(), PREFIX + "recFolder");
        Record RECORD1 = createElectronicRecord(recordFolder.getId(), PREFIX + "recordone");
        Record RECORD2 = createElectronicRecord(recordFolder.getId(), PREFIX + "recordtwo");



        STEP("Create a hold.");
        holdNodeRef = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(), getAdminUser().getUsername(),
            HOLD, HOLD_REASON, HOLD_DESCRIPTION);


        STEP("Add RECORD2 to hold");
        holdsAPI.addItemToHold(getAdminUser().getUsername(), getAdminUser().getPassword(), RECORD2.getId(), HOLD);


        STEP("navigate to RECORD1 and create Relationship");
        customDefinitionsAPI.createRelationship(getAdminUser().getUsername(),
            getAdminUser().getPassword(), RECORD1.getId(), RECORD2.getId(), CustomDefinitions.ATTACHMENT);


            }

}
