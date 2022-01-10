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

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.recordcategory.RetentionPeriodProperty.CUT_OFF_DATE;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_FILING;
import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_MANAGER;
import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_POWER_USER;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicUnfiledContainerChildModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicUnfiledContainerChildModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.getFile;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.v0.BaseAPI.RM_ACTIONS;
import org.alfresco.rest.model.RestNodeBodyMoveCopyModel;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.requests.Node;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.record.RecordBodyFile;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordCategoryAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.service.DispositionScheduleService;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Delete records tests
 *
 * @author Kristijan Conkas
 * @since 2.6
 */
public class DeleteRecordTests extends BaseRMRestTest
{
    @Autowired
    private DispositionScheduleService dispositionScheduleService;
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    @Autowired
    private org.alfresco.rest.v0.RecordsAPI recordsAPI;
    @Autowired
    private RoleService roleService;
    private RecordCategoryChild recordFolder;
    private UnfiledContainerChild unfiledRecordFolder;

    @BeforeClass (alwaysRun = true)
    public void setupDeleteRecordTests()
    {
        testSite = dataSite.usingAdmin().createPublicRandomSite();
        recordFolder = createCategoryFolderInFilePlan();
        unfiledRecordFolder = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, getRandomName("Unfiled Folder "),
                UNFILED_RECORD_FOLDER_TYPE);
    }

    /** Data provider with electronic and non-electronic records to be deleted */
    @DataProvider (name = "recordsToBeDeleted")
    public Object[][] getRecordsToBeDeleted()
    {
        return new String[][]
            {
                // records created in an arbitrary record folder
                {getRestAPIFactory().getRecordFolderAPI().createRecord(createElectronicRecordModel(), recordFolder.getId(), getFile(IMAGE_FILE)).getId()},
                {getRestAPIFactory().getRecordFolderAPI().createRecord(createNonElectronicRecordModel(), recordFolder.getId()).getId()},
                // records created in unfiled records root
                {getRestAPIFactory().getUnfiledContainersAPI().uploadRecord(createElectronicUnfiledContainerChildModel(), UNFILED_RECORDS_CONTAINER_ALIAS, getFile(IMAGE_FILE)).getId()},
                {getRestAPIFactory().getUnfiledContainersAPI().createUnfiledContainerChild(createNonElectronicUnfiledContainerChildModel(), UNFILED_RECORDS_CONTAINER_ALIAS).getId()},
                // records created in an arbitrary unfiled records folder
                {getRestAPIFactory().getUnfiledRecordFoldersAPI().uploadRecord(createElectronicUnfiledContainerChildModel(), unfiledRecordFolder.getId(), getFile(IMAGE_FILE)).getId()},
                {getRestAPIFactory().getUnfiledRecordFoldersAPI().createUnfiledRecordFolderChild(createNonElectronicUnfiledContainerChildModel(), unfiledRecordFolder.getId()).getId()}
            };
    }

    /**
     * <pre>
     * Given an electronic record
     * And that I have the "Delete Record" capability
     * And write permissions
     * When I delete the record
     * Then it is deleted from the file plan
     * </pre>
     */
    @Test
    (
        dataProvider = "recordsToBeDeleted",
        description = "Admin user can delete records"
    )
    @AlfrescoTest(jira="RM-4363")
    public void adminCanDeleteRecords(String recordId)
    {
        // Delete record and verify successful deletion
        deleteAndVerify(recordId);
    }

    /**
     * <pre>
     * Given a non-electronic record
     * And that I don't have write permissions
     * When I try to delete the record
     * Then nothing happens
     * And error gets reported
     * </pre>
     */
    @Test
    (
        description = "User without write permissions can't delete a record"
    )
    @AlfrescoTest(jira="RM-4363")
    public void userWithoutWritePermissionsCantDeleteRecord()
    {
        // Create a non-electronic record in unfiled records
        UnfiledContainerChild nonElectronicRecord = UnfiledContainerChild.builder()
                .name("Record " + RandomData.getRandomAlphanumeric())
                .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                .build();
        UnfiledContainerChild newRecord = getRestAPIFactory().getUnfiledContainersAPI().createUnfiledContainerChild(nonElectronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS);

        assertStatusCode(CREATED);

        // Create test user with RM Manager role
        UserModel deleteUser = roleService.createUserWithRMRole(ROLE_RM_MANAGER.roleId);

        // Try to delete newRecord
        getRestAPIFactory().getRecordsAPI(deleteUser).deleteRecord(newRecord.getId());
        assertStatusCode(FORBIDDEN);
    }

    /**
     * <pre>
     * Given a record
     * And that I don't have the "Delete Record" capability
     * When I try to delete the record
     * Then nothing happens
     * And error gets reported
     * </pre>
     */
    @Test
    (
        description = "User without delete records capability can't delete a record"
    )
    @AlfrescoTest(jira="RM-4363")
    public void userWithoutDeleteRecordsCapabilityCantDeleteRecord()
    {
        // Create test user and add it with collaboration privileges
        // Add RM role to user, RM Power User doesn't have the "Delete Record" capabilities
        UserModel deleteUser = roleService.createUserWithRMRole(ROLE_RM_POWER_USER.roleId);

        // Grant "deleteUser" filing permissions on "randomFolder" parent, this will be inherited to randomFolder
        RecordCategoryAPI recordCategoryAPI = getRestAPIFactory().getRecordCategoryAPI();
        getRestAPIFactory().getRMUserAPI().addUserPermission(recordCategoryAPI.getRecordCategory(recordFolder.getParentId()).getId(), deleteUser, PERMISSION_FILING);
        assertStatusCode(OK);

        // Create a non-electronic record in "randomFolder"
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        Record newRecord = recordFolderAPI.createRecord(createNonElectronicRecordModel(), recordFolder.getId());
        assertStatusCode(CREATED);

        // Verify the user can see "newRecord"
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI(deleteUser);
        recordsAPI.getRecord(newRecord.getId());
        assertStatusCode(OK);

        // Try to delete "newRecord"
        recordsAPI.deleteRecord(newRecord.getId());
        assertStatusCode(FORBIDDEN);
    }

    /**
     * <pre>
     * Given a record
     * And a copy of that record
     * When I delete the copy
     * Then it is still possible to view the content of the original record
     * </pre>
     */
    @Test(description = "Deleting copy of record doesn't delete original content")
    @AlfrescoTest(jira="MNT-18806")
    public void deleteCopyOfRecord()
    {
        STEP("Create another record category with a folder.");
        RecordCategoryChild recordFolderB = createCategoryFolderInFilePlan();

        STEP("Create a record in first folder and copy it into second folder.");
        String recordId = getRestAPIFactory().getRecordFolderAPI()
                    .createRecord(createElectronicRecordModel(), recordFolder.getId(), getFile(IMAGE_FILE)).getId();
        String copyId = copyNode(recordId, recordFolderB.getId()).getId();
        assertStatusCode(CREATED);

        STEP("Check that it's possible to load the original content.");
        getNodeContent(recordId);
        assertStatusCode(OK);

        STEP("Delete the copy.");
        deleteAndVerify(copyId);

        STEP("Check that the original record node and content still exist.");
        checkNodeExists(recordId);
        getNodeContent(recordId);
    }


    /**
     * <pre>
     * Given a file that has a copy
     * And the original file is declared as record
     * When I delete the original
     * Then it is still possible to view the content of the copy
     * </pre>
     */
    @Test (description = "Deleting record doesn't delete the content for the copies")
    @AlfrescoTest (jira = "MNT-20145")
    public void deleteOriginOfRecord()
    {
        STEP("Create a file.");
        FileModel testFile = dataContent.usingAdmin().usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Create a copy of the file.");
        RestNodeModel copyOfTestFile = copyNode(testFile.getNodeRefWithoutVersion(), testSite.getGuid());

        STEP("Declare original file as record");
        getRestAPIFactory().getFilesAPI().declareAsRecord(testFile.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);

        STEP("Delete the record.");
        deleteAndVerify(testFile.getNodeRefWithoutVersion());

        STEP("Check that it's possible to load the copy content.");
        getNodeContent(copyOfTestFile.getId());
        assertStatusCode(OK);
    }

    /**
     * <pre>
     * Given a file that has a copy
     * And the original file is declared as record
     * And the record becomes part of a disposition schedule with a destroy step
     * When the record is destroyed
     * Then it is still possible to view the content of the copy
     * </pre>
     */
    @Test (description = "Destroying record doesn't delete the content for the associated copy")
    @AlfrescoTest (jira = "MNT-20145")
    public void destroyOfRecord()
    {
        STEP("Create a file.");
        FileModel testFile = dataContent.usingAdmin().usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        FolderModel folderModel = dataContent.usingAdmin().usingSite(testSite).createFolder();

        STEP("Create a copy of the file.");
        RestNodeModel copy = copyNode(testFile.getNodeRefWithoutVersion(), folderModel.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);

        STEP("Declare the file as record.");
        getRestAPIFactory().getFilesAPI().declareAsRecord(testFile.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);

        STEP("Create a record category with a disposition schedule.");
        RecordCategory recordCategory = createRootCategory(getRandomName("Category with disposition"));
        dispositionScheduleService.createCategoryRetentionSchedule(recordCategory.getName(), true);

        STEP("Add retention schedule cut off and destroy step with immediate period.");
        dispositionScheduleService.addCutOffImmediatelyStep(recordCategory.getName());
        dispositionScheduleService.addDestroyWithGhostingImmediatelyAfterCutOff(recordCategory.getName());

        STEP("Create a record folder and file the record");
        RecordCategoryChild recFolder = createFolder(recordCategory.getId(), getRandomName("recFolder"));
        RecordBodyFile recordBodyFile = RecordBodyFile.builder().targetParentId(recFolder.getId()).build();
        Record recordFiled = getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, testFile.getNodeRefWithoutVersion());
        getRestAPIFactory().getRecordsAPI().completeRecord(recordFiled.getId());
        assertStatusCode(CREATED);

        STEP("Execute the disposition schedule steps.");
        rmRolesAndActionsAPI.executeAction(getAdminUser().getUsername(), getAdminUser().getUsername(), recordFiled.getName(),
                RM_ACTIONS.CUT_OFF);
        rmRolesAndActionsAPI.executeAction(getAdminUser().getUsername(), getAdminUser().getUsername(), recordFiled.getName(),
                RM_ACTIONS.DESTROY);

        STEP("Check that it's possible to load the copy content.");
        getNodeContent(copy.getId());
        assertStatusCode(OK);
    }

    /**
     * <pre>
     * Given a file that has version declared as record
     * When the record is deleted
     * Then it is still possible to view the content of the file
     * </pre>
     */
    @Test (description = "Deleting record made from version doesn't delete the content for the file")
    @AlfrescoTest (jira = "MNT-20145")
    public void deleteVersionDeclaredAsRecord()
    {
        STEP("Create a file.");
        FileModel testFile = dataContent.usingAdmin().usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Declare file version as record.");
        recordsAPI.declareDocumentVersionAsRecord(getAdminUser().getUsername(), getAdminUser().getPassword(), testSite.getId(),
                testFile.getName());
        UnfiledContainerChild unfiledContainerChild = getRestAPIFactory().getUnfiledContainersAPI()
                                                                       .getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS)
                                                                       .getEntries().stream()
                                                                       .filter(child -> child.getEntry().getName()
                                                                                             .startsWith(testFile.getName().substring(0, testFile.getName().indexOf("."))))
                                                                       .findFirst()
                                                                       .get().getEntry();

        STEP("Delete the record.");
        deleteAndVerify(unfiledContainerChild.getId());

        STEP("Check that it's possible to load the file declared version as record.");
        getNodeContent(testFile.getNodeRefWithoutVersion());
        assertStatusCode(OK);
    }


    /**
     * Utility method to delete a record and verify successful deletion
     *
     * @param recordId The id of the record
     */
    private void deleteAndVerify(String recordId)
    {
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();

        // Delete record and verify status
        recordsAPI.deleteRecord(recordId);
        assertStatusCode(NO_CONTENT);

        // Try to get deleted record
        recordsAPI.getRecord(recordId);
        assertStatusCode(NOT_FOUND);
    }

    /**
     * Copy a node to a folder.
     *
     * @param nodeId The id of the node to copy.
     * @param destinationFolder The id of the folder to copy it to.
     * @return The model returned by the copy API.
     */
    private RestNodeModel copyNode(String nodeId, String destinationFolder)
    {
        Node node = getNode(nodeId);
        RestNodeBodyMoveCopyModel copyBody = new RestNodeBodyMoveCopyModel();
        copyBody.setTargetParentId(destinationFolder);
        try
        {
            return node.copy(copyBody);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Problem copying record.", e);
        }
    }

    /**
     * Get the content from a node.
     *
     * @param nodeId
     * @return The response containing the node content.
     */
    private RestResponse getNodeContent(String nodeId)
    {
        try
        {
            return getNode(nodeId).getNodeContent();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to load content for node.", e);
        }
    }

    /**
     * Check that the given node exists.
     *
     * @param nodeId The node to check.
     */
    private void checkNodeExists(String nodeId)
    {
        try
        {
            getNode(nodeId).getNode();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Node does not exist.", e);
        }
    }

    /**
     * Get the node from a record id.
     *
     * @param recordId The record to get.
     * @return The node object.
     */
    private Node getNode(String recordId)
    {
        RepoTestModel repoTestModel = new RepoTestModel() {};
        repoTestModel.setNodeRef(recordId);
        return getRestAPIFactory().getNodeAPI(repoTestModel);
    }

    @AfterClass(alwaysRun = true)
    public void cleanupDeleteRecordTests()
    {
        dataSite.deleteSite(testSite);
        deleteRecordCategory(recordFolder.getParentId());
        getRestAPIFactory().getUnfiledRecordFoldersAPI().deleteUnfiledRecordFolder(unfiledRecordFolder.getId());
    }
}
