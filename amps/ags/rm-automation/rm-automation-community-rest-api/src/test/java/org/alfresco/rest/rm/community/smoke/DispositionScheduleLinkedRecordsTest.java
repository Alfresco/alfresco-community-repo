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
import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.*;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.NO_CONTENT;

public class DispositionScheduleLinkedRecordsTest extends BaseRMRestTest {
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
    private final static  String TEST_PREFIX = generateTestPrefix(DispositionScheduleLinkedRecordsTest.class);
    private RecordCategory Category1;
    private RecordCategoryChild CopyCatFolder,folder1,CatFolder,folder2;
    private static final String categoryRM3077 = TEST_PREFIX + "RM-3077_manager_sees_me";
    private static final String copyCategoryRM3077 = "Copy_of_" + categoryRM3077;
    private static final String folderRM3077 = "RM-3077_folder_"+ categoryRM3077;
    private static final String copyFolderRM3077 = "Copy_of_" + folderRM3077;
    private final String folder = TEST_PREFIX + "RM-2937 folder ghosting";
    private static final String firstCategoryRM3060 = TEST_PREFIX + "RM-3060_category_record";
    private static final String secondCategoryRM3060 = "Copy_of_" + firstCategoryRM3060;
    private static final String firstFolderRM3060 = TEST_PREFIX + "RM-3060_folder";
    private static final String secondFolderRM3060 = TEST_PREFIX + "RM-3060_disposition_on_Record_Level";
    private static final String electronicRecordRM3060 = TEST_PREFIX + "RM-3060_electronic_1_record";
    private static final String nonElectronicRecordRM3060 = TEST_PREFIX + "RM-3060_non-electronic_record";
    private static final String firstCategoryRM1622 = TEST_PREFIX + "RM-1622_category_record";
    private static final String secondCategoryRM1622 = "Copy_of_" + firstCategoryRM1622;;
    private static final String firstFolderRM1622 = TEST_PREFIX + "RM-1622_folder";
    private static final String electronicRecordRM1622 = TEST_PREFIX + "RM-1622_electronic_1_record";
    private static final String secondFolderRM1622 = TEST_PREFIX + "RM-1622_disposition_on_Record_Level";
    private static final String TRANSFER_LOCATION = TEST_PREFIX + "RM-3060_transferred_records";
    public static final String TRANSFER_TYPE = "rma:transferred";
    private FilePlan filePlanModel;
    private UserModel rmAdmin, rmManager;

    @BeforeClass(alwaysRun = true)
    public void setupDispositionScheduleLinkedRecordsTest() {
        createRMSiteIfNotExists();
        //get file plan
        filePlanModel = getRestAPIFactory().getFilePlansAPI().getFilePlan(FILE_PLAN_ALIAS);

        // create "rm admin" user if it does not exist and assign it to RM Administrator role
        rmAdmin = getDataUser().createRandomTestUser();
        rmRolesAndActionsAPI.assignRoleToUser(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),rmAdmin.getUsername(),
            UserRoles.ROLE_RM_ADMIN.roleId);

        // create "rm Manager" user if it does not exist and assign it to RM Administrator role
        rmManager = getDataUser().createRandomTestUser();
        rmRolesAndActionsAPI.assignRoleToUser(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),rmManager.getUsername(),
            UserRoles.ROLE_RM_MANAGER.roleId);
    }
    /**
     * Disposition Schedule on Record Folder with linked records test
     * <p>
     * Precondition:
     * <p>
     * Create rm_manager user that would have RM Managers role, rm_admin that would have RM Administrator role.
     * Log in with admin user, create a category "manager sees me", give rm_manager read&file permission over it.
     * Create a disposition schedule for it that would cut off folders after 1 day from created date. Copy the category.
     * <p>
     * <p/> TestRail Test C775<p/>
     **/
    @Test
    @AlfrescoTest(jira = "RM-1622")
    public void dispositionScheduleLinkedRecords() throws UnsupportedEncodingException {
        STEP("Create record category");
        Category1 = createRootCategory(categoryRM3077);

        //create retention schedule
        dispositionScheduleService.createCategoryRetentionSchedule(Category1.getName(), false);

        // add cut off step
        dispositionScheduleService.addCutOffAfterPeriodStep(Category1.getName(), "day|2", CREATED_DATE);

        //create a copy of the category recordsCategory
        String CopyCategoryId = copyCategory(getAdminUser(),Category1.getId(), copyCategoryRM3077);

        // create folders in both categories
        CatFolder = createRecordFolder(Category1.getId(), folderRM3077);
        CopyCatFolder = createRecordFolder(CopyCategoryId, copyFolderRM3077);

        // create record  files
        String electronicRecord = "RM-2801 electronic record";
        Record elRecord = createElectronicRecord(CatFolder.getId(), electronicRecord);
        String elRecordFullName = recordsAPI.getRecordFullName(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), CatFolder.getName(), electronicRecord);

        String nonElectronicRecord = "RM-2801 non-electronic record";
        Record nonElRecord = createNonElectronicRecord(CatFolder.getId(), nonElectronicRecord);
        String nonElRecordFullName = recordsAPI.getRecordFullName(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), CatFolder.getName(), nonElectronicRecord);

        // link the records to copy folder, then complete them
        List<String> recordLists = new ArrayList<>();
        recordLists.add(NODE_REF_WORKSPACE_SPACES_STORE + elRecord.getId());
        recordLists.add(NODE_REF_WORKSPACE_SPACES_STORE + nonElRecord.getId());

        linksAPI.linkRecord(getDataUser().getAdminUser().getUsername(),
            getDataUser().getAdminUser().getPassword(), HttpStatus.SC_OK,copyCategoryRM3077 + "/" +
                copyFolderRM3077, recordLists);
        recordsAPI.completeRecord(rmAdmin.getUsername(), rmAdmin.getPassword(), elRecordFullName);
        recordsAPI.completeRecord(rmAdmin.getUsername(), rmAdmin.getPassword(), nonElRecordFullName);

        // edit disposition date
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),editDispositionDateJson(),CatFolder.getName());

        // cut off the Folder
        recordFoldersAPI.postFolderAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","cutoff"),CatFolder.getName());

        // Verify the Content
        Node electronicNode = getNode(elRecord.getId());
        assertTrue("The content of " + electronicRecord + " is available",
            StringUtils.isEmpty(electronicNode.getNodeContent().getResponse().getBody().asString()));

        // verify the Properties
        AssertJUnit.assertNull("The properties are present even after cutting off the record.", elRecord.getProperties().getTitle());

        // delete precondition
        deleteRecordCategory(Category1.getId());
        deleteRecordCategory(CopyCategoryId);
    }
    /**
     * Test covering RM-3060
     * Check the disposition steps for a record can be executed
     * When the record is linked to a folder with the same disposition schedule
     * */
    @Test
    @AlfrescoTest (jira = "RM-3060")
    public void sameDispositionScheduleLinkedRecords() throws UnsupportedEncodingException {

        // create a category with retention applied on records level
        RecordCategory recordCategory = getRestAPIFactory().getFilePlansAPI(rmAdmin)
            .createRootRecordCategory(RecordCategory.builder().name(firstCategoryRM3060).build(),
                RecordCategory.DEFAULT_FILE_PLAN_ALIAS);
        dispositionScheduleService.createCategoryRetentionSchedule(firstCategoryRM3060, true);
        dispositionScheduleService.addCutOffAfterPeriodStep(firstCategoryRM3060, "week|1", DATE_FILED);
        dispositionScheduleService.addTransferAfterEventStep(firstCategoryRM3060, TRANSFER_LOCATION, RMEvents.CASE_CLOSED.getEventName());
        dispositionScheduleService.addDestroyWithoutGhostingAfterPeriodStep(firstCategoryRM3060, "week|1", CUT_OFF_DATE);

        // make a copy of the category created
        String categorySecondId = copyCategory(getAdminUser(), recordCategory.getId(), secondCategoryRM3060);

        // create a folder on the category firstCategoryRM3060 with a complete electronic record
        RecordCategoryChild firstFolderRecordCategoryChild = createRecordFolder(recordCategory.getId(),firstFolderRM3060);
        Record firstElectronicRecord = createElectronicRecord(firstFolderRecordCategoryChild.getId(),electronicRecordRM3060);

        String elRecordFullName = recordsAPI.getRecordFullName(getDataUser().getAdminUser().getUsername(),
            getDataUser().getAdminUser().getPassword(),firstFolderRM3060, electronicRecordRM3060);
        String elRecordNameNodeRef = recordsAPI.getRecordNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), elRecordFullName, "/" + firstCategoryRM3060 + "/" + firstFolderRM3060);

        recordsAPI.completeRecord(getDataUser().getAdminUser().getUsername(),
            getDataUser().getAdminUser().getPassword(), elRecordFullName);

        // create a folder on the category secondCategoryRM3060 with a non electronic record
        RecordCategoryChild secondFolderRecordCategoryChild = createRecordFolder(categorySecondId,secondFolderRM3060);
        Record secondNonElectronicRecord = createNonElectronicRecord(secondFolderRecordCategoryChild.getId(),nonElectronicRecordRM3060);

        // link the nonElectronicRecordRM3060 to firstFolderRM3060
        List<String> recordLists = new ArrayList<>();
        recordLists.add(NODE_REF_WORKSPACE_SPACES_STORE + secondNonElectronicRecord.getId());

        linksAPI.linkRecord(getDataUser().getAdminUser().getUsername(),
            getDataUser().getAdminUser().getPassword(), HttpStatus.SC_OK,secondCategoryRM3060 + "/" +
                secondFolderRM3060, recordLists);
        String nonElRecordFullName = recordsAPI.getRecordFullName(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), secondFolderRM3060, secondNonElectronicRecord.getName());
        String nonElRecordNameNodeRef = recordsAPI.getRecordNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), nonElRecordFullName, "/" + secondCategoryRM3060 + "/" + secondFolderRM3060);

        // complete records and cut them off
        recordsAPI.completeRecord(getDataUser().getAdminUser().getUsername(),
            getDataUser().getAdminUser().getPassword(), nonElRecordFullName);

        // edit the disposition date
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),editDispositionDateJson(),nonElRecordNameNodeRef);

        // cut off the record
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","cutoff"),nonElRecordNameNodeRef);

        //check the record is cut off
        AssertJUnit.assertTrue("The file " + nonElectronicRecordRM3060 + " has not been successfully cut off.", getRestAPIFactory().getRecordsAPI().getRecord(secondNonElectronicRecord.getId()).getAspectNames().contains(CUT_OFF_ASPECT));

        // link the electronic record to secondFolderRM3060
        recordLists.clear();
        recordLists.add(NODE_REF_WORKSPACE_SPACES_STORE + secondNonElectronicRecord.getId());
        linksAPI.linkRecord(getDataUser().getAdminUser().getUsername(),
            getDataUser().getAdminUser().getPassword(), HttpStatus.SC_OK,secondCategoryRM3060 + "/" +
                secondFolderRM3060, recordLists);

        // edit the disposition date and cut off the record
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),editDispositionDateJson(),elRecordNameNodeRef);
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","cutoff"),elRecordNameNodeRef);

        AssertJUnit.assertTrue("The file " + electronicRecordRM3060 + " has not been successfully cut off.", getRestAPIFactory().getRecordsAPI().getRecord(firstElectronicRecord.getId()).getAspectNames().contains(CUT_OFF_ASPECT));

        // open the record and complete the disposition schedule event
        rmRolesAndActionsAPI.completeEvent(getAdminUser().getUsername(),
            getAdminUser().getPassword(), elRecordFullName, RMEvents.CASE_CLOSED, Instant.now());
        rmRolesAndActionsAPI.completeEvent(getAdminUser().getUsername(),
            getAdminUser().getPassword(), nonElRecordFullName, RMEvents.CASE_CLOSED, Instant.now());

        // transfer the files & complete transfers
        HttpResponse nonElRecordNameHttpResponse = recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","transfer"),recordsAPI.getRecordNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), nonElRecordFullName, "/" + secondCategoryRM3060 + "/" + secondFolderRM3060));

        String nonElRecordNameTransferId = getTransferId(nonElRecordNameHttpResponse,nonElRecordNameNodeRef);
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","transferComplete"),nonElRecordNameTransferId);

        HttpResponse elRecordNameHttpResponse = recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","transfer"),recordsAPI.getRecordNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), elRecordFullName, "/" + firstCategoryRM3060 + "/" + firstFolderRM3060));

        String elRecordNameTransferId = getTransferId(elRecordNameHttpResponse,elRecordNameNodeRef);
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","transferComplete"),elRecordNameTransferId);

        AssertJUnit.assertTrue("The file " + electronicRecordRM3060 + " has not been successfully transferred", getRestAPIFactory().getRecordsAPI().getRecord(firstElectronicRecord.getId()).getAspectNames().contains(TRANSFER_TYPE));
        AssertJUnit.assertTrue("The file " + nonElectronicRecordRM3060 + " has not been successfully transferred.", getRestAPIFactory().getRecordsAPI().getRecord(secondNonElectronicRecord.getId()).getAspectNames().contains(TRANSFER_TYPE));

        // edit the disposition date for nonElectronicRecordRM3060 & electronicRecordRM3060
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),editDispositionDateJson(),nonElRecordNameNodeRef);
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),editDispositionDateJson(),elRecordNameNodeRef);

        // destroy nonElectronicRecordRM3060 & electronicRecordRM3060 records
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","destroy"),nonElRecordNameNodeRef);
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),new JSONObject().put("name","destroy"),elRecordNameNodeRef);

        // check the file is not displayed
       assertNull("The file " + nonElectronicRecordRM3060 + " has not been successfully destroyed.", secondNonElectronicRecord.getContent());
       assertNull("The file " + electronicRecordRM3060 + " has not been successfully destroyed.", firstElectronicRecord.getContent());

        // delete precondition
        deleteRecordCategory(recordCategory.getId());
        deleteRecordCategory(categorySecondId);
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

    @Test
    @AlfrescoTest(jira = "RM-1622")
    public void sameLevelDispositionScheduleStepsPeriodsCalculation() throws Exception {

        // create a category with retention applied on records level
        RecordCategory catsameLevel1  = getRestAPIFactory().getFilePlansAPI(rmAdmin)
                                                           .createRootRecordCategory(RecordCategory.builder().name(firstCategoryRM1622).build(),
                                                               RecordCategory.DEFAULT_FILE_PLAN_ALIAS);
        RecordCategory catsameLevel2  = getRestAPIFactory().getFilePlansAPI(rmAdmin)
                                                           .createRootRecordCategory(RecordCategory.builder().name(secondCategoryRM1622).build(),
                                                               RecordCategory.DEFAULT_FILE_PLAN_ALIAS);

        // create retention schedule applied on records for category 1
        dispositionScheduleService.createCategoryRetentionSchedule(firstCategoryRM1622, true);

        // with retain immediately after record creation date and cut 1 day after record creation date
        dispositionScheduleService.addCutOffAfterPeriodStep(firstCategoryRM1622, "day|1", DATE_FILED);


        // create a folder on the category firstCategoryRM1622 with a complete electronic record
        RecordCategoryChild firstFolderRecordCategoryChild = createRecordFolder(catsameLevel1.getId(),firstFolderRM1622);
        Record firstElectronicRecord = createElectronicRecord(firstFolderRecordCategoryChild.getId(),electronicRecordRM1622);

        String elRecordFullName = recordsAPI.getRecordFullName(getDataUser().getAdminUser().getUsername(),
            getDataUser().getAdminUser().getPassword(),firstFolderRM1622, electronicRecordRM1622);
        String elRecordNameNodeRef = recordsAPI.getRecordNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), elRecordFullName, "/" + firstCategoryRM1622 + "/" + firstFolderRM1622);

        recordsAPI.completeRecord(getDataUser().getAdminUser().getUsername(),
            getDataUser().getAdminUser().getPassword(), elRecordFullName);

        // create a folder on the category secondCategoryRM1622 with a non electronic record
        RecordCategoryChild secondFolderRecordCategoryChild = createRecordFolder(catsameLevel2.getId(),secondFolderRM1622);
        String elRecordNameNodeRefs = recordsAPI.getRecordNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), elRecordFullName, "/" + firstCategoryRM1622 + "/" + firstFolderRM1622);


        // link it to the folder in second category through the details page
        List<String> recordLists = new ArrayList<>();
        recordLists.add(NODE_REF_WORKSPACE_SPACES_STORE + firstElectronicRecord.getId());

        linksAPI.linkRecord(getDataUser().getAdminUser().getUsername(),
            getDataUser().getAdminUser().getPassword(), HttpStatus.SC_OK,secondCategoryRM1622 + "/" +
                secondFolderRM1622, recordLists);

        // edit disposition date
        recordFoldersAPI.postRecordAction(getAdminUser().getUsername(),
            getAdminUser().getPassword(),editDispositionDateJson(),elRecordNameNodeRefs);


    }

    @Test (dependsOnMethods = {"sameLevelDispositionScheduleStepsPeriodsCalculation" })
    public void deleteLongestPeriodTestPrecondition() {
        // Delete the RM site
        getRestAPIFactory().getRMSiteAPI().deleteRMSite();

        // Verify the status code
        assertStatusCode(NO_CONTENT);
    }
    }
