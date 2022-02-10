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
package org.alfresco.rest.rm.community.files;

import static org.alfresco.rest.rm.community.base.TestData.HOLD_DESCRIPTION;
import static org.alfresco.rest.rm.community.base.TestData.HOLD_REASON;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_FILING;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_READ_RECORDS;
import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_POWER_USER;
import static org.alfresco.rest.rm.community.requests.gscore.api.FilesAPI.PARENT_ID_PARAM;
import static org.alfresco.rest.v0.HoldsAPI.HOLDS_CONTAINER;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.util.DockerHelper;
import org.alfresco.rest.v0.HoldsAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * API tests for declaring document as record and filing it immediately to a record folder location within the file plan
 *
 * @author Claudia Agache
 * @since 3.1
 */
@AlfrescoTest (jira = "RM-6779")
public class DeclareAndFileDocumentAsRecordTests extends BaseRMRestTest
{
    private final static String DESTINATION_PATH_NOT_FOUND_EXC = "Unable to execute create-record action, because the destination path could not be found.";
    private final static String INVALID_DESTINATION_PATH_EXC = "Unable to execute create-record action, because the destination path is invalid.";
    private final static String DESTINATION_PATH_NOT_RECORD_FOLDER_EXC = "Unable to execute create-record action, because the destination path is not a record folder.";
    private final static String CLOSED_RECORD_FOLDER_EXC = "You can't add new items to a closed record folder.";
    private final static String HOLD_NAME = getRandomName("holdName");
    private final static String RECORD_FOLDER_NAME_WITH_SPACE = "Folder With Spaces In Name";

    private UserModel userFillingPermission, userReadOnlyPermission;
    private SiteModel publicSite;
    private FolderModel testFolder;
    private FileModel testFile;
    private RecordCategory recordCategory;
    private RecordCategoryChild recordFolder, subcategoryRecordFolder, closedRecordFolder, recordFolderWithSpacesInName;
    private UnfiledContainerChild unfiledContainerFolder;
    private String holdNodeRef;

    @Autowired
    private DockerHelper dockerHelper;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;

    @Autowired
    private HoldsAPI holdsAPI;

    /**
     * Invalid destination paths where in-place records can't be filed
     */
    @DataProvider (name = "invalidDestinationPaths")
    public Object[][] getInvalidDestinationPaths()
    {
        return new String[][]
            {
                { "/", DESTINATION_PATH_NOT_FOUND_EXC },
                { "Unfiled Records", INVALID_DESTINATION_PATH_EXC },
                { "Transfers", INVALID_DESTINATION_PATH_EXC },
                { "Holds", INVALID_DESTINATION_PATH_EXC },
                { "rm/documentlibrary", DESTINATION_PATH_NOT_FOUND_EXC },
                { recordCategory.getName(), DESTINATION_PATH_NOT_RECORD_FOLDER_EXC },
                // a closed record folder
                { Utility.buildPath(recordCategory.getName(), closedRecordFolder.getName()), CLOSED_RECORD_FOLDER_EXC},
                // an arbitrary unfiled records folder
                { "Unfiled Records/" + unfiledContainerFolder.getName(), INVALID_DESTINATION_PATH_EXC },
                // a collaboration site folder
                { testFolder.getCmisLocation(), DESTINATION_PATH_NOT_FOUND_EXC }
            };
    }

    /**
     * Invalid destination ids where in-place records can't be filed
     */
    @DataProvider (name = "invalidDestinationIds")
    public Object[][] getInvalidDestinationIds()
    {
        return new String[][]
            {
                { getFilePlan(FILE_PLAN_ALIAS).getId() },
                { getUnfiledContainer(UNFILED_RECORDS_CONTAINER_ALIAS).getId() },
                { getTransferContainer(TRANSFERS_ALIAS).getId() },
                { rmRolesAndActionsAPI.getItemNodeRef(getAdminUser().getUsername(), getAdminUser().getPassword(),
                        "/" + HOLDS_CONTAINER) },
                { recordCategory.getId() },
                { unfiledContainerFolder.getId() },
                { testFolder.getNodeRef() }
        };
    }

    @BeforeClass (alwaysRun = true)
    public void declareAndFileDocumentAsRecordSetup()
    {
        STEP("Create test collaboration site to store documents in.");
        publicSite = dataSite.usingAdmin().createPublicRandomSite();

        STEP("Create a test folder within the collaboration site");
        testFolder = dataContent.usingAdmin().usingSite(publicSite).createFolder();

        STEP("Create record categories and record folders");
        recordCategory = createRootCategory(getRandomName("recordCategory"));
        RecordCategoryChild subCategory = createRecordCategory(recordCategory.getId(), getRandomName("subCategory"));
        recordFolder = createFolder(recordCategory.getId(), getRandomName("recordFolder"));
        subcategoryRecordFolder = createFolder(subCategory.getId(), getRandomName("recordFolder"));
        unfiledContainerFolder = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS,
                "Unfiled Folder " + getRandomAlphanumeric(), UNFILED_RECORD_FOLDER_TYPE);
        closedRecordFolder = createFolder(recordCategory.getId(), getRandomName("closedRecordFolder"));
        closeFolder(closedRecordFolder.getId());
        recordFolderWithSpacesInName = createFolder(recordCategory.getId(), RECORD_FOLDER_NAME_WITH_SPACE);

        STEP("Create rm users with different permissions on the record category");
        userFillingPermission = roleService.createCollaboratorWithRMRoleAndPermission(publicSite, recordCategory, ROLE_RM_POWER_USER, PERMISSION_FILING);
        userReadOnlyPermission = roleService.createCollaboratorWithRMRoleAndPermission(publicSite, recordCategory,
                ROLE_RM_POWER_USER, PERMISSION_READ_RECORDS);
    }

    @BeforeMethod(alwaysRun = true)
    public void createDocument()
    {
        STEP("Create a document in the collaboration site");
        testFile = dataContent.usingSite(publicSite)
                                        .usingAdmin()
                                        .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
    }

    /**
     * Given I am calling the "declare as record" action
     * And I am not providing a location parameter value
     * When I execute the action
     * Then the document is declared as a record
     * And is placed in the Unfiled Records location
     */
    @Test
    public void declareAndFileNoLocationUsingActionsAPI() throws Exception
    {
        STEP("Declare document as record without providing a location parameter value using v1 actions api");
        getRestAPIFactory().getActionsAPI(userReadOnlyPermission).declareAsRecord(testFile);

        STEP("Verify the declared record is placed in the Unfiled Records folder");
        assertTrue(isMatchingRecordInUnfiledRecords(testFile), "Record should be filed to Unfiled Records folder");

        STEP("Verify the document in collaboration site is now a record");
        assertTrue(hasRecordAspect(testFile), "File should have record aspect");
    }

    /**
     * Given I am calling the "declare as record" action
     * And I provide a valid record folder in the location parameter
     * When I execute the action
     * Then the document is declared as a record
     * And is filed to the record folder specified
     */
    @Test
    public void declareAndFileToValidLocationUsingActionsAPI() throws Exception
    {
        STEP("Declare document as record with a location parameter value");
        getRestAPIFactory().getActionsAPI(userFillingPermission).declareAndFile(testFile,
                Utility.buildPath(recordCategory.getName(), recordFolder.getName()));

        STEP("Verify the declared record is placed in the record folder");
        assertTrue(isMatchingRecordInRecordFolder(testFile, recordFolder), "Record should be filed to record folder");

        STEP("Verify the document in collaboration site is now a record");
        assertTrue(hasRecordAspect(testFile), "File should have record aspect");
    }

    /**
     * Given I am calling the "declare as record" action
     * And I provide a valid record folder name in the location parameter
     * When I execute the action
     * Then the document is declared as a record
     * And is filed to the record folder specified
     */
    @Test
    public void declareAndFileToValidLocationWithSpacesUsingActionsAPI() throws Exception
    {
        STEP("Declare document as record with an encoded location parameter value");
        getRestAPIFactory().getActionsAPI(userFillingPermission).declareAndFile(testFile,
            Utility.buildPath(recordCategory.getName(), RECORD_FOLDER_NAME_WITH_SPACE));

        STEP("Verify the declared record is placed in the record folder");
        assertTrue(isMatchingRecordInRecordFolder(testFile, recordFolderWithSpacesInName), "Record should be filed to record folder");

        STEP("Verify the document in collaboration site is now a record");
        assertTrue(hasRecordAspect(testFile), "File should have record aspect");
    }

    /**
     * Given I am calling the "declare as record" action
     * And I provide an invalid record folder in the location parameter
     * When I execute the action
     * Then I receive an error indicating that I have attempted to declare and file a document into an invalid record folder
     * And the document is not declared as a record
     */
    @Test (dataProvider = "invalidDestinationPaths")
    public void declareAndFileToInvalidLocationUsingActionsAPI(String containerPath, String expectedException) throws Exception
    {
        STEP("Declare document as record with an invalid location parameter value");
        getRestAPIFactory().getActionsAPI().declareAndFile(testFile, containerPath);
        assertStatusCode(ACCEPTED);

        STEP("Check the exception thrown in alfresco logs");
        dockerHelper.checkExceptionIsInAlfrescoLogs(expectedException);

        STEP("Check that the file is not a record");
        assertFalse(hasRecordAspect(testFile), "File should not have record aspect");
    }

    /**
     * Given I declare a record using the v1 API
     * When I provide a location parameter
     * Then the record is declared in the correct location
     */
    @Test
    public void declareAndFileToValidLocationUsingFilesAPI() throws Exception
    {
        STEP("Declare document as record with a location parameter value");
        Record record = getRestAPIFactory().getFilesAPI(userFillingPermission)
                                           .usingParams(String.format("%s=%s", PARENT_ID_PARAM, recordFolder.getId()))
                                           .declareAsRecord(testFile.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);

        STEP("Verify the declared record is placed in the record folder");
        assertEquals(record.getParentId(), recordFolder.getId(), "Record should be filed to record folder");

        STEP("Verify the document in collaboration site is now a record");
        assertTrue(hasRecordAspect(testFile), "File should have record aspect");
    }

    /**
     * Given I declare a record using the v1 API
     * When I provide an invalid record folder in the location parameter
     * Then I receive an error indicating that I have attempted to declare and file a document into an invalid record folder
     * And the document is not declared as a record
     */
    @Test (dataProvider = "invalidDestinationIds")
    public void declareAndFileToInvalidLocationUsingFilesAPI(String containerID) throws Exception
    {
        STEP("Declare document as record with an invalid location parameter value");
        getRestAPIFactory().getFilesAPI()
                           .usingParams(String.format("%s=%s", PARENT_ID_PARAM, containerID))
                           .declareAsRecord(testFile.getNodeRefWithoutVersion());
        assertStatusCode(BAD_REQUEST);
        getRestAPIFactory().getRmRestWrapper()
                           .assertLastError()
                           .containsSummary("is not valid for this endpoint. Expected nodeType is:{http://www.alfresco.org/model/recordsmanagement/1.0}recordFolder");

        STEP("Check that the file is not a record");
        assertFalse(hasRecordAspect(testFile), "File should not have record aspect");
    }

    /**
     * Given I am an user with read only permissions on a record folder
     * When I declare and file a record to the record folder
     * Then I receive an error indicating that the access is denied
     * And the document is not declared as a record
     */
    @Test
    public void declareAndFileByUserWithReadOnlyPermission() throws Exception
    {
        STEP("Declare document as record with a record folder as location parameter");
        getRestAPIFactory().getFilesAPI(userReadOnlyPermission)
                           .usingParams(String.format("%s=%s", PARENT_ID_PARAM, recordFolder.getId()))
                           .declareAsRecord(testFile.getNodeRefWithoutVersion());
        assertStatusCode(FORBIDDEN);

        STEP("Check that the file is not a record");
        assertFalse(hasRecordAspect(testFile), "File should not have record aspect");
    }

    /**
     * Given I am a non RM user
     * When I declare and file a record to the record folder
     * Then I receive an error indicating that the access is denied
     * And the document is not declared as a record
     */
    @Test
    public void declareAndFileByNonRMUser() throws Exception
    {
        STEP("Create an user with no rm rights");
        UserModel nonRMUser = getDataUser().createRandomTestUser();
        getDataUser().addUserToSite(nonRMUser, publicSite, UserRole.SiteCollaborator);

        STEP("Declare document as record with a record folder as location parameter");
        getRestAPIFactory().getFilesAPI(nonRMUser)
                           .usingParams(String.format("%s=%s", PARENT_ID_PARAM, recordFolder.getId()))
                           .declareAsRecord(testFile.getNodeRefWithoutVersion());
        assertStatusCode(FORBIDDEN);

        STEP("Check that the file is not a record");
        assertFalse(hasRecordAspect(testFile), "File should not have record aspect");
    }

    /**
     * Given I declare a record using the v1 API
     * When I provide a nonexistent record folder in the location parameter
     * Then I receive an error indicating that the record folder does not exist
     * And the document is not declared as a record
     */
    @Test
    public void declareAndFileToNonexistentRecordFolderUsingFilesAPI() throws Exception
    {
        STEP("Declare document as record with a nonexistent location parameter value");
        getRestAPIFactory().getFilesAPI()
                           .usingParams(String.format("%s=%s", PARENT_ID_PARAM, "nonexistent"))
                           .declareAsRecord(testFile.getNodeRefWithoutVersion());
        assertStatusCode(NOT_FOUND);

        STEP("Check that the file is not a record");
        assertFalse(hasRecordAspect(testFile), "File should not have record aspect");
    }

    /**
     * Given I declare a record using the v1 API
     * When I provide a closed record folder in the location parameter
     * Then I receive an error indicating that the record folder is closed
     * And the document is not declared as a record
     */
    @Test
    public void declareAndFileToClosedRecordFolderUsingFilesAPI() throws Exception
    {
        STEP("Declare document as record with a closed location parameter value");
        getRestAPIFactory().getFilesAPI()
                           .usingParams(String.format("%s=%s", PARENT_ID_PARAM, closedRecordFolder.getId()))
                           .declareAsRecord(testFile.getNodeRefWithoutVersion());
        assertStatusCode(UNPROCESSABLE_ENTITY);
        getRestAPIFactory().getRmRestWrapper()
                           .assertLastError()
                           .containsSummary(CLOSED_RECORD_FOLDER_EXC);

        STEP("Check that the file is not a record");
        assertFalse(hasRecordAspect(testFile), "File should not have record aspect");
    }

    /**
     * Given I declare a record using the v1 API
     * When I provide a held record folder in the location parameter
     * Then I receive an error indicating that the record folder is held
     * And the document is not declared as a record
     */
    @Test
    public void declareAndFileToHeldRecordFolderUsingFilesAPI() throws Exception
    {
        RecordCategoryChild heldRecordFolder = createFolder(recordCategory.getId(), getRandomName("heldRecordFolder"));
        holdNodeRef = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(), getAdminUser().getPassword(), HOLD_NAME, HOLD_REASON, HOLD_DESCRIPTION);
        holdsAPI.addItemToHold(getAdminUser().getUsername(), getAdminUser().getPassword(), heldRecordFolder.getId(),
                HOLD_NAME);

        STEP("Declare document as record with a frozen location parameter value");
        getRestAPIFactory().getFilesAPI()
                           .usingParams(String.format("%s=%s", PARENT_ID_PARAM, heldRecordFolder.getId()))
                           .declareAsRecord(testFile.getNodeRefWithoutVersion());
        assertStatusCode(UNPROCESSABLE_ENTITY);

        STEP("Check that the file is not a record");
        assertFalse(hasRecordAspect(testFile), "File should not have record aspect");
    }

    /**
     * Given I declare a record using the v1 API
     * When I provide a location parameter
     * Then the record is declared in the correct location
     * And when I declare it again using a different location
     * Then I get an invalid operation exception
     */
    @Test
    public void declareAndFileTwiceDifferentLocations()
    {
        STEP("Create a document in the collaboration site");
        FileModel testFile = dataContent.usingSite(publicSite).usingAdmin()
                                        .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Declare document as record with a record folder as location parameter");
        getRestAPIFactory().getFilesAPI(userFillingPermission)
                           .usingParams(String.format("%s=%s", PARENT_ID_PARAM, subcategoryRecordFolder.getId()))
                           .declareAsRecord(testFile.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);

        STEP("Declare it again using a different record folder as location parameter");
        getRestAPIFactory().getFilesAPI(userFillingPermission)
                           .usingParams(String.format("%s=%s", PARENT_ID_PARAM, recordFolder.getId()))
                           .declareAsRecord(testFile.getNodeRefWithoutVersion());
        assertStatusCode(UNPROCESSABLE_ENTITY);

        STEP("Verify the declared record is placed in the first record folder");
        assertTrue(isMatchingRecordInRecordFolder(testFile, subcategoryRecordFolder),
                "Record should be filed to recordFolder");
        assertFalse(isMatchingRecordInRecordFolder(testFile, recordFolder),
                "Record should not be filed to subcategoryRecordFolder");
    }

    @AfterClass(alwaysRun = true)
    public void declareAndFileDocumentAsRecordCleanup()
    {
        //delete rm items
        holdsAPI.deleteHold(getAdminUser(), holdNodeRef);
        deleteRecordCategory(recordCategory.getId());
        getRestAPIFactory().getUnfiledRecordFoldersAPI().deleteUnfiledRecordFolder(unfiledContainerFolder.getId());

        //delete created collaboration site
        dataSite.deleteSite(publicSite);

        //delete users
        getDataUser().deleteUser(userFillingPermission);
        getDataUser().deleteUser(userReadOnlyPermission);
    }
}
