/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import static org.springframework.http.HttpStatus.CREATED;
import static org.testng.Assert.assertNotNull;

import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.core.v0.BaseAPI.RMProperty;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.test.AlfrescoTest;

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

    /**
     * data prep services
     */
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;

    @Autowired
    private RecordsAPI recordsAPI;

    private final String TEST_PREFIX = generateTestPrefix(CreateNonElectronicRecordsTests.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    private final String recordName = "RM-2777 record";
    private final String recordTitle = recordName + " title";
    private final String recordDescription = recordName + " description";

    @BeforeClass(alwaysRun = true)
    public void preConditions()
    {
        STEP("Create RM Site");
        createRMSiteIfNotExists();

        STEP("Create RM Admin user");
        rmRolesAndActionsAPI.createUserAndAssignToRole(getAdminUser().getUsername(), getAdminUser().getPassword(), RM_ADMIN,
                getAdminUser().getPassword(),
                "Administrator");

        STEP("Create root level category");
        rootCategory = createRootCategory(getRandomName("Category"));

        STEP("Create the record folder inside the rootCategory");
        recordFolder = createRecordFolder(rootCategory.getId(), getRandomName("Folder"));

    }

    /**
     * Test v0 methods to create and get non-electronic records.
     */
    @Test
    @AlfrescoTest(jira = "RM-2777")
    public void createNonElectronicRecordTest()
    {
        STEP("Create a non-electronic record by completing some of the fields");
        Map<Enum<?>, String> properties = new HashMap<Enum<?>, String>();
        properties.put(RMProperty.TITLE, recordTitle);
        properties.put(RMProperty.DESCRIPTION, recordDescription);
        properties.put(RMProperty.NAME, recordName);
        properties.put(RMProperty.PHYSICAL_SIZE, "");
        properties.put(RMProperty.NUMBER_OF_COPIES, "");
        properties.put(RMProperty.SHELF, "");
        properties.put(RMProperty.STORAGE_LOCATION, "");
        properties.put(RMProperty.BOX, "");
        properties.put(RMProperty.FILE, "");

        recordsAPI.createNonElectronicRecord(getAdminUser().getUsername(),
                getAdminUser().getPassword(), properties, rootCategory.getName(), recordFolder.getName());

        STEP("Check the non-electronic record has been created");
        assertStatusCode(CREATED);
        assertNotNull(recordsAPI.getRecord(getAdminUser().getUsername(), getAdminUser().getPassword(),
                recordFolder.getName(), recordName));

    }

    @AfterClass(alwaysRun = true)
    public void deletePreConditions()
    {
        STEP("Delete the created rootCategory along with corresponding record folders/records present in it");
        getRestAPIFactory().getRecordCategoryAPI().deleteRecordCategory(rootCategory.getId());
    }

}
