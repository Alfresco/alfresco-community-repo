/*-
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
package org.alfresco.rest.rm.community.hold;

import static java.util.Arrays.asList;

import static org.alfresco.rest.rm.community.base.TestData.HOLD_DESCRIPTION;
import static org.alfresco.rest.rm.community.base.TestData.HOLD_REASON;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAspects.FROZEN_ASPECT;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_FILING;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_READ_RECORDS;
import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_MANAGER;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.getFile;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.model.RestNodeAssociationModelCollection;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.hold.Hold;
import org.alfresco.rest.rm.community.model.hold.HoldChild;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.rm.community.requests.gscore.api.FilePlanAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.rm.community.utils.CoreUtil;
import org.alfresco.rest.v0.service.RoleService;
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
 * V1 API tests for removing content/record folder/record from holds
 *
 * @author Damian Ujma
 */
public class RemoveFromHoldsV1Tests extends BaseRMRestTest
{
    private static final String HOLD_ONE = "HOLD_ONE" + generateTestPrefix(RemoveFromHoldsV1Tests.class);
    private static final String HOLD_TWO = "HOLD_TWO" + generateTestPrefix(RemoveFromHoldsV1Tests.class);
    private static final String ACCESS_DENIED_ERROR_MESSAGE = "Access Denied.  You do not have the appropriate " +
        "permissions to perform this operation.";

    private SiteModel testSite;
    private SiteModel privateSite;
    private String holdNodeRefOne;
    private FileModel contentHeld;
    private FileModel contentAddToManyHolds;
    private List<String> holdsListRef = new ArrayList<>();
    private final Set<UserModel> usersToBeClean = new HashSet<>();
    private final Set<String> nodesToBeClean = new HashSet<>();

    @Autowired
    private RoleService roleService;

    @BeforeClass(alwaysRun = true)
    public void preconditionForRemoveContentFromHold()
    {
        STEP("Create two holds.");

        holdNodeRefOne = createHold(FILE_PLAN_ALIAS,
            Hold.builder().name(HOLD_ONE).description(HOLD_DESCRIPTION).reason(HOLD_REASON).build(),
            getAdminUser()).getId();
        String holdNodeRefTwo = createHold(FILE_PLAN_ALIAS,
            Hold.builder().name(HOLD_TWO).description(HOLD_DESCRIPTION).reason(HOLD_REASON).build(),
            getAdminUser()).getId();
        holdsListRef = asList(holdNodeRefOne, holdNodeRefTwo);

        STEP("Create test files.");
        testSite = dataSite.usingAdmin().createPublicRandomSite();
        privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        contentHeld = dataContent.usingAdmin().usingSite(testSite)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        contentAddToManyHolds = dataContent.usingSite(testSite)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Add content to the holds.");
        getRestAPIFactory()
            .getHoldsAPI(getAdminUser())
            .addChildToHold(HoldChild.builder().id(contentHeld.getNodeRefWithoutVersion()).build(), holdNodeRefOne);
        getRestAPIFactory()
            .getHoldsAPI(getAdminUser())
            .addChildToHold(HoldChild.builder().id(contentAddToManyHolds.getNodeRefWithoutVersion()).build(),
                holdNodeRefOne);
        getRestAPIFactory()
            .getHoldsAPI(getAdminUser())
            .addChildToHold(HoldChild.builder().id(contentAddToManyHolds.getNodeRefWithoutVersion()).build(),
                holdNodeRefTwo);
    }

    /**
     * Valid nodes to be removed from hold
     */
    @DataProvider(name = "validNodesToRemoveFromHold")
    public Object[][] getValidNodesToRemoveFromHold()
    {
        //create electronic and nonElectronic record in record folder
        RecordCategoryChild recordFolder = createCategoryFolderInFilePlan();
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        nodesToBeClean.add(recordFolder.getParentId());
        Record electronicRecord = recordFolderAPI.createRecord(createElectronicRecordModel(), recordFolder.getId(),
            getFile
                (IMAGE_FILE));
        assertStatusCode(CREATED);
        Record nonElectronicRecord = recordFolderAPI.createRecord(createNonElectronicRecordModel(),
            recordFolder.getId());
        assertStatusCode(CREATED);

        RecordCategoryChild folderToHeld = createCategoryFolderInFilePlan();
        nodesToBeClean.add(folderToHeld.getParentId());
        Stream.of(electronicRecord.getId(), nonElectronicRecord.getId(), folderToHeld.getId())
            .forEach(id -> getRestAPIFactory()
                .getHoldsAPI(getAdminUser())
                .addChildToHold(HoldChild.builder().id(id).build(), holdNodeRefOne));

        return new String[][]
            {       // record folder
                { folderToHeld.getId() },
                //electronic record
                { electronicRecord.getId() },
                // non electronic record
                { nonElectronicRecord.getId() },
                // document from collaboration site
                { contentHeld.getNodeRefWithoutVersion() },
            };
    }

    /**
     * Given content/record folder/record that is held
     * And the corresponding hold
     * When I use the existing REST API to remove the node from the hold
     * Then the node is removed from the hold
     * And is no longer frozen
     */
    @Test(dataProvider = "validNodesToRemoveFromHold")
    public void removeContentFromHold(String nodeId) throws Exception
    {
        STEP("Remove node from hold");
        getRestAPIFactory()
            .getHoldsAPI(getAdminUser()).deleteHoldChild(holdNodeRefOne, nodeId);

        STEP("Check the node is not held");
        assertFalse(hasAspect(nodeId, FROZEN_ASPECT));

        STEP("Check node is not in any hold");
        RestNodeAssociationModelCollection holdsEntries = getRestAPIFactory()
            .getNodeAPI(CoreUtil.toContentModel(nodeId)).usingParams("where=(assocType='rma:frozenContent')")
            .getParents();
        assertTrue(holdsEntries.getEntries().isEmpty(), "Content held is still added to a hold.");
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
        STEP("Remove content from hold. ");

        getRestAPIFactory().getHoldsAPI(getAdminUser())
            .deleteHoldChild(holdNodeRefOne, contentAddToManyHolds.getNodeRefWithoutVersion());

        STEP("Check the content is held. ");
        assertTrue(hasAspect(contentAddToManyHolds.getNodeRefWithoutVersion(), FROZEN_ASPECT));

        STEP("Check node is in hold HOLD_TWO. ");

        RestNodeAssociationModelCollection holdsEntries = getRestAPIFactory()
            .getNodeAPI(CoreUtil.toContentModel(contentAddToManyHolds.getNodeRefWithoutVersion()))
            .usingParams("where=(assocType='rma:frozenContent')").getParents();
        assertFalse(holdsEntries.getEntries().isEmpty(), "Content held is not held after removing from one hold.");
        assertTrue(holdsEntries.getEntries().stream()
                .anyMatch(restNodeModel -> restNodeModel.getModel().getName().equals(HOLD_TWO)),
            "Content held is not held after removing from one hold.");
    }

    /**
     * Data provider with user without right permission or capability to remove from hold a specific node
     *
     * @return user model and the node ref to be removed from hold
     */
    @DataProvider(name = "userWithoutPermissionForRemoveFromHold")
    public Object[][] getUserWithoutPermissionForAddToHold()
    {
        //create record folder
        RecordCategoryChild recordFolder = createCategoryFolderInFilePlan();
        nodesToBeClean.add(recordFolder.getParentId());
        UserModel user = roleService.createUserWithRMRole(ROLE_RM_MANAGER.roleId);
        getRestAPIFactory().getRMUserAPI().addUserPermission(holdNodeRefOne, user, PERMISSION_FILING);
        //create files that will be removed from hold
        FileModel contentNoHoldPerm = dataContent.usingAdmin().usingSite(testSite)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        FileModel contentNoHoldCap = dataContent.usingAdmin().usingSite(testSite)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        FileModel privateFile = dataContent.usingAdmin().usingSite(privateSite)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        //add files to hold
        asList(recordFolder.getId(), contentNoHoldCap.getNodeRefWithoutVersion(),
            contentNoHoldPerm.getNodeRefWithoutVersion(), privateFile.getNodeRefWithoutVersion())
            .forEach(id -> getRestAPIFactory()
                .getHoldsAPI(getAdminUser())
                .addChildToHold(HoldChild.builder().id(id).build(), holdNodeRefOne));

        return new Object[][]
            {
                // user with read permission on the content, with remove from hold capability and without
                // filling permission on a hold
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
                        holdNodeRefOne, UserRoles.ROLE_RM_POWER_USER, PERMISSION_FILING),
                    contentNoHoldCap.getNodeRefWithoutVersion()
                },
                //user without read permission on RM  record folder
                {
                    user, recordFolder.getId()
                },
                //user without read permission over the content from the private site
                {
                    user, privateFile.getNodeRefWithoutVersion()
                }
            };
    }

    /**
     * Given node on hold in a single hold location
     * And the user does not have sufficient permissions or capabilities to remove the node from the hold
     * When the user tries to remove the node from the hold
     * Then it's unsuccessful
     *
     * @throws Exception
     */
    @Test(dataProvider = "userWithoutPermissionForRemoveFromHold")
    public void removeFromHoldWithUserWithoutPermission(UserModel userModel, String nodeIdToBeRemoved) throws Exception
    {
        STEP("Update the list of users to be deleted after running the tests");
        usersToBeClean.add(userModel);

        STEP("Remove node from hold with user without right permission or capability");
        getRestAPIFactory().getHoldsAPI(userModel).deleteHoldChild(holdNodeRefOne, nodeIdToBeRemoved);

        assertStatusCode(FORBIDDEN);
        getRestAPIFactory().getRmRestWrapper().assertLastError().containsSummary(ACCESS_DENIED_ERROR_MESSAGE);

        STEP("Check node is frozen.");
        assertTrue(hasAspect(nodeIdToBeRemoved, FROZEN_ASPECT));
    }

    /**
     * Data provider with user with right permission or capability to remove from hold a specific node
     *
     * @return user model and the node ref to be removed from hold
     */
    @DataProvider(name = "userWithPermissionForRemoveFromHold")
    public Object[][] getUserWithPermissionForAddToHold()
    {
        //create record folder
        RecordCategoryChild recordFolder = createCategoryFolderInFilePlan();
        nodesToBeClean.add(recordFolder.getParentId());
        UserModel user = roleService.createUserWithRMRoleAndRMNodePermission(ROLE_RM_MANAGER.roleId,
            recordFolder.getId(),
            PERMISSION_READ_RECORDS);
        getRestAPIFactory().getRMUserAPI().addUserPermission(holdNodeRefOne, user, PERMISSION_FILING);
        //create file that will be removed from hold
        FileModel contentPermission = dataContent.usingAdmin().usingSite(testSite)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        //add files to hold
        asList(recordFolder.getId(), contentPermission.getNodeRefWithoutVersion())
            .forEach(id -> getRestAPIFactory()
                .getHoldsAPI(getAdminUser())
                .addChildToHold(HoldChild.builder().id(id).build(), holdNodeRefOne));

        return new Object[][]
            {
                // user with write permission on the content
                {
                    roleService.createUserWithSiteRoleRMRoleAndPermission(testSite, UserRole.SiteConsumer,
                        holdNodeRefOne, UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING),
                    contentPermission.getNodeRefWithoutVersion()
                },
                //user with read permission on RM  record folder
                {
                    user, recordFolder.getId()
                },

            };
    }

    @Test(dataProvider = "userWithPermissionForRemoveFromHold")
    public void removeFromHoldWithUserWithPermission(UserModel userModel, String nodeIdToBeRemoved) throws Exception
    {
        STEP("Update the list of users to be deleted after running the tests");
        usersToBeClean.add(userModel);

        STEP("Remove node from hold with user with right permission and capability");
        getRestAPIFactory().getHoldsAPI(userModel).deleteHoldChild(holdNodeRefOne, nodeIdToBeRemoved);

        STEP("Check node is not frozen.");
        assertFalse(hasAspect(nodeIdToBeRemoved, FROZEN_ASPECT));
    }

    private Hold createHold(String parentId, Hold hold, UserModel user)
    {
        FilePlanAPI filePlanAPI = getRestAPIFactory().getFilePlansAPI(user);
        return filePlanAPI.createHold(hold, parentId);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpRemoveContentFromHold()
    {
        holdsListRef.forEach(holdRef -> getRestAPIFactory().getHoldsAPI(getAdminUser()).deleteHold(holdRef));
        dataSite.usingAdmin().deleteSite(testSite);
        dataSite.usingAdmin().deleteSite(privateSite);
        usersToBeClean.forEach(user -> getDataUser().usingAdmin().deleteUser(user));
        nodesToBeClean.forEach(this::deleteRecordCategory);
    }
}
