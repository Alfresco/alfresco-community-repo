/*-
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

import static org.alfresco.rest.rm.community.base.TestData.FROZEN_ASPECT;
import static org.alfresco.rest.rm.community.base.TestData.HOLD_DESCRIPTION;
import static org.alfresco.rest.rm.community.base.TestData.HOLD_REASON;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_FILING;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_READ_RECORDS;
import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_MANAGER;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.CoreUtil.toContentModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.getFile;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.apache.commons.httpclient.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.CREATED;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.hold.HoldEntry;
import org.alfresco.rest.rm.community.model.record.Record;
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
 * API tests for removing content from holds
 *
 * @author Rodica Sutu
 * @since 3.2
 */
@AlfrescoTest (jira = "RM-6874, RM-6873")
public class RemoveContentFromHoldsTests extends BaseRMRestTest
{
    private static final String HOLD_ONE = "HOLD_ONE" + generateTestPrefix(RemoveContentFromHoldsTests.class);
    private static final String HOLD_TWO = "HOLD_TWO" + generateTestPrefix(RemoveContentFromHoldsTests.class);
    private static final String ACCESS_DENIED_ERROR_MESSAGE = "Access Denied.  You do not have the appropriate " +
            "permissions to perform this operation.";

    private SiteModel testSite;
    private String holdNodeRefOne;
    private FileModel contentHeld, contentAddToManyHolds;

    @Autowired
    private HoldsAPI holdsAPI;
    @Autowired
    private RoleService roleService;

    @BeforeClass (alwaysRun = true)
    public void preconditionForRemoveContentFromHold() throws Exception
    {
        STEP("Create two holds.");
        holdNodeRefOne = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(), getAdminUser().getUsername(),
                HOLD_ONE, HOLD_REASON, HOLD_DESCRIPTION);
        String holdNodeRefTwo = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(), getAdminUser()
                        .getUsername(), HOLD_TWO, HOLD_REASON, HOLD_DESCRIPTION);

        STEP("Create test files and add them to hold");
        testSite = dataSite.usingAdmin().createPublicRandomSite();
        contentHeld = dataContent.usingSite(testSite)
                              .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        contentAddToManyHolds = dataContent.usingSite(testSite)
                                                  .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Add the content to the hold.");
        holdsAPI.addItemToHold(getAdminUser().getUsername(), getAdminUser().getPassword(), contentHeld
                .getNodeRefWithoutVersion(), HOLD_ONE);
        holdsAPI.addItemToHold(getAdminUser().getUsername(), getAdminUser().getPassword(), contentAddToManyHolds
                .getNodeRefWithoutVersion(), String.format("%s,%s", HOLD_ONE, HOLD_TWO));

    }

    /**
     * Valid nodes to be removed from hold
     */
    @DataProvider (name = "validNodesToRemoveFromHold")
    public Object[][] getValidNodesToRemoveFromHold() throws Exception
    {
        //create electronic and nonElectronic record in record folder
        String recordFolderId = createCategoryFolderInFilePlan().getId();
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();

        Record electronicRecord = recordFolderAPI.createRecord(createElectronicRecordModel(), recordFolderId, getFile
                (IMAGE_FILE));
        assertStatusCode(CREATED);
        Record nonElectronicRecord = recordFolderAPI.createRecord(createNonElectronicRecordModel(), recordFolderId);
        assertStatusCode(CREATED);

        String folderToHeld = createCategoryFolderInFilePlan().getId();

        Arrays.asList(electronicRecord.getId(), nonElectronicRecord.getId(), folderToHeld).forEach(item ->
                holdsAPI.addItemToHold(getAdminUser().getUsername(), getAdminUser().getPassword(), item, HOLD_ONE));

        return new String[][]
                {       // record folder
                        { folderToHeld },
                        //electronic record
                        { electronicRecord.getId() },
                        // non electronic record
                        { nonElectronicRecord.getId() },
                        // document from collaboration site
                        { contentHeld.getNodeRefWithoutVersion() },
                };
    }

    /**
     * Given active content that is held
     * And the corresponding hold
     * When I use the existing REST API to remove the active content from the hold
     * Then the active content is removed from the hold
     * And is no longer frozen
     */
    @Test(dataProvider = "validNodesToRemoveFromHold")
    public void removeContentFromHold(String nodeId) throws Exception
    {
        STEP("Remove content from hold");
        holdsAPI.removeItemFromHold(getAdminUser().getUsername(), getAdminUser().getPassword(), nodeId, HOLD_ONE);
        STEP("Check the content is not held");
        RestNodeModel heldActiveContent = restClient.authenticateUser(getAdminUser())
                                                    .withCoreAPI().usingNode(toContentModel(nodeId)).getNode();
        assertFalse(heldActiveContent.getAspectNames().contains(FROZEN_ASPECT));

        STEP("Check node is not in any hold");
        List<HoldEntry> holdEntries = holdsAPI.getHolds(getAdminUser().getUsername(), getAdminUser().getPassword(),
                nodeId, true, null);
        assertTrue(holdEntries.isEmpty(), "Content held is still added to a hold.");
    }

    /**
     * Given active content that is held on many holds
     * When I use the existing REST API to remove the active content from one hold
     * Then the active content is removed from the specific hold
     * And is frozen
     * And in the other holds
     */
    @Test
    public void removeContentAddedToManyHolds() throws Exception
    {
        STEP("Remove content from hold");
        holdsAPI.removeItemFromHold(getAdminUser().getUsername(), getAdminUser().getPassword(), contentAddToManyHolds
                .getNodeRefWithoutVersion(), HOLD_ONE);

        STEP("Check the content is held");
        RestNodeModel heldActiveContent = restClient.authenticateUser(getAdminUser())
                                                    .withCoreAPI().usingNode(contentAddToManyHolds).getNode();
        assertTrue(heldActiveContent.getAspectNames().contains(FROZEN_ASPECT));

        STEP("Check node is not in any hold");
        List<HoldEntry> holdEntries = holdsAPI.getHolds(getAdminUser().getUsername(), getAdminUser().getPassword(),
                contentAddToManyHolds.getNodeRefWithoutVersion(), true, null);
        assertFalse(holdEntries.isEmpty(), "Content held is not held after removing from one hold.");
        assertTrue(holdEntries.stream().anyMatch(holdEntry -> holdEntry.getName().contains(HOLD_TWO)), "Content held is " +
                "not held after removing from one hold.");
    }

    /**
     *
     * @return
     * @throws Exception
     */
    @DataProvider (name = "userWithoutPermissionForRemoveFromHold")
    public Object[][] getUserWithoutPermissionForAddToHold() throws Exception
    {
        //create electronic and nonElectronic record in record folder
        String recordFolderId = createCategoryFolderInFilePlan().getId();
        UserModel user = roleService.createUserWithRMRoleAndRMNodePermission(ROLE_RM_MANAGER.roleId, recordFolderId,
                PERMISSION_READ_RECORDS);
        getRestAPIFactory().getRMUserAPI().addUserPermission(holdNodeRefOne, user, PERMISSION_FILING);
        //create files that will be removed from hold
        FileModel contentNoPermission = dataContent.usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        FileModel contentNoHoldPerm = dataContent.usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        FileModel contentNoHoldCap = dataContent.usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        //add files to hold
        Arrays.asList(recordFolderId, contentNoHoldCap.getNodeRefWithoutVersion(),
                contentNoHoldPerm.getNodeRefWithoutVersion(), contentNoPermission.getNodeRefWithoutVersion()).forEach(
                        node -> holdsAPI.addItemToHold(getAdminUser().getUsername(), getAdminUser().getPassword(), node,
                                HOLD_ONE)
                                                                                                                     );

        return new Object[][]
                {
                        // user without write permission on the content
                        {
                                roleService.createUserWithSiteRoleRMRoleAndPermission(testSite, UserRole.SiteConsumer,
                                        holdNodeRefOne, UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING),
                                contentNoPermission.getNodeRefWithoutVersion()
                        },
                        // user with write permission on the content and without filling permission on a hold
                        {
                                roleService.createUserWithSiteRoleRMRoleAndPermission(testSite, UserRole.SiteCollaborator,
                                        holdNodeRefOne, UserRoles.ROLE_RM_MANAGER, PERMISSION_READ_RECORDS),
                                contentNoHoldPerm.getNodeRefWithoutVersion()
                        },
                        // user with write permission on the content, filling permission on a hold without remove from
                        // hold capability
                        {
                                roleService.createUserWithSiteRoleRMRoleAndPermission(testSite, UserRole
                                                .SiteCollaborator,
                                        holdNodeRefOne, UserRoles.ROLE_RM_POWER_USER, PERMISSION_READ_RECORDS),
                                contentNoHoldCap.getNodeRefWithoutVersion()
                        },
                        //user without write permission on RM  record folder
                        {
                                user, recordFolderId
                        },

                };
    }
    /**
     * Given active content on hold in a single hold location
     * And the user does not have sufficient permissions or capabilities to remove the active content from the hold
     * When the user tries to remove the active content from the hold
     * Then they are unsuccessful
     * @throws Exception
     */
    @Test (dataProvider = "userWithoutPermissionForRemoveFromHold")
    public void removeFromHoldWithUserWithoutPermission(UserModel userModel, String nodeIdToBeRemoved) throws Exception
    {
        STEP("Remove content from hold with user without right permission or capability");
        String responseNoHoldPermission = holdsAPI.removeFromHoldAndGetMessage(userModel.getUsername(),
                userModel.getPassword(), SC_INTERNAL_SERVER_ERROR, nodeIdToBeRemoved, HOLD_ONE);
        assertTrue(responseNoHoldPermission.contains(ACCESS_DENIED_ERROR_MESSAGE));

        STEP("Check active content is frozen.");
        RestNodeModel heldActiveContent = restClient.authenticateUser(getAdminUser())
                                                    .withCoreAPI().usingNode(toContentModel(nodeIdToBeRemoved))
                                                    .getNode();
        assertTrue(heldActiveContent.getAspectNames().contains(FROZEN_ASPECT));

    }

    @AfterClass (alwaysRun = true)
    public void cleanUpRemoveContentFromHold() throws Exception
    {
        holdsAPI.deleteHold(getAdminUser().getUsername(), getAdminUser().getPassword(), HOLD_ONE);
        holdsAPI.deleteHold(getAdminUser().getUsername(), getAdminUser().getPassword(), HOLD_TWO);
        dataSite.usingAdmin().deleteSite(testSite);
    }
}
