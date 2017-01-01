/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.fileplancomponents;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicRecordModel;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.user.UserPermissions;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.rm.community.requests.igCoreAPI.FilePlanComponentAPI;
import org.alfresco.rest.rm.community.requests.igCoreAPI.RMUserAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * Create/File electronic records tests
 * <br>
 * These tests only test the creation and filing of electronic records, update at
 * present isn't implemented in the API under test.
 * <p>
 * @author Kristijan Conkas
 * @since 2.6
 */
public class DeleteRecordTests extends BaseRMRestTest
{
    @Autowired
    private RMUserAPI rmUserAPI;

    /**
     * <pre>
     * Given a record
     * And that I have the "Delete Record" capability
     * And write permissions
     * When I delete the record
     * Then it is deleted from the file plan
     * </pre>
     *
     * @param container
     * @throws Exception
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Admin user can delete an electronic record"
    )
    @AlfrescoTest(jira="RM-4363")
    public void adminCanDeleteElectronicRecord(FilePlanComponent container) throws Exception
    {
        FilePlanComponent newRecord = getRestAPIFactory().getFilePlanComponentsAPI().createElectronicRecord(createElectronicRecordModel(), IMAGE_FILE, container.getId());

        assertStatusCode(CREATED);

        deleteAndVerify(newRecord);
    }

    /**
     * <pre>
     * Given a record
     * And that I have the "Delete Record" capability
     * And write permissions
     * When I delete the record
     * Then it is deleted from the file plan
     * </pre>
     *
     * @param container
     * @throws Exception
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Admin user can delete a non-electronic record"
    )
    @AlfrescoTest(jira="RM-4363")
    public void adminCanDeleteNonElectronicRecord(FilePlanComponent container) throws Exception
    {
        // create a non-electronic record
        FilePlanComponent newRecord = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(createNonElectronicRecordModel(), container.getId());

        assertStatusCode(CREATED);

        deleteAndVerify(newRecord);
    }

    /**
     * <pre>
     * Given a record
     * And that I don't have write permissions
     * When I try to delete the record
     * Then nothing happens
     * And error gets reported
     * </pre>
     *
     * @param container
     * @throws Exception
     */
    @Test
    (
        description = "User without write permissions can't delete a record"
    )
    @AlfrescoTest(jira="RM-4363")
    public void userWithoutWritePermissionsCantDeleteRecord() throws Exception
    {
        // create a non-electronic record in unfiled records
        FilePlanComponent newRecord = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(createNonElectronicRecordModel(), UNFILED_RECORDS_CONTAINER_ALIAS);

        assertStatusCode(CREATED);

        // create test user and add it with collab. privileges
        UserModel deleteUser = getDataUser().createRandomTestUser("delnoperm");
        deleteUser.setUserRole(UserRole.SiteCollaborator);
        logger.info("test user: " + deleteUser.getUsername());
        getDataUser().addUserToSite(deleteUser, new SiteModel(getRestAPIFactory().getRMSiteAPI().getSite().getId()), UserRole.SiteCollaborator);

        // add RM role to user
        rmUserAPI.assignRoleToUser(deleteUser.getUsername(), UserRoles.ROLE_RM_POWER_USER);
        rmUserAPI.usingRestWrapper().assertStatusCodeIs(OK);

        // try to delete newRecord
        getRestAPIFactory().getFilePlanComponentsAPI(deleteUser).deleteFilePlanComponent(newRecord.getId());
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
     *
     * @param container
     * @throws Exception
     */
    @Test
    (
        description = "User without delete records capability can't delete a record"
    )
    @AlfrescoTest(jira="RM-4363")
    public void userWithoutDeleteRecordsCapabilityCantDeleteRecord() throws Exception
    {
        // create test user and add it with collab. privileges
        UserModel deleteUser = getDataUser().createRandomTestUser("delnoperm");
        deleteUser.setUserRole(UserRole.SiteCollaborator);
        getDataUser().addUserToSite(deleteUser, new SiteModel(getRestAPIFactory().getRMSiteAPI().getSite().getId()), UserRole.SiteCollaborator);
        logger.info("test user: " + deleteUser.getUsername());

        // add RM role to user, RM Power User doesn't have the Delete Record capabilities
        rmUserAPI.assignRoleToUser(deleteUser.getUsername(), UserRoles.ROLE_RM_POWER_USER);
        rmUserAPI.usingRestWrapper().assertStatusCodeIs(OK);

        // create random folder
        FilePlanComponent randomFolder = createCategoryFolderInFilePlan();
        logger.info("random folder:" + randomFolder.getName());

        // grant deleteUser Filing privileges on randomFolder category, this will be
        // inherited to randomFolder
        FilePlanComponentAPI filePlanComponentsAPIAsAdmin = getRestAPIFactory().getFilePlanComponentsAPI();
        rmUserAPI.addUserPermission(filePlanComponentsAPIAsAdmin.getFilePlanComponent(randomFolder.getParentId()),
            deleteUser, UserPermissions.PERMISSION_FILING);
        rmUserAPI.usingRestWrapper().assertStatusCodeIs(OK);

        // create a non-electronic record in randomFolder
        FilePlanComponent newRecord = filePlanComponentsAPIAsAdmin.createFilePlanComponent(createNonElectronicRecordModel(), randomFolder.getId());
        assertStatusCode(CREATED);

        // verify the user can see the newRecord
        FilePlanComponentAPI filePlanComponentsAPIAsUser = getRestAPIFactory().getFilePlanComponentsAPI(deleteUser);
        filePlanComponentsAPIAsUser.getFilePlanComponent(newRecord.getId());
        assertStatusCode(OK);

        // try to delete newRecord
        filePlanComponentsAPIAsUser.deleteFilePlanComponent(newRecord.getId());
        assertStatusCode(FORBIDDEN);
    }

    /**
     * Utility method to delete a record and verify successful deletion
     * @param record
     * @throws Exception
     */
    private void deleteAndVerify(FilePlanComponent record) throws Exception
    {
        FilePlanComponentAPI filePlanComponentsAPI = getRestAPIFactory().getFilePlanComponentsAPI();

        // delete it and verify status
        filePlanComponentsAPI.deleteFilePlanComponent(record.getId());
        assertStatusCode(NO_CONTENT);

        // try to get deleted file plan component
        filePlanComponentsAPI.getFilePlanComponent(record.getId());
        assertStatusCode(NOT_FOUND);
    }
}
