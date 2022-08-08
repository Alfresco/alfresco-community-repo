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

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.rules.ActionsOnRule;
import org.alfresco.rest.rm.community.model.rules.RuleDefinition;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainer;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.rest.v0.RulesAPI;
import org.alfresco.rest.v0.service.DispositionScheduleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static java.util.Arrays.asList;
import static org.alfresco.rest.core.v0.BaseAPI.NODE_PREFIX;
import static org.alfresco.rest.rm.community.base.TestData.DEFAULT_PASSWORD;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.CREATED_DATE;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.CREATED;

public class DeclareInPlaceRecordsTestLevel2 extends BaseRMRestTest {

    private final String TEST_PREFIX = generateTestPrefix(DeclareInPlaceRecordTests.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    private final String RECORDS_CATEGORY = TEST_PREFIX + "category";
    public static final String RECORD_FOLDER_ONE = "record-folder-one";
    private final String RULE_NAME = TEST_PREFIX + "rule unfiled";
    private String unfiledRecordsNodeRef;
    private UserModel testUser;
    private SiteModel testSite;
    private FolderModel testFolder;
    private RecordCategory Category;
    private SiteModel privateSite;
    @Autowired
    private DispositionScheduleService dispositionScheduleService;
    @Autowired
    private RulesAPI rulesAPI;

    /**
     * data prep services
     */
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    @Autowired
    private RecordsAPI recordsAPI;

    @BeforeClass(alwaysRun = true)
    public void preConditions() {
        STEP("Create RM Site");
        createRMSiteIfNotExists();
    }

    /**
     * Given that a user is the owner of a document
     * And that user has been deleted
     * When admin tries to declare the document as a record
     * Then the document becomes an inplace record
     */
    @Test
    @AlfrescoTest(jira="RM-2584")
    public void DeclareRecordOwnerDeleted() throws Exception {

        createTestPrecondition();

        // Upload document in a folder in a collaboration site
        FileModel uploadedDoc = dataContent.usingSite(testSite)
            .usingUser(testUser)
            .usingResource(testFolder)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        // delete the test user
        dataUser.deleteUser(testUser);

        // declare uploadedDocument as record
        getRestAPIFactory().getFilesAPI(getDataUser().getAdminUser()).declareAsRecord(uploadedDoc.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);

        // assert that the document is now a record
        assertTrue(hasRecordAspect(uploadedDoc));
    }

    /**
     * Given that a user is the owner of a document
     * And that user declare the document as a record
     * When admin files the record to a category that has a disposition schedule applied on records and a cut off step
     * And admin completes the record so the pending record action is now Cut off
     * Then user is still able to see the in place record in original share site location
     */
    @Test
    @AlfrescoTest(jira="MNT-18558")
    public void inPlaceRecordVisibilityAfterFilingToCategoryWithCutOffStep() throws Exception {

        // create test precondition
        createTestPrecondition(RECORDS_CATEGORY);

        //create a disposition schedule on Records level with a cut off step
        dispositionScheduleService.createCategoryRetentionSchedule(RECORDS_CATEGORY, true);
        dispositionScheduleService.addCutOffAfterPeriodStep(RECORDS_CATEGORY, "day|2", CREATED_DATE);

        //create a folder in category
        createFolder(getAdminUser(),Category.getId(),RECORD_FOLDER_ONE);

        // create a File to record folder rule applied on Unfiled Records container
        fileToRuleAppliedOnUnfiledRecords();

        //create a new test user
        UserModel testUser = createSiteManager();

        // upload a new document as the user and declare the document as record
        FileModel uploadedDoc = dataContent.usingSite(privateSite)
            .usingUser(testUser)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        Record uploadedRecord = getRestAPIFactory().getFilesAPI(getDataUser().getAdminUser()).declareAsRecord(uploadedDoc.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);

        //Complete the record as admin to be sure that pending action is now Cut off
        recordsAPI.completeRecord(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), uploadedRecord.getName());

        // As test user navigate to collaboration site documents library and check that the record is still visible
    }

    private void createTestPrecondition(String categoryName) {

        // create "rm admin" user if it does not exist and assign it to RM Administrator role
        rmRolesAndActionsAPI.createUserAndAssignToRole(
            getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            RM_ADMIN, DEFAULT_PASSWORD, "Administrator");

        // create category
        STEP("Create category");
        Category = createRootCategory(categoryName,"Title");

        privateSite = dataSite.usingAdmin().createPrivateRandomSite();

        UnfiledContainer unfiledContainer = getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainer(UNFILED_RECORDS_CONTAINER_ALIAS);

        unfiledRecordsNodeRef = NODE_PREFIX + unfiledContainer.getId();
    }

    private void createTestPrecondition() {
        STEP("Create collab_user user");
        testUser = getDataUser().createRandomTestUser();
        testSite = dataSite.usingAdmin().createPublicRandomSite();

        // invite collab_user to Collaboration site with Contributor role
        getDataUser().addUserToSite(testUser, testSite, UserRole.SiteContributor);

        testFolder = dataContent.usingSite(testSite).usingUser(testUser).createFolder();
    }

    private void fileToRuleAppliedOnUnfiledRecords() {
        unfiledRecordsRuleTeardown();

        // create a rule
        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title(RULE_NAME)
            .description(RULE_NAME)
            .createRecordPath(false)
            .path("/" + RECORDS_CATEGORY + "/" + RECORD_FOLDER_ONE)
            .runInBackground(true)
            .actions(asList(ActionsOnRule.FILE_TO.getActionValue()));

        // create a rule on unfiledRecords
        rulesAPI.createRule(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), unfiledRecordsNodeRef, ruleDefinition);
    }

    private void unfiledRecordsRuleTeardown() {
        rulesAPI.deleteAllRulesOnContainer(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), unfiledRecordsNodeRef);
    }

    public UserModel createSiteManager() {
        UserModel siteManager = getDataUser().createRandomTestUser();
        getDataUser().addUserToSite(siteManager, privateSite, UserRole.SiteManager);
        return siteManager;
    }
}
