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

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.getFile;
import static org.alfresco.rest.rm.community.utils.RMSiteUtil.createDOD5015RMSiteModel;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.rest.core.v0.BaseAPI.RMProperty;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.fileplan.FilePlan;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.v0.ExportAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.test.AlfrescoTest;
import org.apache.http.HttpResponse;
import org.checkerframework.checker.units.qual.K;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Contains CreateNonElectronicRecords test which checks creation and basic actions(view details, edit, move, copy, delete) on non-electronic records
 * <p/>
 * Precondition:
 * <p/>
 * RM site created, contains category 1 with folder 1 and folder 2 inside it
 * <p/>
 * RM user has RM admin role
 *
 * @author Shubham Jain
 * @Since 7.2.0 M2
 */
public class CreateNonElectronicRecordsTests extends BaseRMRestTest
{
    private RecordCategory rootCategory;

    private RecordCategoryChild recordFolder;

    /** data prep services */
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;

    @Autowired
    private RecordsAPI recordsAPI;

    private final String TEST_PREFIX = generateTestPrefix(CreateNonElectronicRecordsTests.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    String category1 = TEST_PREFIX + "RM-2777 category1";
    String folder1 = TEST_PREFIX + "RM-2777 folder1";
    String folder2 = TEST_PREFIX + "RM-2777 folder2";
    String recordName = "RM-2777 record";
    String recordTitle = recordName + " title";
    String recordDescription = recordName + " description";
    String editedRecord = "edited RM-2777 record";
    String editedRecordTitle = "edited RM-2777 record title";
    String editedRecordDescription = "edited RM-2777 record description";



    @BeforeClass (alwaysRun = true)
    public void CreateNonElectronicRecordsTestsBeforeClass()
    {
        createRMSiteIfNotExists();

        // create "rm admin" user
        rmRolesAndActionsAPI.createUserAndAssignToRole(getAdminUser().getUsername(), getAdminUser().getPassword(), RM_ADMIN,
                getAdminUser().getPassword(),
                "Administrator");

        STEP("Create root level category");
        rootCategory = createRootCategory(getRandomName("Category"));

        STEP("Create the record folder inside the rootCategory");
        recordFolder = createRecordFolder(rootCategory.getId(), getRandomName("Folder"));

    }

    @Test
    @AlfrescoTest(jira = "RM-2777")
    public void createNonElectronicRecord()
    {
        // create a non-electronic record by completing some of the fields
        HashMap<Enum<?>,String> properties = new HashMap<Enum<?>,String>();
        properties.put(RMProperty.TITLE,recordTitle);
        properties.put(RMProperty.DESCRIPTION,recordDescription);
        properties.put(RMProperty.NAME,recordName);
        properties.put(RMProperty.PHYSICAL_SIZE,"");
        properties.put(RMProperty.NUMBER_OF_COPIES,"");
        properties.put(RMProperty.SHELF,"");
        properties.put(RMProperty.STORAGE_LOCATION,"");
        properties.put(RMProperty.BOX,"");
        properties.put(RMProperty.FILE,"");

        HttpResponse response= recordsAPI.createNonElectronicRecord(getAdminUser().getUsername(),
                getAdminUser().getPassword(), properties, rootCategory.getName(),recordFolder.getName());

        // check the non-electronic record has been created
        assertStatusCode(CREATED);
        assertNotNull(recordsAPI.getRecord(getAdminUser().getUsername(),getAdminUser().getPassword(),
                recordFolder.getName(),recordName));
        recordsAPI.GetRecordActions(getAdminUser().getUsername(),getAdminUser().getPassword(),recordName);

    }

}
