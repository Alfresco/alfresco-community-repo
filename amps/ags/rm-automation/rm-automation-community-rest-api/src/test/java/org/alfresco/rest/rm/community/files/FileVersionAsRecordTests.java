/*-
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
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_FILING;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_READ_RECORDS;
import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_POWER_USER;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.util.DockerHelper;
import org.alfresco.rest.v0.HoldsAPI;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.Utility;
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
 * API tests for declaring a document version as record and filing to a record folder location within the file plan
 *
 * @author Rodica Sutu
 * @since 3.4
 */
@AlfrescoTest (jira = "APPS-35")
public class FileVersionAsRecordTests extends BaseRMRestTest
{
    private final static String DESTINATION_PATH_NOT_FOUND_EXC = "Unable to execute declare-version-record action, " +
            "because the destination path could not be found.";
    private final static String INVALID_DESTINATION_PATH_EXC = "Unable to execute declare-version-record action, " +
            "because the destination path is invalid.";
    private final static String DESTINATION_PATH_NOT_RECORD_FOLDER_EXC = "Unable to execute declare-version-record " +
            "action, because the destination path is not a record folder.";
    private final static String ACCESS_DENIED_EXC = "Access Denied.  You do not have the appropriate " +
            "permissions to perform this operation.";
      private final static String HOLD_NAME = getRandomName("holdName");

    private UserModel userFillingPermission, userReadOnlyPermission;
    private SiteModel publicSite;
    private FileModel testFile;
    private FolderModel testFolder;
    private RecordCategory recordCategory;
    private RecordCategoryChild recordFolder, closedRecordFolder, heldRecordFolder;
    private UnfiledContainerChild unfiledContainerFolder;
    private String holdNodeRef;

    @Autowired
    private RoleService roleService;
    @Autowired
    private DockerHelper dockerHelper;
    @Autowired
    private HoldsAPI holdsAPI;

    @BeforeClass (alwaysRun = true)
    public void declareAndFileVersionAsRecordSetup()
    {
        STEP("Create test collaboration site to store documents in.");
        publicSite = dataSite.usingAdmin().createPublicRandomSite();

        STEP("Create a test folder within the collaboration site");
        testFolder = dataContent.usingAdmin().usingSite(publicSite).createFolder();

        STEP("Create record categories and record folders");
        recordCategory = createRootCategory(getRandomName("recordCategory"));
        recordFolder = createFolder(recordCategory.getId(), getRandomName("recordFolder"));
        closedRecordFolder = createFolder(recordCategory.getId(), getRandomName("closedRecordFolder"));
        closeFolder(closedRecordFolder.getId());
        unfiledContainerFolder = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS,
                "Unfiled Folder " + getRandomAlphanumeric(), UNFILED_RECORD_FOLDER_TYPE);
        heldRecordFolder = createFolder(recordCategory.getId(), getRandomName("heldRecordFolder"));
        holdNodeRef = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(), getAdminUser().getPassword(), HOLD_NAME, HOLD_REASON, HOLD_DESCRIPTION);
        holdsAPI.addItemToHold(getAdminUser().getUsername(), getAdminUser().getPassword(), heldRecordFolder.getId(),
                HOLD_NAME);

        STEP("Create rm users with different permissions on the record category");
        userFillingPermission = roleService.createCollaboratorWithRMRoleAndPermission(publicSite, recordCategory,
                ROLE_RM_POWER_USER, PERMISSION_FILING);
        userReadOnlyPermission = roleService.createCollaboratorWithRMRoleAndPermission(publicSite, recordCategory,
                ROLE_RM_POWER_USER, PERMISSION_READ_RECORDS);
    }

    @BeforeMethod (alwaysRun = true)
    public void createDocument()
    {
        STEP("Create a document in the collaboration site");
        testFile = dataContent.usingSite(publicSite)
                              .usingAdmin()
                              .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
    }

    /**
     * Given I am calling the "declare version as record" action
     * And I am not providing a location parameter value
     * When I execute the action
     * Then the document is declared as a version record
     * And is placed in the Unfiled Records location
     */
    @Test
    public void declareVersionAndFileNoLocationUsingActionsAPI() throws Exception
    {
        STEP("Declare document version as record without providing a location parameter value using v1 actions api");
        getRestAPIFactory().getActionsAPI(userReadOnlyPermission).declareVersionAsRecord(testFile);

        STEP("Verify the declared version record is placed in the Unfiled Records folder and is a record version");
        assertTrue(isRecordVersionInUnfiledRecords(testFile, "1.0"), "Version record should be filed to Unfiled " +
                "Records folder");
    }

    /**
     * Given I am calling the "declare version as record" action
     * And I provide a valid record folder in the location parameter
     * When I execute the action
     * Then the document is declared as a version record
     * And is filed to the record folder specified
     */
    @Test
    public void fileVersionAsRecordToValidLocationUsingActionsAPI() throws Exception
    {
        STEP("Declare document version as record with a location parameter value");
        getRestAPIFactory().getActionsAPI(userFillingPermission).declareAndFileVersionAsRecord(testFile,
                Utility.buildPath(recordCategory.getName(), recordFolder.getName()));

        STEP("Verify the declared version record is placed in the record folder");
        assertTrue(isRecordVersionInRecordFolder(testFile, recordFolder, "1.0"), "Record version should be filed to " +
                "record folder");
    }

    /**
     * Invalid destination paths where version records can't be filed
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
                        { Utility.buildPath(recordCategory.getName(), closedRecordFolder.getName()),
                                ACCESS_DENIED_EXC },
                         // a frozen record folder
                        { Utility.buildPath(recordCategory.getName(), heldRecordFolder.getName()),
                                ACCESS_DENIED_EXC },
                        // an arbitrary unfiled records folder
                        { "Unfiled Records/" + unfiledContainerFolder.getName(), INVALID_DESTINATION_PATH_EXC },
                        // a collaboration site folder
                        { testFolder.getCmisLocation(), DESTINATION_PATH_NOT_FOUND_EXC }
                };
    }

    /**
     * Given I am calling the "declare version as record" action
     * And I provide an invalid record folder in the path parameter
     * When I execute the action
     * Then I receive an error indicating that I have attempted to file version as record a document into an invalid
     * record folder
     * And the document is not declared as a version record
     */
    @Test (dataProvider = "invalidDestinationPaths")
    public void declareVersionAndFileToInvalidLocationUsingActionsAPI(String containerPath, String expectedException) throws Exception
    {
        STEP("Declare document as record version with an invalid location parameter value");
        getRestAPIFactory().getActionsAPI().declareAndFileVersionAsRecord(testFile, containerPath);
        assertStatusCode(ACCEPTED);

        STEP("Check the exception thrown in alfresco logs");
        dockerHelper.checkExceptionIsInAlfrescoLogs(expectedException);
    }

    /**
     * Given I am an user with read only permissions on a record folder
     * When I declare and file a version record to the record folder
     * Then I receive an error indicating that the access is denied
     * And the document is not declared as a record
     */
    @Test
    public void declareAndFileByUserWithReadOnlyPermission() throws Exception
    {
        STEP("Declare document as record with a record folder as location parameter");
        getRestAPIFactory().getActionsAPI(userReadOnlyPermission).declareAndFileVersionAsRecord(testFile,
                Utility.buildPath(recordCategory.getName(), recordFolder.getName()));

        STEP("Check that the record version is not added to the record folder");
        assertFalse(isRecordVersionInRecordFolder(testFile, recordFolder, "1.0"), "Record version is filed to " +
                "record folder where the user doesn't have filling permission");
    }

    /**
     * Given I am calling the "declare version as record" action for a minor document version
     * And I am not providing a path parameter value
     * When I execute the action
     * Then the document version is declared as a version record
     * And is placed in the Unfiled Records location
     */
    @Test
    public void declareVersionAsRecordMinorVersionUsingActionsAPI() throws Exception
    {
        STEP("Update document in the collaboration site");
        dataContent.usingSite(publicSite).usingAdmin().usingResource(testFile).updateContent("This is the new content" +
               "for " + testFile.getName());

        STEP("Declare document version as record with providing a location parameter value using v1 actions api");
        getRestAPIFactory().getActionsAPI(userFillingPermission).declareAndFileVersionAsRecord(testFile,
                Utility.buildPath(recordCategory.getName(), recordFolder.getName()));

        STEP("Verify the declared version record is placed in the record folder and is a record version");
        assertTrue(isRecordVersionInRecordFolder(testFile, recordFolder, "1.1"), "Record should be filed to fileplan " +
                "location");
    }

    /**
     * Given I am calling the "declare version as record" action for a major document version
     * And I am not providing a path parameter value
     * When I execute the action
     * Then the document version is declared as a version record version
     * And is placed in the Unfiled Records location
     */
    @Test
    public void declareVersionAsRecordMajorVersionUsingActionsAPI() throws Exception
    {
        STEP("Update document in the collaboration site");
        File sampleFile = Utility.getResourceTestDataFile("SampleTextFile_10kb.txt");
        restClient.authenticateUser(getAdminUser()).withCoreAPI().usingParams("majorVersion=true").usingNode(testFile).updateNodeContent(sampleFile);

        STEP("Declare document version as record with providing a location parameter value using v1 actions api");
        getRestAPIFactory().getActionsAPI(userFillingPermission).declareAndFileVersionAsRecord(testFile,
                Utility.buildPath(recordCategory.getName(), recordFolder.getName()));

        STEP("Verify the declared version record is placed in the record folder and is a record version");
        assertTrue(isRecordVersionInRecordFolder(testFile, recordFolder, "2.0"), "Version record should be filed to " +
                "the record folder");
    }

    @AfterClass (alwaysRun = true)
    public void declareAndFileVersionAsRecordCleanUp()
    {
        holdsAPI.deleteHold(getAdminUser(), holdNodeRef);
        deleteRecordCategory(recordCategory.getId());

        //delete created collaboration site
        dataSite.deleteSite(publicSite);

        //delete users
        getDataUser().deleteUser(userFillingPermission);
        getDataUser().deleteUser(userReadOnlyPermission);
    }
}
