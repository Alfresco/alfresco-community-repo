/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
import org.alfresco.rest.model.RestNodeBodyMoveCopyModel;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.requests.Node;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.fileplan.FilePlan;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.v0.LinksAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordFoldersAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.rest.v0.service.DispositionScheduleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.UserModel;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.alfresco.rest.core.v0.BaseAPI.NODE_REF_WORKSPACE_SPACES_STORE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAspects.CUT_OFF_ASPECT;
import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.CREATED_DATE;
import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.DATE_FILED;
import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.CUT_OFF_DATE;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.NO_CONTENT;

public class Sampletest extends BaseRMRestTest {
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    @Autowired
    private DispositionScheduleService dispositionScheduleService;
    @Autowired
    private LinksAPI linksAPI;
    @Autowired
    private RecordsAPI recordsAPI;
    @Autowired
    private RecordFoldersAPI recordFoldersAPI;
    private final static String TEST_PREFIX = generateTestPrefix(DispositionScheduleLinkedRecordsTest.class);
    private static final String CATEGORY_RM_3077 = TEST_PREFIX + "RM-3077_manager_sees_me";
    private static final String COPY_CATEGORY_RM_3077 = "Copy_of_" + CATEGORY_RM_3077;
    private static final String FOLDER_RM_3077 = "RM-3077_folder_" + CATEGORY_RM_3077;
    private static final String COPY_FOLDER_RM_3077 = "Copy_of_" + FOLDER_RM_3077;
    private static final String FIRST_CATEGORY_RM_3060 = TEST_PREFIX + "RM-3060_category_record";
    private static final String SECOND_CATEGORY_RM_3060 = "Copy_of_" + FIRST_CATEGORY_RM_3060;
    private static final String FIRST_FOLDER_RM_3060 = TEST_PREFIX + "RM-3060_folder";
    private static final String SECOND_FOLDER_RM_3060 = TEST_PREFIX + "RM-3060_disposition_on_Record_Level";
    private static final String ELECTRONIC_RECORD_RM_3060 = TEST_PREFIX + "RM-3060_electronic_1_record";
    private static final String NON_ELECTRONIC_RECORD_RM_3060 = TEST_PREFIX + "RM-3060_non-electronic_record";
    private static final String FIRST_CATEGORY_RM_1622 = TEST_PREFIX + "RM-1622_category_record";
    private static final String SECOND_CATEGORY_RM_1622 = "Copy_of_" + FIRST_CATEGORY_RM_1622;
    private static final String FIRST_FOLDER_RM_1622 = TEST_PREFIX + "RM-1622_folder";
    private static final String ELECTRONIC_RECORD_RM_1622 = TEST_PREFIX + "RM-1622_electronic_1_record";
    private static final String SECOND_FOLDER_RM_1622 = TEST_PREFIX + "RM-1622_disposition_on_Record_Level";
    private static final String TRANSFER_LOCATION = TEST_PREFIX + "RM-3060_transferred_records";
    public static final String TRANSFER_TYPE = "rma:transferred";
    private FilePlan filePlanModel;
    private UserModel rmAdmin;

    @BeforeClass(alwaysRun = true)
    public void setupDispositionScheduleLinkedRecordsTest() {
        createRMSiteIfNotExists();
        //get file plan
        filePlanModel = getRestAPIFactory().getFilePlansAPI().getFilePlan(FILE_PLAN_ALIAS);

        // create "rm admin" user if it does not exist and assign it to RM Administrator role
        rmAdmin = getDataUser().createRandomTestUser();
        rmRolesAndActionsAPI.assignRoleToUser(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), rmAdmin.getUsername(),
            UserRoles.ROLE_RM_ADMIN.roleId);

        // create "rm Manager" user if it does not exist and assign it to RM Administrator role
        UserModel rmManager = getDataUser().createRandomTestUser();
        rmRolesAndActionsAPI.assignRoleToUser(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), rmManager.getUsername(),
            UserRoles.ROLE_RM_MANAGER.roleId);
    }
    @Test
    @AlfrescoTest(jira = "RM-1622")
    public void sampledispositionScheduleLinkedRecords() throws UnsupportedEncodingException {
        STEP("Create record category");
        RecordCategory category1 = createRootCategory(CATEGORY_RM_3077);

        //create retention schedule
        dispositionScheduleService.createCategoryRetentionSchedule(category1.getName(), false);

        // add cut off step
        dispositionScheduleService.addCutOffAfterPeriodStep(category1.getName(), "day|2", CREATED_DATE);

        //create a copy of the category recordsCategory
        String copyCategoryId = copyCategory(getAdminUser(), category1.getId(), COPY_CATEGORY_RM_3077);

        // create folders in both categories
        RecordCategoryChild catFolder = createRecordFolder(category1.getId(), FOLDER_RM_3077);
        createRecordFolder(copyCategoryId, COPY_FOLDER_RM_3077);

        // create record  files
        String electronicRecord = "RM-2801 electronic record";
        Record elRecord = createElectronicRecord(catFolder.getId(), electronicRecord);
        String elRecordFullName = recordsAPI.getRecordFullName(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), catFolder.getName(), electronicRecord);

        String nonElectronicRecord = "RM-2801 non-electronic record";
        Record nonElRecord = createNonElectronicRecord(catFolder.getId(), nonElectronicRecord);
        String nonElRecordFullName = recordsAPI.getRecordFullName(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), catFolder.getName(), nonElectronicRecord);

        // link the records to copy folder, then complete them
        List<String> recordLists = new ArrayList<>();
        recordLists.add(NODE_REF_WORKSPACE_SPACES_STORE + elRecord.getId());
        recordLists.add(NODE_REF_WORKSPACE_SPACES_STORE + nonElRecord.getId());

        linksAPI.linkRecord(getDataUser().getAdminUser().getUsername(),
            getDataUser().getAdminUser().getPassword(), HttpStatus.SC_OK, COPY_CATEGORY_RM_3077 + "/" +
                COPY_FOLDER_RM_3077, recordLists);
        recordsAPI.completeRecord(rmAdmin.getUsername(), rmAdmin.getPassword(), elRecordFullName);
        recordsAPI.completeRecord(rmAdmin.getUsername(), rmAdmin.getPassword(), nonElRecordFullName);

        // edit disposition date
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),editDispositionDateJson(), catFolder.getName());

        // cut off the Folder
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","cutoff"), catFolder.getName());

//        // Verify the Content
//        Node electronicNode = getNode(elRecord.getId());
//        assertTrue("The content of " + electronicRecord + " is available",
//            StringUtils.isEmpty(electronicNode.getNodeContent().getResponse().getBody().asString()));
//
//        // verify the Properties
//        AssertJUnit.assertNull("The properties are present even after cutting off the record.", elRecord.getProperties().getTitle());
//
        // delete precondition
        deleteRecordCategory(category1.getId());
        deleteRecordCategory(copyCategoryId);
    }
    private String copyCategory(UserModel user, String categoryId, String copyName) {
        RepoTestModel repoTestModel = new RepoTestModel() {};
        repoTestModel.setNodeRef(categoryId);
        RestNodeModel restNodeModel;

        RestNodeBodyMoveCopyModel copyDestinationInfo = new RestNodeBodyMoveCopyModel();
        copyDestinationInfo.setTargetParentId(filePlanModel.getId());
        copyDestinationInfo.setName(copyName);

        try
        {
            restNodeModel = getRestAPIFactory().getNodeAPI(user, repoTestModel).copy(copyDestinationInfo);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Problem copying category.", e);
        }
        return restNodeModel.getId();
    }

    private Node getNode(String recordId)
    {
        RepoTestModel repoTestModel = new RepoTestModel() {};
        repoTestModel.setNodeRef(recordId);
        return getRestAPIFactory().getNodeAPI(repoTestModel);
    }


}
