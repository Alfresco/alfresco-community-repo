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
package org.alfresco.rest.rm.community.hold;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import static org.alfresco.rest.rm.community.base.TestData.HOLD_DESCRIPTION;
import static org.alfresco.rest.rm.community.base.TestData.HOLD_REASON;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_FILING;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_READ_RECORDS;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.dataprep.ContentActions;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.hold.BulkBodyCancel;
import org.alfresco.rest.rm.community.model.hold.Hold;
import org.alfresco.rest.rm.community.model.hold.HoldBulkOperation;
import org.alfresco.rest.rm.community.model.hold.HoldBulkOperation.HoldBulkOperationType;
import org.alfresco.rest.rm.community.model.hold.HoldBulkOperationEntry;
import org.alfresco.rest.rm.community.model.hold.HoldBulkStatus;
import org.alfresco.rest.rm.community.model.hold.HoldBulkStatusCollection;
import org.alfresco.rest.rm.community.model.hold.HoldBulkStatusEntry;
import org.alfresco.rest.rm.community.model.hold.HoldChild;
import org.alfresco.rest.rm.community.model.hold.HoldChildEntry;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.search.RestRequestQueryModel;
import org.alfresco.rest.search.SearchRequest;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.UserModel;

/**
 * API tests for adding items to holds via the bulk process
 */
public class AddToHoldsBulkV1Tests extends BaseRMRestTest
{
    private static final String ACCESS_DENIED_ERROR_MESSAGE = "Access Denied.  You do not have the appropriate " +
            "permissions to perform this operation.";
    private static final int NUMBER_OF_FILES = 5;
    private final List<FileModel> addedFiles = new ArrayList<>();
    private final List<UserModel> users = new ArrayList<>();
    private final List<Hold> holds = new ArrayList<>();
    private Hold hold;
    private Hold hold2;
    private Hold hold3;
    private FolderModel rootFolder;
    private HoldBulkOperation holdBulkOperation;
    @Autowired
    private RoleService roleService;
    @Autowired
    private ContentActions contentActions;

    @BeforeClass(alwaysRun = true)
    public void preconditionForAddContentToHold()
    {
        STEP("Create a hold.");
        hold = getRestAPIFactory().getFilePlansAPI(getAdminUser()).createHold(
                Hold.builder().name("HOLD" + generateTestPrefix(AddToHoldsV1Tests.class)).description(HOLD_DESCRIPTION)
                        .reason(HOLD_REASON).build(),
                FILE_PLAN_ALIAS);
        holds.add(hold);

        STEP("Create test files.");
        testSite = dataSite.usingAdmin().createPublicRandomSite();

        rootFolder = dataContent.usingAdmin().usingSite(testSite).createFolder();
        FolderModel folder1 = dataContent.usingAdmin().usingResource(rootFolder).createFolder();
        FolderModel folder2 = dataContent.usingAdmin().usingResource(folder1).createFolder();

        // Add files to subfolders in the site
        for (int i = 0; i < NUMBER_OF_FILES; i++)
        {
            FileModel documentHeld = dataContent.usingAdmin()
                    .usingResource(i % 2 == 0 ? folder1 : folder2)
                    .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
            addedFiles.add(documentHeld);
        }

        RestRequestQueryModel queryReq = getContentFromSiteQuery(testSite.getId());
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQuery(queryReq);

        STEP("Wait until all files are searchable.");
        await().atMost(30, TimeUnit.SECONDS)
                .until(() -> getRestAPIFactory().getSearchAPI(null).search(searchRequest).getPagination()
                        .getTotalItems() == NUMBER_OF_FILES);

        RestRequestQueryModel ancestorReq = getContentFromFolderAndAllSubfoldersQuery(rootFolder.getNodeRefWithoutVersion());
        SearchRequest ancestorSearchRequest = new SearchRequest();
        ancestorSearchRequest.setQuery(ancestorReq);

        STEP("Wait until paths are indexed.");
        // to improve stability on CI - seems that sometimes during big load we need to wait longer for the condition
        await().atMost(120, TimeUnit.SECONDS)
                .until(() -> getRestAPIFactory().getSearchAPI(null).search(ancestorSearchRequest).getPagination()
                        .getTotalItems() == NUMBER_OF_FILES);

        holdBulkOperation = HoldBulkOperation.builder()
                .query(queryReq)
                .op(HoldBulkOperationType.ADD).build();
    }

    /**
     * Given a user with the add to hold capability and hold filing permission When the user adds content from a site to a hold using the bulk API Then the content is added to the hold and the status of the bulk operation is DONE
     */
    @Test
    public void addContentFromTestSiteToHoldUsingBulkAPI()
    {
        UserModel userAddHoldPermission = roleService.createUserWithSiteRoleRMRoleAndPermission(testSite,
                UserRole.SiteCollaborator, hold.getId(), UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING);
        users.add(userAddHoldPermission);

        STEP("Add content from the site to the hold using the bulk API.");
        HoldBulkOperationEntry bulkOperationEntry = getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                .startBulkProcess(holdBulkOperation, hold.getId());

        // Verify the status code
        assertStatusCode(ACCEPTED);
        assertEquals(NUMBER_OF_FILES, bulkOperationEntry.getTotalItems());

        STEP("Wait until all files are added to the hold.");
        await().atMost(20, TimeUnit.SECONDS).until(
                () -> getRestAPIFactory().getHoldsAPI(getAdminUser()).getChildren(hold.getId()).getEntries().size() == NUMBER_OF_FILES);
        List<String> holdChildrenNodeRefs = getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                .getChildren(hold.getId()).getEntries().stream().map(HoldChildEntry::getEntry).map(
                        HoldChild::getId)
                .toList();
        assertEquals(addedFiles.stream().map(FileModel::getNodeRefWithoutVersion).sorted().toList(),
                holdChildrenNodeRefs.stream().sorted().toList());

        STEP("Check the bulk status.");
        HoldBulkStatus holdBulkStatus = getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                .getBulkStatus(hold.getId(), bulkOperationEntry.getBulkStatusId());
        assertBulkProcessStatus(holdBulkStatus, NUMBER_OF_FILES, 0, null, holdBulkOperation);

        STEP("Check the bulk statuses.");
        HoldBulkStatusCollection holdBulkStatusCollection = getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                .getBulkStatuses(hold.getId());
        assertEquals(Arrays.asList(holdBulkStatus),
                holdBulkStatusCollection.getEntries().stream().map(HoldBulkStatusEntry::getEntry).toList());
    }

    /**
     * Given a user with the add to hold capability and hold filing permission When the user adds content from a folder and all subfolders to a hold using the bulk API Then the content is added to the hold and the status of the bulk operation is DONE
     */
    @Test
    public void addContentFromFolderAndAllSubfoldersToHoldUsingBulkAPI()
    {
        hold3 = getRestAPIFactory().getFilePlansAPI(getAdminUser()).createHold(
                Hold.builder().name("HOLD" + generateTestPrefix(AddToHoldsV1Tests.class)).description(HOLD_DESCRIPTION)
                        .reason(HOLD_REASON).build(),
                FILE_PLAN_ALIAS);
        holds.add(hold3);

        UserModel userAddHoldPermission = roleService.createUserWithSiteRoleRMRoleAndPermission(testSite,
                UserRole.SiteCollaborator, hold3.getId(), UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING);
        users.add(userAddHoldPermission);

        STEP("Add content from the site to the hold using the bulk API.");
        // Get content from folder and all subfolders of the root folder
        HoldBulkOperation bulkOperation = HoldBulkOperation.builder()
                .query(getContentFromFolderAndAllSubfoldersQuery(rootFolder.getNodeRefWithoutVersion()))
                .op(HoldBulkOperationType.ADD).build();
        HoldBulkOperationEntry bulkOperationEntry = getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                .startBulkProcess(bulkOperation, hold3.getId());

        // Verify the status code
        assertStatusCode(ACCEPTED);
        assertEquals(NUMBER_OF_FILES, bulkOperationEntry.getTotalItems());

        STEP("Wait until all files are added to the hold.");
        await().atMost(20, TimeUnit.SECONDS).until(
                () -> getRestAPIFactory().getHoldsAPI(getAdminUser()).getChildren(hold3.getId()).getEntries().size() == NUMBER_OF_FILES);
        List<String> holdChildrenNodeRefs = getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                .getChildren(hold3.getId()).getEntries().stream().map(HoldChildEntry::getEntry).map(
                        HoldChild::getId)
                .toList();
        assertEquals(addedFiles.stream().map(FileModel::getNodeRefWithoutVersion).sorted().toList(),
                holdChildrenNodeRefs.stream().sorted().toList());

        STEP("Check the bulk status.");
        HoldBulkStatus holdBulkStatus = getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                .getBulkStatus(hold3.getId(), bulkOperationEntry.getBulkStatusId());
        assertBulkProcessStatus(holdBulkStatus, NUMBER_OF_FILES, 0, null, bulkOperation);

        STEP("Check the bulk statuses.");
        HoldBulkStatusCollection holdBulkStatusCollection = getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                .getBulkStatuses(hold3.getId());
        assertEquals(List.of(holdBulkStatus),
                holdBulkStatusCollection.getEntries().stream().map(HoldBulkStatusEntry::getEntry).toList());
    }

    /**
     * Given a user without the add to hold capability When the user adds content from a site to a hold using the bulk API Then the user receives access denied error
     */
    @Test
    public void testBulkProcessWithUserWithoutAddToHoldCapability()
    {
        UserModel userWithoutAddToHoldCapability = roleService.createUserWithSiteRoleRMRoleAndPermission(testSite,
                UserRole.SiteCollaborator,
                hold.getId(), UserRoles.ROLE_RM_POWER_USER, PERMISSION_FILING);
        users.add(userWithoutAddToHoldCapability);

        STEP("Add content from the site to the hold using the bulk API.");
        getRestAPIFactory().getHoldsAPI(userWithoutAddToHoldCapability)
                .startBulkProcess(holdBulkOperation, hold.getId());

        STEP("Verify the response status code and the error message.");
        assertStatusCode(FORBIDDEN);
        getRestAPIFactory().getRmRestWrapper().assertLastError().containsSummary(ACCESS_DENIED_ERROR_MESSAGE);
    }

    /**
     * Given a user without the filing permission on a hold When the user adds content from a site to a hold using the bulk API Then the user receives access denied error
     */
    @Test
    public void testBulkProcessWithUserWithoutFilingPermissionOnAHold()
    {
        // User without filing permission on a hold
        UserModel userWithoutPermission = roleService.createUserWithSiteRoleRMRoleAndPermission(testSite,
                UserRole.SiteCollaborator, hold.getId(), UserRoles.ROLE_RM_MANAGER, PERMISSION_READ_RECORDS);
        users.add(userWithoutPermission);

        STEP("Add content from the site to the hold using the bulk API.");
        getRestAPIFactory().getHoldsAPI(userWithoutPermission)
                .startBulkProcess(holdBulkOperation, hold.getId());

        STEP("Verify the response status code and the error message.");
        assertStatusCode(FORBIDDEN);
        getRestAPIFactory().getRmRestWrapper().assertLastError().containsSummary(ACCESS_DENIED_ERROR_MESSAGE);

    }

    /**
     * Given a user without the write permission on all the content When the user adds content from a site to a hold using the bulk API Then all processed items are marked as errors and the last error message contains access denied error
     */
    @Test
    public void testBulkProcessWithUserWithoutWritePermissionOnTheContent()
    {
        // User without write permission on the content
        UserModel userWithoutPermission = roleService.createUserWithSiteRoleRMRoleAndPermission(
                testSite, UserRole.SiteConsumer,
                hold.getId(), UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING);
        users.add(userWithoutPermission);

        // Wait until permissions are reverted
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQuery(holdBulkOperation.getQuery());
        await().atMost(30, TimeUnit.SECONDS)
                .until(() -> getRestAPIFactory().getSearchAPI(userWithoutPermission).search(searchRequest).getPagination()
                        .getTotalItems() == NUMBER_OF_FILES);

        STEP("Add content from the site to the hold using the bulk API.");
        HoldBulkOperationEntry bulkOperationEntry = getRestAPIFactory().getHoldsAPI(
                userWithoutPermission).startBulkProcess(holdBulkOperation, hold.getId());

        STEP("Verify the response.");
        assertStatusCode(ACCEPTED);

        await().atMost(20, TimeUnit.SECONDS).until(() -> Objects.equals(getRestAPIFactory().getHoldsAPI(userWithoutPermission)
                .getBulkStatus(hold.getId(), bulkOperationEntry.getBulkStatusId()).getStatus(), "DONE"));

        HoldBulkStatus holdBulkStatus = getRestAPIFactory().getHoldsAPI(userWithoutPermission)
                .getBulkStatus(hold.getId(), bulkOperationEntry.getBulkStatusId());
        assertBulkProcessStatus(holdBulkStatus, NUMBER_OF_FILES, NUMBER_OF_FILES, ACCESS_DENIED_ERROR_MESSAGE,
                holdBulkOperation);
    }

    /**
     * Given a user without the write permission on one file When the user adds content from a site to a hold using the bulk API Then all processed items are added to the hold except the one that the user does not have write permission And the status of the bulk operation is DONE, contains the error message and the number of errors is 1
     */
    @Test
    public void testBulkProcessWithUserWithoutWritePermissionOnOneFile()
    {
        hold2 = getRestAPIFactory().getFilePlansAPI(getAdminUser()).createHold(
                Hold.builder().name("HOLD" + generateTestPrefix(AddToHoldsV1Tests.class)).description(HOLD_DESCRIPTION)
                        .reason(HOLD_REASON).build(),
                FILE_PLAN_ALIAS);
        holds.add(hold2);

        UserModel userAddHoldPermission = roleService.createUserWithSiteRoleRMRoleAndPermission(testSite,
                UserRole.SiteCollaborator, hold2.getId(), UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING);
        users.add(userAddHoldPermission);

        contentActions.setPermissionForUser(getAdminUser().getUsername(), getAdminUser().getPassword(),
                testSite.getId(), addedFiles.get(0).getName(), userAddHoldPermission.getUsername(),
                UserRole.SiteConsumer.getRoleId(), false);

        STEP("Add content from the site to the hold using the bulk API.");
        HoldBulkOperationEntry bulkOperationEntry = getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                .startBulkProcess(holdBulkOperation, hold2.getId());

        // Verify the status code
        assertStatusCode(ACCEPTED);
        assertEquals(NUMBER_OF_FILES, bulkOperationEntry.getTotalItems());

        STEP("Wait until all files are added to the hold.");
        await().atMost(30, TimeUnit.SECONDS).until(
                () -> getRestAPIFactory().getHoldsAPI(getAdminUser()).getChildren(hold2.getId()).getEntries().size() == NUMBER_OF_FILES - 1);
        await().atMost(30, TimeUnit.SECONDS).until(
                () -> getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                        .getBulkStatus(hold2.getId(), bulkOperationEntry.getBulkStatusId()).getProcessedItems() == NUMBER_OF_FILES);
        List<String> holdChildrenNodeRefs = getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                .getChildren(hold2.getId()).getEntries().stream().map(HoldChildEntry::getEntry).map(
                        HoldChild::getId)
                .toList();
        assertEquals(addedFiles.stream().skip(1).map(FileModel::getNodeRefWithoutVersion).sorted().toList(),
                holdChildrenNodeRefs.stream().sorted().toList());

        STEP("Check the bulk status.");
        HoldBulkStatus holdBulkStatus = getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                .getBulkStatus(hold2.getId(), bulkOperationEntry.getBulkStatusId());
        assertBulkProcessStatus(holdBulkStatus, NUMBER_OF_FILES, 1, ACCESS_DENIED_ERROR_MESSAGE, holdBulkOperation);

        STEP("Check the bulk statuses.");
        HoldBulkStatusCollection holdBulkStatusCollection = getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                .getBulkStatuses(hold2.getId());
        assertEquals(List.of(holdBulkStatus),
                holdBulkStatusCollection.getEntries().stream().map(HoldBulkStatusEntry::getEntry).toList());

        // Revert the permissions
        contentActions.setPermissionForUser(getAdminUser().getUsername(), getAdminUser().getPassword(),
                testSite.getId(), addedFiles.get(0).getName(), userAddHoldPermission.getUsername(),
                UserRole.SiteCollaborator.getRoleId(), true);
    }

    /**
     * Given an unauthenticated user When the user adds content from a site to a hold using the bulk API Then the user receives unauthorized error
     */
    @Test
    public void testBulkProcessAsUnauthenticatedUser()
    {
        STEP("Start bulk process as unauthenticated user");
        getRestAPIFactory().getHoldsAPI(new UserModel(getAdminUser().getUsername(), "wrongPassword"))
                .startBulkProcess(holdBulkOperation, hold.getId());

        STEP("Verify the response status code.");
        assertStatusCode(UNAUTHORIZED);
    }

    /**
     * Given a user with the add to hold capability and hold filing permission When the user adds content from a site to a hold using the bulk API And the hold does not exist Then the user receives not found error
     */
    @Test
    public void testBulkProcessForNonExistentHold()
    {
        STEP("Start bulk process for non existent hold");
        getRestAPIFactory().getHoldsAPI(getAdminUser()).startBulkProcess(holdBulkOperation, "nonExistentHoldId");

        STEP("Verify the response status code.");
        assertStatusCode(NOT_FOUND);
    }

    /**
     * Given a user with the add to hold capability and hold filing permission When the user adds content from a site to a hold using the bulk API and the bulk operation is invalid Then the user receives bad request error
     */
    @Test
    public void testGetBulkStatusesForInvalidOperation()
    {
        STEP("Start bulk process for non existent hold");

        HoldBulkOperation invalidHoldBulkOperation = HoldBulkOperation.builder().op(null)
                .query(holdBulkOperation.getQuery()).build();
        getRestAPIFactory().getHoldsAPI(getAdminUser()).startBulkProcess(invalidHoldBulkOperation, hold.getId());

        STEP("Verify the response status code.");
        assertStatusCode(BAD_REQUEST);
    }

    /**
     * Given a user with the add to hold capability and hold filing permission When the user adds content from a site to a hold using the bulk API And the hold does not exist Then the user receives not found error
     */
    @Test
    public void testGetBulkStatusForNonExistentHold()
    {
        STEP("Start bulk process for non existent hold");
        getRestAPIFactory().getHoldsAPI(getAdminUser()).getBulkStatus("nonExistentHoldId", "nonExistenBulkStatusId");

        STEP("Verify the response status code.");
        assertStatusCode(NOT_FOUND);
    }

    /**
     * Given a user with the add to hold capability and hold filing permission When the user adds content from a site to a hold using the bulk API And the bulk status does not exist Then the user receives not found error
     */
    @Test
    public void testGetBulkStatusForNonExistentBulkStatus()
    {
        STEP("Start bulk process for non bulk status");
        getRestAPIFactory().getHoldsAPI(getAdminUser()).getBulkStatus(hold.getId(), "nonExistenBulkStatusId");

        STEP("Verify the response status code.");
        assertStatusCode(NOT_FOUND);
    }

    /**
     * Given a user with the add to hold capability and hold filing permission When the user adds content from a site to a hold using the bulk API And the hold does not exist Then the user receives not found error
     */
    @Test
    public void testGetBulkStatusesForNonExistentHold()
    {
        STEP("Start bulk process for non existent hold");
        getRestAPIFactory().getHoldsAPI(getAdminUser()).getBulkStatuses("nonExistentHoldId");

        STEP("Verify the response status code.");
        assertStatusCode(NOT_FOUND);
    }

    /**
     * Given a user with the add to hold capability and hold filing permission When the user adds content from all sites to a hold using the bulk API to exceed the limit (30 items) Then the user receives bad request error
     */
    @Test
    public void testExceedingBulkOperationLimit()
    {
        RestRequestQueryModel queryReq = new RestRequestQueryModel();
        queryReq.setQuery("TYPE:content");
        queryReq.setLanguage("afts");

        HoldBulkOperation exceedLimitOp = HoldBulkOperation.builder()
                .query(queryReq)
                .op(HoldBulkOperationType.ADD).build();

        STEP("Start bulk process to exceed the limit");
        getRestAPIFactory().getHoldsAPI(getAdminUser()).startBulkProcess(exceedLimitOp, hold.getId());

        STEP("Verify the response status code.");
        assertStatusCode(BAD_REQUEST);
    }

    /**
     * Given a user with the add to hold capability and hold filing permission When the user adds content from a site to a hold using the bulk API And then the user cancels the bulk operation Then the user receives OK status code
     */
    @Test
    public void testBulkProcessCancellationWithAllowedUser()
    {
        Hold hold4 = getRestAPIFactory().getFilePlansAPI(getAdminUser()).createHold(
                Hold.builder().name("HOLD" + generateTestPrefix(AddToHoldsV1Tests.class)).description(HOLD_DESCRIPTION)
                        .reason(HOLD_REASON).build(),
                FILE_PLAN_ALIAS);
        holds.add(hold4);

        UserModel userAddHoldPermission = roleService.createUserWithSiteRoleRMRoleAndPermission(testSite,
                UserRole.SiteCollaborator, hold4.getId(), UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING);
        users.add(userAddHoldPermission);

        STEP("Add content from the site to the hold using the bulk API.");
        HoldBulkOperationEntry bulkOperationEntry = getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                .startBulkProcess(holdBulkOperation, hold4.getId());

        // Verify the status code
        assertStatusCode(ACCEPTED);
        assertEquals(NUMBER_OF_FILES, bulkOperationEntry.getTotalItems());

        STEP("Cancel the bulk operation.");
        getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                .cancelBulkOperation(hold4.getId(), bulkOperationEntry.getBulkStatusId(), new BulkBodyCancel());

        // Verify the status code
        assertStatusCode(OK);
    }

    /**
     * Given a user with the add to hold capability and hold filing permission When the user adds content from a site to a hold using the bulk API And a 2nd user without the add to hold capability cancels the bulk operation Then the 2nd user receives access denied error
     */
    @Test
    public void testBulkProcessCancellationWithUserWithoutAddToHoldCapability()
    {
        Hold hold5 = getRestAPIFactory().getFilePlansAPI(getAdminUser()).createHold(
                Hold.builder().name("HOLD" + generateTestPrefix(AddToHoldsV1Tests.class)).description(HOLD_DESCRIPTION)
                        .reason(HOLD_REASON).build(),
                FILE_PLAN_ALIAS);
        holds.add(hold5);

        UserModel userAddHoldPermission = roleService.createUserWithSiteRoleRMRoleAndPermission(testSite,
                UserRole.SiteCollaborator, hold5.getId(), UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING);
        users.add(userAddHoldPermission);

        STEP("Add content from the site to the hold using the bulk API.");
        HoldBulkOperationEntry bulkOperationEntry = getRestAPIFactory().getHoldsAPI(userAddHoldPermission)
                .startBulkProcess(holdBulkOperation, hold5.getId());

        // Verify the status code
        assertStatusCode(ACCEPTED);
        assertEquals(NUMBER_OF_FILES, bulkOperationEntry.getTotalItems());

        UserModel userWithoutAddToHoldCapability = roleService.createUserWithSiteRoleRMRoleAndPermission(testSite,
                UserRole.SiteCollaborator,
                hold5.getId(), UserRoles.ROLE_RM_POWER_USER, PERMISSION_FILING);
        users.add(userWithoutAddToHoldCapability);

        STEP("Cancel the bulk operation.");
        getRestAPIFactory().getHoldsAPI(userWithoutAddToHoldCapability)
                .cancelBulkOperation(hold5.getId(), bulkOperationEntry.getBulkStatusId(), new BulkBodyCancel());

        STEP("Verify the response status code and the error message.");
        assertStatusCode(FORBIDDEN);
        getRestAPIFactory().getRmRestWrapper().assertLastError().containsSummary(ACCESS_DENIED_ERROR_MESSAGE);
    }

    private void assertBulkProcessStatus(HoldBulkStatus holdBulkStatus, long expectedProcessedItems,
            int expectedErrorsCount, String expectedErrorMessage, HoldBulkOperation holdBulkOperation)
    {
        assertEquals("DONE", holdBulkStatus.getStatus());
        assertEquals(expectedProcessedItems, holdBulkStatus.getTotalItems());
        assertEquals(expectedProcessedItems, holdBulkStatus.getProcessedItems());
        assertEquals(expectedErrorsCount, holdBulkStatus.getErrorsCount());
        assertEquals(holdBulkStatus.getHoldBulkOperation(), holdBulkOperation);
        assertNotNull(holdBulkStatus.getStartTime());
        assertNotNull(holdBulkStatus.getEndTime());

        if (expectedErrorMessage != null)
        {
            assertTrue(holdBulkStatus.getLastError().contains(expectedErrorMessage));
        }
    }

    private RestRequestQueryModel getContentFromSiteQuery(String siteId)
    {
        RestRequestQueryModel queryReq = new RestRequestQueryModel();
        queryReq.setQuery("SITE:\"" + siteId + "\" and TYPE:content");
        queryReq.setLanguage("afts");
        return queryReq;
    }

    private RestRequestQueryModel getContentFromFolderAndAllSubfoldersQuery(String folderId)
    {
        RestRequestQueryModel queryReq = new RestRequestQueryModel();
        queryReq.setQuery("ANCESTOR:\"workspace://SpacesStore/" + folderId + "\" and TYPE:content");
        queryReq.setLanguage("afts");
        return queryReq;
    }

    @AfterClass(alwaysRun = true)
    public void cleanupAddToHoldsBulkV1Tests()
    {
        dataSite.usingAdmin().deleteSite(testSite);
        users.forEach(user -> getDataUser().usingAdmin().deleteUser(user));
        holds.forEach(hold -> getRestAPIFactory().getHoldsAPI(getAdminUser()).deleteHold(hold.getId()));
    }
}
