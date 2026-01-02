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

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.CREATED;
import static org.testng.Assert.assertTrue;
/**
 * This class contains the tests for
 * Create the Document, marking them as Record, Hiding them using Site Collaborator
 * The Rm_Admin user then verofy if he is able to access the documents using Rest Api
 *
 * @author Kavit Shah
 */
public class DeclareInPlaceRecordTests extends BaseRMRestTest {

    private final String TEST_PREFIX = generateTestPrefix(DeclareInPlaceRecordTests.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    private UserModel testUser;
    private UserModel RmAdminUser;
    private SiteModel testSite;
    private FolderModel testFolder;
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

        STEP("Create RM Admin user");
        rmRolesAndActionsAPI.createUserAndAssignToRole(getAdminUser().getUsername(), getAdminUser().getPassword(), RM_ADMIN,
            getAdminUser().getPassword(),
            "Administrator");

        RmAdminUser = new UserModel(RM_ADMIN,getAdminUser().getPassword());

        STEP("Create collab_user user");
        testUser = getDataUser().createRandomTestUser();
        testSite = dataSite.usingAdmin().createPublicRandomSite();

        // invite collab_user to Collaboration site with Contributor role
        getDataUser().addUserToSite(testUser, testSite, UserRole.SiteContributor);

        testFolder = dataContent.usingSite(testSite).usingUser(testUser).createFolder();
    }

    @Test
    @AlfrescoTest(jira = "RM-2366")
    public void declareInplaceRecord() {

        // Upload document in a folder in a collaboration site
        FileModel uploadedDocHidden = dataContent.usingSite(testSite)
            .usingUser(testUser)
            .usingResource(testFolder)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        // declare uploadedDocument as record
        Record uploadedRecordHidden = getRestAPIFactory().getFilesAPI(testUser).declareAsRecord(uploadedDocHidden.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);

        recordsAPI.hideRecord(testUser.getUsername(),testUser.getPassword(),uploadedRecordHidden.getId());

        // Upload document in a folder in a collaboration site
        FileModel uploadedDocWithoutHidden = dataContent.usingSite(testSite)
            .usingUser(testUser)
            .usingResource(testFolder)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        Record uploadedRecordWithoutHidden = getRestAPIFactory().getFilesAPI(testUser).declareAsRecord(uploadedDocWithoutHidden.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);

        assertTrue(isRecordChildOfUnfiledContainer(uploadedRecordHidden.getId()), uploadedRecordHidden.getId() + " doesn't exist in Unfiled Records");
        assertTrue(isRecordChildOfUnfiledContainer(uploadedRecordWithoutHidden.getId()), uploadedRecordWithoutHidden.getId() + " doesn't exist in Unfiled Records");
    }

    @AfterClass(alwaysRun = true)
    public void deletePreConditions() {
        STEP("Delete the records created in the test");
        getRestAPIFactory()
            .getUnfiledContainersAPI(RmAdminUser)
            .getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS)
            .getEntries()
            .stream()
                .forEach(x -> getRestAPIFactory()
                    .getRecordsAPI()
                    .deleteRecord(x.getEntry().getId()));

    }

    private boolean isRecordChildOfUnfiledContainer(String recordId) {
        return getRestAPIFactory()
            .getUnfiledContainersAPI(RmAdminUser)
            .getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS)
            .getEntries()
            .stream()
            .anyMatch(c -> c.getEntry().getId().equals(recordId));
    }
}
