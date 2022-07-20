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

package org.alfresco.rest.rm.community.smoke;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.RMAuditAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordFoldersAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.rest.v0.service.DispositionScheduleService;
import org.alfresco.test.AlfrescoTest;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.alfresco.rest.rm.community.base.TestData.DEFAULT_PASSWORD;
import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.CREATED_DATE;
import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.CUT_OFF_DATE;
import static org.alfresco.rest.rm.community.records.SearchRecordsTests.*;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;

/**
 * Audit Access tests
 * @author Kavit Shah
 */

public class FoldersDispositionScheduleTests extends BaseRMRestTest {

    /** data prep services */
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    @Autowired
    private RecordsAPI recordsAPI;
    @Autowired
    private RecordFoldersAPI recordFoldersAPI;
    @Autowired
    private DispositionScheduleService dispositionScheduleService;
    private RecordCategory Category1;
    private final String TEST_PREFIX = generateTestPrefix(FoldersDispositionScheduleTests.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    private final String RM_USER = TEST_PREFIX + "rm_user_no_clearance";
    private final String folderDisposition = TEST_PREFIX + "RM-2937 folder ghosting";
    private final String electronicRecord = "RM-2937 electronic 2 record";
    private final String nonElectronicRecord = "RM-2937 non-electronic record";
    private final String recordsCategoryWithout = TEST_PREFIX + "category without ghosting";
    public static final String TITLE = "Title";
    /**
     * Test covering RM-2937 for disposition schedule applied to folders
     */
    @Test
    @AlfrescoTest(jira="RM-2937")
    public void foldersDispositionScheduleWithGhosting() {

        // create test precondition
        createTestPrecondition(recordsCategoryWithout);

        // create user with RM User role
        createRMUser();

        // create disposition schedule
        dispositionScheduleService.createCategoryRetentionSchedule(Category1.getName(), false);

        // add cut off step
        dispositionScheduleService.addCutOffAfterPeriodStep(Category1.getName(), "day|1", CREATED_DATE);

        // add destroy step with ghosting
        dispositionScheduleService.addDestroyWithGhostingAfterPeriodStep(Category1.getName(), "day|1", CUT_OFF_DATE);

        RecordCategoryChild folder1 = createFolder(getAdminUser(),Category1.getId(),folderDisposition);

        recordsAPI.uploadElectronicRecord(getAdminUser().getUsername(),
            getAdminUser().getPassword(),
            getDefaultElectronicRecordProperties(electronicRecord),
            folderDisposition,
            CMISUtil.DocumentType.TEXT_PLAIN);

        recordsAPI.createNonElectronicRecord(getAdminUser().getUsername(),
            getAdminUser().getPassword(),getDefaultNonElectronicRecordProperties(nonElectronicRecord),
            Category1.getName(), folderDisposition);

        // complete records
        String nonElRecordName = recordsAPI.getRecordFullName(getAdminUser().getUsername(),
            getAdminUser().getPassword(), folderDisposition, nonElectronicRecord);
        String elRecordName = recordsAPI.getRecordFullName(getAdminUser().getUsername(),
            getAdminUser().getPassword(), folderDisposition, electronicRecord);
        recordsAPI.completeRecord(RM_ADMIN, DEFAULT_PASSWORD, nonElRecordName);
        recordsAPI.completeRecord(RM_ADMIN, DEFAULT_PASSWORD, elRecordName);

        // edit disposition date and cut off the folder
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),editDispositionDateJson(),folder1.getName());
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","cutoff"),folder1.getName());

        // edit disposition date and destroy the folder
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),editDispositionDateJson(),folder1.getName());
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","destroy"),folder1.getName());

        // check the electronic record content is not available
        CmisObject elRecordContent = recordsAPI.getRecord(getAdminUser().getUsername(),
            getAdminUser().getPassword(),folderDisposition,elRecordName);

        CmisObject nonElRecordContent = recordsAPI.getRecord(getAdminUser().getUsername(),
            getAdminUser().getPassword(),folderDisposition,nonElRecordName);

        // delete electronic record
        recordsAPI.deleteRecord(getAdminUser().getUsername(),
            getAdminUser().getPassword(),elRecordName,Category1.getName(),folderDisposition);
        recordsAPI.deleteRecord(getAdminUser().getUsername(),
            getAdminUser().getPassword(),nonElRecordName,Category1.getName(),folderDisposition);

        // delete category
        deleteRecordCategory(Category1.getId());
    }

    @Test
    @AlfrescoTest(jira="RM-2937")
    public void foldersDispositionScheduleWithoutGhosting() {
        // create test precondition
        createTestPrecondition(recordsCategoryWithout);

        // create disposition schedule
        dispositionScheduleService.createCategoryRetentionSchedule(Category1.getName(), false);

        // add cut off step
        dispositionScheduleService.addCutOffAfterPeriodStep(Category1.getName(), "day|1", CREATED_DATE);

        // add destroy step without ghosting
        dispositionScheduleService.addDestroyWithoutGhostingAfterPeriodStep(Category1.getName(), "day|1", CUT_OFF_DATE);

        RecordCategoryChild folder1 = createFolder(getAdminUser(),Category1.getId(),folderDisposition);

        recordsAPI.uploadElectronicRecord(getAdminUser().getUsername(),
            getAdminUser().getPassword(),
            getDefaultElectronicRecordProperties(electronicRecord),
            folderDisposition,
            CMISUtil.DocumentType.TEXT_PLAIN);

        recordsAPI.createNonElectronicRecord(getAdminUser().getUsername(),
            getAdminUser().getPassword(),getDefaultNonElectronicRecordProperties(nonElectronicRecord),
            Category1.getName(), folderDisposition);

        // complete records
        String nonElRecordName = recordsAPI.getRecordFullName(getAdminUser().getUsername(),
            getAdminUser().getPassword(), folderDisposition, nonElectronicRecord);
        String elRecordName = recordsAPI.getRecordFullName(getAdminUser().getUsername(),
            getAdminUser().getPassword(), folderDisposition, electronicRecord);
        recordsAPI.completeRecord(RM_ADMIN, DEFAULT_PASSWORD, nonElRecordName);
        recordsAPI.completeRecord(RM_ADMIN, DEFAULT_PASSWORD, elRecordName);

        // edit disposition date and cut off the folder
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),editDispositionDateJson(),folder1.getName());
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","cutoff"),folder1.getName());

        // edit disposition date and destroy the folder
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),editDispositionDateJson(),folder1.getName());
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","destroy"),folder1.getName());

        // delete category
        deleteRecordCategory(Category1.getId());
    }

    private void createTestPrecondition(String categoryName) {
        createRMSiteIfNotExists();

        // create "rm admin" user if it does not exist and assign it to RM Administrator role
        rmRolesAndActionsAPI.createUserAndAssignToRole(
            getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            RM_ADMIN, DEFAULT_PASSWORD, "Administrator");

        // create category
        STEP("Create two category");
        Category1 = createRootCategory(categoryName,"Title");
    }

    private void createRMUser() {
        getDataUser().deleteUser(getDataUser().createUser(RM_USER));
        // create "rm_user_no_clearance" and assign it to RM User role
        rmRolesAndActionsAPI.createUserAndAssignToRole(
            getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            RM_USER, DEFAULT_PASSWORD,
            "User");
    }

    private Map<BaseAPI.RMProperty, String> getDefaultElectronicRecordProperties(String recordName) {
        Map<BaseAPI.RMProperty, String> defaultProperties = new HashMap<>();
        defaultProperties.put(BaseAPI.RMProperty.NAME, recordName);
        defaultProperties.put(BaseAPI.RMProperty.TITLE, TITLE);
        defaultProperties.put(BaseAPI.RMProperty.DESCRIPTION, DESCRIPTION);
        defaultProperties.put(BaseAPI.RMProperty.CONTENT, TEST_CONTENT);
        return defaultProperties;
    }

    public Map<BaseAPI.RMProperty, String> getDefaultNonElectronicRecordProperties(String recordName)
    {
        Map<BaseAPI.RMProperty, String> defaultProperties = new HashMap<>();
        defaultProperties.put(BaseAPI.RMProperty.NAME, recordName);
        defaultProperties.put(BaseAPI.RMProperty.TITLE, TITLE);
        defaultProperties.put(BaseAPI.RMProperty.DESCRIPTION, DESCRIPTION);
        return defaultProperties;
    }

    private JSONObject editDispositionDateJson() {
        JSONObject requestParams = new JSONObject();

        requestParams.put("name","editDispositionActionAsOfDate");
        JSONObject params = new JSONObject();
        requestParams.put("params",params);

        JSONObject asOfDate = new JSONObject();
        params.put("asOfDate",asOfDate);
        asOfDate.put("iso8601",getCurrentDate());
        return requestParams;
    }

    private String getCurrentDate() {
        Date date = new Date(System.currentTimeMillis());
        // Conversion
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date);
    }

}
