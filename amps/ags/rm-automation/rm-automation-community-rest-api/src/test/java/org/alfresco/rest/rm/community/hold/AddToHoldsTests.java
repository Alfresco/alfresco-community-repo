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
package org.alfresco.rest.rm.community.hold;


import static org.alfresco.rest.rm.community.base.TestData.HOLD_DESCRIPTION;
import static org.alfresco.rest.rm.community.base.TestData.HOLD_REASON;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAspects.FROZEN_ASPECT;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_FILING;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_READ_RECORDS;
import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_MANAGER;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.CoreUtil.toContentModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.getFile;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.apache.commons.httpclient.HttpStatus.SC_BAD_REQUEST;
import static org.apache.commons.httpclient.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.CREATED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.dataprep.ContentActions;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.hold.HoldEntry;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.v0.HoldsAPI;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * API tests for adding content/record folder/records to holds
 *
 * @author Rodica Sutu
 * @since 3.2
 */
@AlfrescoTest (jira = "RM-6874")
public class AddToHoldsTests extends BaseRMRestTest
{
    private static final String HOLD = "HOLD" + generateTestPrefix(AddToHoldsTests.class);
    private static final String ACCESS_DENIED_ERROR_MESSAGE = "Access Denied.  You do not have the appropriate " +
            "permissions to perform this operation.";
    private static final String INVALID_TYPE_ERROR_MESSAGE = "Items added to a hold must be either a record, a " +
            "record folder or active content.";
    private static final String LOCKED_FILE_ERROR_MESSAGE = "Locked content can't be added to a hold.";
    private SiteModel testSite;
    private String holdNodeRef;
    private FileModel documentHeld, contentToAddToHold, contentAddToHoldNoPermission;
    private UserModel userAddHoldPermission;
    private final List<UserModel> users = new ArrayList<>();
    private final List<String> nodesToBeClean = new ArrayList<>();

    @Autowired
    private HoldsAPI holdsAPI;
    @Autowired
    private RoleService roleService;
    @Autowired
    private ContentActions contentActions;

    @BeforeClass (alwaysRun = true)
    public void preconditionForAddContentToHold()
    {
        STEP("Create a hold.");
        holdNodeRef = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(), getAdminUser().getUsername(),
                HOLD, HOLD_REASON, HOLD_DESCRIPTION);

        STEP("Create test files.");
        testSite = dataSite.usingAdmin().createPublicRandomSite();
        documentHeld = dataContent.usingAdmin().usingSite(testSite)
                                  .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        contentToAddToHold = dataContent.usingAdmin().usingSite(testSite)
                                        .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        contentAddToHoldNoPermission = dataContent.usingAdmin().usingSite(testSite)
                                                  .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Add the content to the hold.");
        holdsAPI.addItemToHold(getAdminUser().getUsername(), getAdminUser().getPassword(), documentHeld
                .getNodeRefWithoutVersion(), HOLD);

        STEP("Create users");
        userAddHoldPermission = roleService.createUserWithSiteRoleRMRoleAndPermission(testSite,
                UserRole.SiteCollaborator, holdNodeRef, UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING);
        users.add(userAddHoldPermission);

    }

    /**
     * Given a hold that contains at least one active content
     * When I use the existing REST API to retrieve the contents of the hold
     * Then I should see all the active content on hold
     */
    @Test
    public void retrieveTheContentOfTheHoldUsingV1API() throws Exception
    {
        STEP("Retrieve the list of children from the hold and collect the entries that have the name of the active " +
                "content held");
        List<RestNodeModel> documentsHeld = restClient.authenticateUser(getAdminUser()).withCoreAPI()
                                                      .usingNode(toContentModel(holdNodeRef))
                                                      .listChildren().getEntries().stream()
                                                      .filter(child -> child.onModel().getName().contains(documentHeld
                                                              .getName()))
                                                      .collect(Collectors.toList());
        STEP("Check the list of active content");
        assertEquals(documentsHeld.size(), 1, "The active content is not retrieve when getting the children from the " +
                "hold folder");
        assertEquals(documentsHeld.get(0).onModel().getName(), documentHeld.getName());
    }

    /**
     * Given a hold that contains at least one active content
     * When I use the existing REST API to retrieve the holds the content is added
     * Then the hold where the content held is returned
     */
    @Test
    public void retrieveTheHoldWhereTheContentIsAdded()
    {
        List<HoldEntry> holdEntries = holdsAPI.getHolds(getAdminUser().getUsername(), getAdminUser().getPassword(),
                documentHeld.getNodeRefWithoutVersion(), true, null);
        assertTrue(holdEntries.stream().anyMatch(holdEntry -> holdEntry.getName().contains(HOLD)), "Could not find " +
                "hold with name " + HOLD);
    }

    /**
     * Valid nodes to be added to hold
     */
    @DataProvider (name = "validNodesForAddToHold")
    public Object[][] getValidNodesForAddToHold()
    {
        //create electronic and nonElectronic record in record folder
        RecordCategoryChild recordFolder = createCategoryFolderInFilePlan();
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        nodesToBeClean.add(recordFolder.getParentId());
        Record electronicRecord = recordFolderAPI.createRecord(createElectronicRecordModel(), recordFolder.getId(), getFile
                (IMAGE_FILE));
        assertStatusCode(CREATED);

        Record nonElectronicRecord = recordFolderAPI.createRecord(createNonElectronicRecordModel(), recordFolder.getId());
        assertStatusCode(CREATED);
        getRestAPIFactory().getRMUserAPI().addUserPermission(recordFolder.getId(), userAddHoldPermission,
                PERMISSION_FILING);

        RecordCategoryChild folderToHold = createCategoryFolderInFilePlan();
        getRestAPIFactory().getRMUserAPI().addUserPermission(folderToHold.getId(), userAddHoldPermission,
                PERMISSION_FILING);
        nodesToBeClean.add(folderToHold.getParentId());

        return new String[][]
                {       // record folder
                        { folderToHold.getId() },
                        //electronic record
                        { electronicRecord.getId() },
                        // non electronic record
                        { nonElectronicRecord.getId() },
                        // document from collaboration site
                        { contentToAddToHold.getNodeRefWithoutVersion() },
                };
    }

    /**
     * Given record folder/record/document not on hold
     * And a hold
     * And file permission on the hold
     * And the appropriate capability to add to hold
     * When I use the existing REST API to add the node to the hold
     * Then the record folder/record/document is added to the hold
     * And the item is frozen
     *
     * @throws Exception
     */
    @Test (dataProvider = "validNodesForAddToHold")
    public void addValidNodesToHoldWithAllowedUser(String nodeId) throws Exception
    {
        STEP("Add node to hold with user with permission.");
        holdsAPI.addItemToHold(userAddHoldPermission.getUsername(), userAddHoldPermission.getPassword(), nodeId, HOLD);

        STEP("Check the node is frozen.");
        assertTrue(hasAspect(nodeId, FROZEN_ASPECT));
    }

    /**
     * Data provider with user without correct permission to add to hold and the node ref to be added to hold
     * @return object with user model and the node ref to be added to hold
     */
    @DataProvider (name = "userWithoutPermissionForAddToHold")
    public Object[][] getUserWithoutPermissionForAddToHold()
    {
        //create record folder
        RecordCategoryChild recordFolder = createCategoryFolderInFilePlan();
        //create a rm manager and grant read permission over the record folder created
        UserModel user = roleService.createUserWithRMRoleAndRMNodePermission(ROLE_RM_MANAGER.roleId, recordFolder.getId(),
                PERMISSION_READ_RECORDS);
        getRestAPIFactory().getRMUserAPI().addUserPermission(holdNodeRef, user, PERMISSION_FILING);
        nodesToBeClean.add(recordFolder.getParentId());
        return new Object[][]
                {       // user without write permission on the content
                        {
                                roleService.createUserWithSiteRoleRMRoleAndPermission(testSite, UserRole.SiteConsumer,
                                        holdNodeRef, UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING),
                                contentAddToHoldNoPermission.getNodeRefWithoutVersion()
                        },
                        // user with write permission on the content and without filling permission on a hold
                        {
                                roleService.createUserWithSiteRoleRMRoleAndPermission(testSite, UserRole
                                                .SiteCollaborator,
                                        holdNodeRef, UserRoles.ROLE_RM_MANAGER, PERMISSION_READ_RECORDS),
                                contentAddToHoldNoPermission.getNodeRefWithoutVersion()
                        },
                        // user with write permission on the content, filling permission on a hold without add to
                        // hold capability
                        {
                                roleService.createUserWithSiteRoleRMRoleAndPermission(testSite, UserRole
                                                .SiteCollaborator,
                                        holdNodeRef, UserRoles.ROLE_RM_POWER_USER, PERMISSION_READ_RECORDS),
                                contentAddToHoldNoPermission.getNodeRefWithoutVersion()
                        },
                        //user without write permission on RM  record folder
                        {
                                user, recordFolder.getId()
                        },

                };
    }

    /**
     * Given a node not on hold
     * And a hold
     * And user without right permission to add to hold
     * When I use the existing REST API to add the node to the hold
     * Then the node is not added to the hold
     * And the node is not frozen
     *
     * @throws Exception
     */
    @Test (dataProvider = "userWithoutPermissionForAddToHold")
    public void addContentToHoldWithUserWithoutHoldPermission(UserModel userModel, String nodeToBeAddedToHold) throws Exception
    {
        users.add(userModel);
        STEP("Add the node to the hold with user without permission.");
        String response = holdsAPI.addToHoldAndGetMessage(userModel.getUsername(), userModel.getPassword(),
                SC_INTERNAL_SERVER_ERROR, nodeToBeAddedToHold, holdNodeRef);
        assertTrue(response.contains(ACCESS_DENIED_ERROR_MESSAGE));

        STEP("Check the node is not frozen.");
        assertFalse(hasAspect(nodeToBeAddedToHold,FROZEN_ASPECT));
    }

    /**
     * Data provider with invalid node types that can be added to a hold
     */
    @DataProvider (name = "invalidNodesForAddToHold")
    public Object[][] getInvalidNodesForAddToHold()
    {
        //create locked file
        FileModel contentLocked = dataContent.usingAdmin().usingSite(testSite)
                                            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        contentActions.checkOut(getAdminUser().getUsername(), getAdminUser().getPassword(),
                testSite.getId(), contentLocked.getName());
        RecordCategory category = createRootCategory(getRandomAlphanumeric());
        nodesToBeClean.add(category.getId());
        return new Object[][]
                {       // file plan node id
                        { getFilePlan(FILE_PLAN_ALIAS).getId(), SC_BAD_REQUEST, INVALID_TYPE_ERROR_MESSAGE },
                        //transfer container
                        { getTransferContainer(TRANSFERS_ALIAS).getId(), SC_BAD_REQUEST, INVALID_TYPE_ERROR_MESSAGE },
                        // a record category
                        { category.getId(), SC_BAD_REQUEST, INVALID_TYPE_ERROR_MESSAGE },
                        // unfiled records root
                        { getUnfiledContainer(UNFILED_RECORDS_CONTAINER_ALIAS).getId(), SC_BAD_REQUEST,
                                INVALID_TYPE_ERROR_MESSAGE },
                        // an arbitrary unfiled records folder
                        { createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, "Unfiled Folder " +
                                getRandomAlphanumeric(), UNFILED_RECORD_FOLDER_TYPE).getId(), SC_BAD_REQUEST,
                                INVALID_TYPE_ERROR_MESSAGE },
                        //folder,
                        { dataContent.usingAdmin().usingSite(testSite).createFolder().getNodeRef(), SC_BAD_REQUEST,
                                INVALID_TYPE_ERROR_MESSAGE },
                        //document locked
                        { contentLocked.getNodeRefWithoutVersion(), SC_INTERNAL_SERVER_ERROR, LOCKED_FILE_ERROR_MESSAGE }
                };
    }

    /**
     * Given a node that is not a document/record/ record folder ( a valid node type to be added to hold)
     * And a hold
     * And user without right permission to add to hold
     * When I use the existing REST API to add the node to the hold
     * Then the node is not added to the hold
     * And the node is not frozen
     *
     * @throws Exception
     */
    @Test (dataProvider = "invalidNodesForAddToHold")
    public void addInvalidNodesToHold(String itemNodeRef, int responseCode, String errorMessage) throws Exception
    {
        STEP("Add the node to the hold ");
        String responseErrorMessage = holdsAPI.addToHoldAndGetMessage(getAdminUser().getUsername(),
                getAdminUser().getPassword(), responseCode, itemNodeRef, holdNodeRef);
        assertTrue(responseErrorMessage.contains(errorMessage),
                "Actual error message " + responseErrorMessage + " expected " + errorMessage);

        STEP("Check node is not frozen.");
        assertFalse(hasAspect(itemNodeRef, FROZEN_ASPECT));
    }

    @AfterClass (alwaysRun = true)
    public void cleanUpAddContentToHold()
    {
        holdsAPI.deleteHold(getAdminUser(), holdNodeRef);
        dataSite.usingAdmin().deleteSite(testSite);
        users.forEach(user -> getDataUser().usingAdmin().deleteUser(user));
        nodesToBeClean.forEach( category -> deleteRecordCategory(category));
    }
}
