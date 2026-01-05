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

package org.alfresco.rest.rm.community.smoke;

import org.alfresco.rest.core.v0.RMEvents;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordFoldersAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.rest.v0.service.DispositionScheduleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.Utility;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import java.io.IOException;
import java.time.Instant;
import static org.alfresco.rest.rm.community.base.TestData.DEFAULT_PASSWORD;
import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.CUT_OFF_DATE;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;

/**
 * Contains recordsDispositionScheduleWithoutGhosting test which checks disposition schedule cut off, transfer and destroy without maintaining metadata steps applied to records
 * <p/>
 * Precondition:
 * <p/>
 * RM site created, contains an empty category "RM-2801 disposition for records". <p/>
 * RM user has RM admin role. <p/>
 * A transfer location named "transferred files" is created to which RM user has access
 * <p/>
 * <img src="doc-files/Disposition Schedule without ghosting.png" alt="Records Disposition Schedule without ghosting" />
 *
 * @author Kavit Shah
 */

public class RecordsDispositionScheduleTests extends BaseRMRestTest {

    /** data prep 6services */
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    @Autowired
    private RecordsAPI recordsAPI;
    @Autowired
    private RecordFoldersAPI recordFoldersAPI;
    @Autowired
    private DispositionScheduleService dispositionScheduleService;
    private RecordCategory Category1;
    private final String TEST_PREFIX = generateTestPrefix(RecordsDispositionScheduleTests.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    private final String recordsCategory = TEST_PREFIX + "RM-2801 category";
    private final String folderDisposition = TEST_PREFIX + "RM-2801 folder";

    @Test
    @AlfrescoTest(jira="RM-2801")
    public void recordsDispositionScheduleWithoutGhosting() {

        // create test precondition
        createTestPrecondition(recordsCategory);

        // create disposition schedule
        dispositionScheduleService.createCategoryRetentionSchedule(Category1.getName(), true);

        // add cut off step
        dispositionScheduleService.addCutOffImmediatelyStep(Category1.getName());

        // add transfer step
        dispositionScheduleService.addTransferAfterEventStep(Category1.getName(),"transferred records","all_allowances_granted_are_terminated");

        // add destroy step without retaining metadata
        dispositionScheduleService.addDestroyWithoutGhostingAfterPeriodStep(Category1.getName(), "day|1", CUT_OFF_DATE);

        // create a folder and an electronic and a non-electronic record in it
        RecordCategoryChild FOLDER_DESTROY = createFolder(getAdminUser(),Category1.getId(),folderDisposition);

        String electronicRecord = "RM-2801 electronic record";
        Record elRecord = createElectronicRecord(FOLDER_DESTROY.getId(), electronicRecord);
        String nonElectronicRecord = "RM-2801 non-electronic record";
        Record nonElRecord = createNonElectronicRecord(FOLDER_DESTROY.getId(), nonElectronicRecord);

        // complete records and cut them off
        String nonElRecordName = recordsAPI.getRecordFullName(getAdminUser().getUsername(),
            getAdminUser().getPassword(), folderDisposition, nonElectronicRecord);
        String elRecordName = recordsAPI.getRecordFullName(getAdminUser().getUsername(),
            getAdminUser().getPassword(), folderDisposition, electronicRecord);

        // complete records and cut them off
        completeRecord(elRecord.getId());
        completeRecord(nonElRecord.getId());

        String nonElRecordNameNodeRef = recordsAPI.getRecordNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), nonElRecordName, "/" + Category1.getName() + "/" + folderDisposition);
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","cutoff"),nonElRecordNameNodeRef);

        String elRecordNameNodeRef = recordsAPI.getRecordNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), elRecordName, "/" + Category1.getName() + "/" + folderDisposition);
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","cutoff"),elRecordNameNodeRef);

        // ensure the complete event action is displayed for both events
        rmRolesAndActionsAPI.completeEvent(getAdminUser().getUsername(),
               getAdminUser().getPassword(), nonElRecordName, RMEvents.ALL_ALLOWANCES_GRANTED_ARE_TERMINATED, Instant.now());
        rmRolesAndActionsAPI.completeEvent(getAdminUser().getUsername(),
            getAdminUser().getPassword(), elRecordName, RMEvents.ALL_ALLOWANCES_GRANTED_ARE_TERMINATED, Instant.now());

        // Create and Complete transfer
        HttpResponse nonElRecordNameHttpResponse = recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","transfer"),recordsAPI.getRecordNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), nonElRecordName, "/" + Category1.getName() + "/" + folderDisposition));

        String nonElRecordNameTransferId = getTransferId(nonElRecordNameHttpResponse,nonElRecordNameNodeRef);
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","transferComplete"),nonElRecordNameTransferId);

        HttpResponse elRecordNameHttpResponse = recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","transfer"),recordsAPI.getRecordNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), elRecordName, "/" + Category1.getName() + "/" + folderDisposition));

        String elRecordNameTransferId = getTransferId(elRecordNameHttpResponse,elRecordNameNodeRef);
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","transferComplete"),elRecordNameTransferId);

        // edit the disposition schedule date to current date
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),editDispositionDateJson(),nonElRecordNameNodeRef);
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),editDispositionDateJson(),elRecordNameNodeRef);

        Utility.waitToLoopTime(5,"Waiting for Edit Disposition to be processed");

        // destroy records
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","destroy"),nonElRecordNameNodeRef);
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","destroy"),elRecordNameNodeRef);

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

    private String getTransferId(HttpResponse httpResponse,String nodeRef) {
        HttpEntity entity = httpResponse.getEntity();
        String responseString = null;
        try {
            responseString = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JSONObject result = new JSONObject(responseString);
        return result
            .getJSONObject("results")
            .get(nodeRef)
            .toString();

    }
}
