/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_FILING;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_READ_RECORDS;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.dataprep.ContentActions;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.hold.Hold;
import org.alfresco.rest.rm.community.model.hold.HoldBulkOperation;
import org.alfresco.rest.rm.community.model.hold.HoldBulkOperation.HoldBulkOperationType;
import org.alfresco.rest.rm.community.model.hold.HoldBulkOperationEntry;
import org.alfresco.rest.rm.community.model.hold.HoldBulkStatus;
import org.alfresco.rest.rm.community.model.hold.HoldBulkStatus.Status;
import org.alfresco.rest.rm.community.model.hold.HoldBulkStatusCollection;
import org.alfresco.rest.rm.community.model.hold.HoldChild;
import org.alfresco.rest.rm.community.model.hold.HoldChildEntry;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.search.RestRequestQueryModel;
import org.alfresco.rest.search.SearchRequest;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * API tests for adding items to holds via the bulk process
 *
 * @since 3.5
 */
public class AddToHoldsBulkV1Tests extends BaseRMRestTest
{
    private static final String ACCESS_DENIED_ERROR_MESSAGE = "Access Denied.  You do not have the appropriate " +
        "permissions to perform this operation.";
    private static final int NUMBER_OF_FILES = 30;
    private final List<FileModel> addedFiles = new ArrayList<>();
    private List<UserModel> users = new ArrayList<>();
    private Hold hold;
    private HoldBulkOperation holdBulkOperation;
    @Autowired
    private RoleService roleService;
    @Autowired
    private ContentActions contentActions;

    @BeforeClass(alwaysRun = true)
    public void preconditionForAddContentToHold()
    {
        STEP("Create a hold.");
        hold = getRestAPIFactory().getFilePlansAPI(getAdminUser()).createHold(Hold.builder().name("HOLD" + generateTestPrefix(AddToHoldsV1Tests.class)).description(HOLD_DESCRIPTION).reason(HOLD_REASON).build(), FILE_PLAN_ALIAS);

        STEP("Create test files.");
        testSite = dataSite.usingAdmin().createPublicRandomSite();

        for(int i = 0; i < NUMBER_OF_FILES; i++)
        {
            FileModel documentHeld = dataContent.usingAdmin().usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
            addedFiles.add(documentHeld);
        }

        RestRequestQueryModel queryReq = getContentFromSiteQuery(testSite.getId());
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQuery(queryReq);

        STEP("Wait until all files are searchable.");
        await().atMost(30, TimeUnit.SECONDS).until(() -> getRestAPIFactory().getSearchAPI(null).search(searchRequest).getPagination()
            .getTotalItems() == NUMBER_OF_FILES);

        holdBulkOperation = HoldBulkOperation.builder()
            .query(queryReq)
            .op(HoldBulkOperationType.ADD).build();

    }

    /**
     * Given a user with the add to hold capability and hold filling permission
     * When the user adds content from a site to a hold using the bulk API
     * Then the content is added to the hold and the status of the bulk operation is DONE
     */
    @Test
    public void addContentFromTestSiteToHoldUsingBulkAPI()
    {
        UserModel userAddHoldPermission = roleService.createUserWithSiteRoleRMRoleAndPermission(testSite,
        UserRole.SiteCollaborator, hold.getId(), UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING);
        users.add(userAddHoldPermission);

        STEP("Add content from the site to the hold using the bulk API.");
        HoldBulkOperationEntry bulkOperationEntry = getRestAPIFactory().getHoldsAPI(userAddHoldPermission).startBulkProcess(holdBulkOperation, hold.getId());

        // Verify the status code
        assertStatusCode(ACCEPTED);
        assertEquals(NUMBER_OF_FILES, bulkOperationEntry.getTotalItems());

        STEP("Wait until all files are added to the hold.");
        await().atMost(20, TimeUnit.SECONDS).until(() -> getRestAPIFactory().getHoldsAPI(getAdminUser()).getChildren(hold.getId()).getEntries().size() == NUMBER_OF_FILES);
        List<String> holdChildrenNodeRefs = getRestAPIFactory().getHoldsAPI(userAddHoldPermission).getChildren(hold.getId()).getEntries().stream().map(HoldChildEntry::getEntry).map(
            HoldChild::getId).toList();
        assertEquals(addedFiles.stream().map(FileModel::getNodeRefWithoutVersion).sorted().toList(), holdChildrenNodeRefs.stream().sorted().toList());

        STEP("Check the bulk status.");
        HoldBulkStatus holdBulkStatus = getRestAPIFactory().getHoldsAPI(userAddHoldPermission).getBulkStatus(hold.getId(), bulkOperationEntry.getBulkStatusId());
        assertSuccessfulBulkStatus(holdBulkStatus, bulkOperationEntry, NUMBER_OF_FILES);

        STEP("Check the bulk statuses.");
        HoldBulkStatusCollection holdBulkStatusCollection =  getRestAPIFactory().getHoldsAPI(userAddHoldPermission).getBulkStatuses(hold.getId());
        assertEquals(1, holdBulkStatusCollection.getEntries().size());
        assertEquals(holdBulkStatus, holdBulkStatusCollection.getEntries().get(0).getEntry());
    }

    /**
     * Given a user without the add to hold capability
     * When the user adds content from a site to a hold using the bulk API
     * Then the user receives access denied error
     */
    @Test
    public void testBulkProcessWithUserWithoutAddToHoldCapability()
    {
        UserModel userWithoutAddToHoldCapability = roleService.createUserWithSiteRoleRMRoleAndPermission(testSite, UserRole
                .SiteCollaborator,
            hold.getId(), UserRoles.ROLE_RM_POWER_USER, PERMISSION_FILING);
        users.add(userWithoutAddToHoldCapability);

        getRestAPIFactory().getHoldsAPI(userWithoutAddToHoldCapability).startBulkProcess(holdBulkOperation, hold.getId());

        // Verify the status code
        assertStatusCode(FORBIDDEN);
        getRestAPIFactory().getRmRestWrapper().assertLastError().containsSummary(ACCESS_DENIED_ERROR_MESSAGE);
    }

    /**
     * Given a user without the filling permission on a hold
     * When the user adds content from a site to a hold using the bulk API
     * Then the user receives access denied error
     */
    @Test
    public void testBulkProcessWithUserWithoutFillingPermissionOnAHold()
    {
        UserModel userWithoutFillingPermissionOnAHold = roleService.createUserWithSiteRoleRMRoleAndPermission(testSite,
            UserRole.SiteCollaborator, hold.getId(), UserRoles.ROLE_RM_MANAGER, PERMISSION_READ_RECORDS);
        users.add(userWithoutFillingPermissionOnAHold);

        getRestAPIFactory().getHoldsAPI(userWithoutFillingPermissionOnAHold).startBulkProcess(holdBulkOperation, hold.getId());

        // Verify the status code
        assertStatusCode(FORBIDDEN);
        getRestAPIFactory().getRmRestWrapper().assertLastError().containsSummary(ACCESS_DENIED_ERROR_MESSAGE);

    }

    /**
     * Given a user without the write permission on all the content
     * When the user adds content from a site to a hold using the bulk API
     * Then all processed items are marked as errors and the last error message contains access denied error
     */
    @Test
    public void testBulkProcessWithUserWithoutWritePermissionOnTheContent()
    {
        UserModel userWithoutWritePermissionOnTheContent = roleService.createUserWithSiteRoleRMRoleAndPermission(testSite, UserRole.SiteConsumer,
            hold.getId(), UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING);
        users.add(userWithoutWritePermissionOnTheContent);

        HoldBulkOperationEntry bulkOperationEntry = getRestAPIFactory().getHoldsAPI(userWithoutWritePermissionOnTheContent).startBulkProcess(holdBulkOperation, hold.getId());

        // Verify the status code
        assertStatusCode(ACCEPTED);

        await().atMost(20, TimeUnit.SECONDS).until(() -> getRestAPIFactory().getHoldsAPI(userWithoutWritePermissionOnTheContent).getBulkStatus(hold.getId(), bulkOperationEntry.getBulkStatusId()).getStatus() == Status.DONE);

        HoldBulkStatus holdBulkStatus = getRestAPIFactory().getHoldsAPI(userWithoutWritePermissionOnTheContent).getBulkStatus(hold.getId(), bulkOperationEntry.getBulkStatusId());
        assertBulkProcessStatusErrors(holdBulkStatus, NUMBER_OF_FILES, NUMBER_OF_FILES, ACCESS_DENIED_ERROR_MESSAGE);
    }

    /**
     * Given a user without the write permission on one file
     * When the user adds content from a site to a hold using the bulk API
     * Then all processed items are added to the hold except the one that the user does not have write permission
     * And the status of the bulk operation is DONE, contains the error message and the number of errors is 1
     */
    @Test
    public void testBulkProcessWithUserWithoutWritePermissionOnOneFile()
    {
        Hold hold2 = getRestAPIFactory().getFilePlansAPI(getAdminUser()).createHold(Hold.builder().name("HOLD" + generateTestPrefix(AddToHoldsV1Tests.class)).description(HOLD_DESCRIPTION).reason(HOLD_REASON).build(), FILE_PLAN_ALIAS);

        UserModel userAddHoldPermission = roleService.createUserWithSiteRoleRMRoleAndPermission(testSite,
            UserRole.SiteCollaborator, hold2.getId(), UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING);
        users.add(userAddHoldPermission);

        contentActions.removePermissionForUser(getAdminUser().getUsername(), getAdminUser().getPassword(), testSite.getId(), addedFiles.get(0).getName(), userAddHoldPermission.getUsername(), UserRole.SiteCollaborator.getRoleId(), false);

        STEP("Add content from the site to the hold using the bulk API.");
        HoldBulkOperationEntry bulkOperationEntry = getRestAPIFactory().getHoldsAPI(userAddHoldPermission).startBulkProcess(holdBulkOperation, hold2.getId());

        // Verify the status code
        assertStatusCode(ACCEPTED);
        assertEquals(NUMBER_OF_FILES, bulkOperationEntry.getTotalItems());

        STEP("Wait until all files are added to the hold.");
        await().atMost(20, TimeUnit.SECONDS).until(() -> getRestAPIFactory().getHoldsAPI(getAdminUser()).getChildren(hold2.getId()).getEntries().size() == NUMBER_OF_FILES - 1);
        List<String> holdChildrenNodeRefs = getRestAPIFactory().getHoldsAPI(userAddHoldPermission).getChildren(hold2.getId()).getEntries().stream().map(HoldChildEntry::getEntry).map(
            HoldChild::getId).toList();
        assertEquals(addedFiles.stream().skip(1).map(FileModel::getNodeRefWithoutVersion).sorted().toList(), holdChildrenNodeRefs.stream().sorted().toList());

        STEP("Check the bulk status.");
        HoldBulkStatus holdBulkStatus = getRestAPIFactory().getHoldsAPI(userAddHoldPermission).getBulkStatus(hold2.getId(), bulkOperationEntry.getBulkStatusId());
        assertBulkProcessStatusErrors(holdBulkStatus, NUMBER_OF_FILES, 1, ACCESS_DENIED_ERROR_MESSAGE);

        STEP("Check the bulk statuses.");
        HoldBulkStatusCollection holdBulkStatusCollection =  getRestAPIFactory().getHoldsAPI(userAddHoldPermission).getBulkStatuses(hold2.getId());
        assertEquals(1, holdBulkStatusCollection.getEntries().size());
        assertEquals(holdBulkStatus, holdBulkStatusCollection.getEntries().get(0).getEntry());

        getRestAPIFactory().getHoldsAPI(getAdminUser()).deleteHold(hold2.getId());
    }


    /**
     * Given an unauthenticated user
     * When the user adds content from a site to a hold using the bulk API
     * Then the user receives unauthorized error
     */
    @Test
    public void testBulkProcessAsUnauthenticatedUser()
    {
        STEP("Start bulk process as unauthenticated user");
        getRestAPIFactory().getHoldsAPI(new UserModel(getAdminUser().getUsername(), "wrongPassword")).startBulkProcess(holdBulkOperation, hold.getId());

        assertStatusCode(UNAUTHORIZED);
    }

    /**
     * Given a user with the add to hold capability and hold filling permission
     * When the user adds content from a site to a hold using the bulk API
     * And the hold does not exist
     * Then the user receives not found error
     */
    @Test
    public void testBulkProcessForNonExistentHold()
    {
        STEP("Start bulk process for non existent hold");
        getRestAPIFactory().getHoldsAPI(getAdminUser()).startBulkProcess(holdBulkOperation, "nonExistentHoldId");

        assertStatusCode(NOT_FOUND);
    }

    /**
     * Given a user with the add to hold capability and hold filling permission
     * When the user adds content from a site to a hold using the bulk API
     * And the hold does not exist
     * Then the user receives not found error
     */
    @Test
    public void getBulkStatusForNonExistentHold()
    {
        STEP("Start bulk process for non existent hold");
        getRestAPIFactory().getHoldsAPI(getAdminUser()).getBulkStatus("nonExistentHoldId", "nonExistenBulkStatusId");

        assertStatusCode(NOT_FOUND);
    }

    /**
     * Given a user with the add to hold capability and hold filling permission
     * When the user adds content from a site to a hold using the bulk API
     * And the bulk status does not exist
     * Then the user receives not found error
     */
    @Test
    public void getBulkStatusForNonExistentBulkStatus()
    {
        STEP("Start bulk process for non bulk status");
        getRestAPIFactory().getHoldsAPI(getAdminUser()).getBulkStatus(hold.getId(), "nonExistenBulkStatusId");

        assertStatusCode(NOT_FOUND);
    }

    /**
     * Given a user with the add to hold capability and hold filling permission
     * When the user adds content from a site to a hold using the bulk API
     * And the hold does not exist
     * Then the user receives not found error
     */
    @Test
    public void getBulkStatusesForNonExistentHold()
    {
        STEP("Start bulk process for non existent hold");
        getRestAPIFactory().getHoldsAPI(getAdminUser()).getBulkStatuses("nonExistentHoldId");

        assertStatusCode(NOT_FOUND);
    }

    private void assertSuccessfulBulkStatus(HoldBulkStatus holdBulkStatus, HoldBulkOperationEntry bulkOperationEntry, long expectedProcessedItems)
    {
        assertEquals(bulkOperationEntry.getBulkStatusId(), holdBulkStatus.getBulkStatusId());
        assertEquals(expectedProcessedItems, holdBulkStatus.getTotalItems());
        assertEquals(expectedProcessedItems, holdBulkStatus.getProcessedItems());
        assertEquals(0L, holdBulkStatus.getErrorsCount());
        assertEquals(Status.DONE, holdBulkStatus.getStatus());
        assertNotNull(holdBulkStatus.getStartTime());
        assertNotNull(holdBulkStatus.getEndTime());
    }

    private void assertBulkProcessStatusErrors(HoldBulkStatus holdBulkStatus, long expectedProcessedItems, int expectedErrorsCount, String expectedErrorMessage) {
        assertEquals(Status.DONE, holdBulkStatus.getStatus());
        assertEquals(expectedErrorsCount, holdBulkStatus.getErrorsCount());
        assertEquals(Status.DONE, holdBulkStatus.getStatus());
        assertNotNull(holdBulkStatus.getStartTime());
        assertNotNull(holdBulkStatus.getEndTime());

        assertTrue(holdBulkStatus.getLastError().contains(expectedErrorMessage));
    }

    private RestRequestQueryModel getContentFromSiteQuery(String siteId)
    {
        RestRequestQueryModel queryReq = new RestRequestQueryModel();
        queryReq.setQuery("SITE:'" + siteId + "' and TYPE:content");
        queryReq.setLanguage("afts");
        return queryReq;
    }

    @AfterClass(alwaysRun = true)
    public void cleanupAddToHoldsBulkV1Tests()
    {
        getRestAPIFactory().getHoldsAPI(getAdminUser()).deleteHold(hold.getId());
        dataSite.usingAdmin().deleteSite(testSite);
        users.forEach(user -> getDataUser().usingAdmin().deleteUser(user));
    }
}
