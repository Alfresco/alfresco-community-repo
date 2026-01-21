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

package org.alfresco.rest.rm.community.smoke;

import static org.junit.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import static org.alfresco.rest.core.v0.BaseAPI.NODE_PREFIX;
import static org.alfresco.rest.core.v0.BaseAPI.RM_SITE_ID;
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.DELETE_PERSON;
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.LOGIN_SUCCESSFUL;
import static org.alfresco.rest.rm.community.records.SearchRecordsTests.*;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.audit.AuditEntry;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.rest.v0.service.RMAuditService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.model.UserModel;

/**
 * Audit Access tests
 *
 * @author Kavit Shah
 */
public class AuditAccessTests extends BaseRMRestTest
{

    private Optional<UserModel> deletedUser;
    private final String TEST_PREFIX = generateTestPrefix(AuditAccessTests.class);
    private static final String DELETE_USER_EVENT = "Delete User";
    private final String record1 = TEST_PREFIX + "RM-2967 uploaded record";
    private final String classifiedRecord = TEST_PREFIX + "RM-2967 classified record";
    private final String folderName = TEST_PREFIX + "RM-2967 folder";
    private final String categoryName = TEST_PREFIX + "RM-2967 category";
    private final String editedCategoryName = "edited " + categoryName;
    private final String editedFolderName = "edited " + folderName;
    private final String editedRecordName = "edited " + record1;
    private final String login_successfull = "Login Successful";
    private RecordCategory categoryAll;

    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    @Autowired
    private RecordsAPI recordsAPI;
    @Autowired
    private RMAuditService rmAuditService;

    @Test(priority = 1)
    @AlfrescoTest(jira = "RM-2967")
    public void deleteRMUsersShowFullAuditTest()
    {

        createTestPrecondition();
        updateCategoryMetadata();
        updateFolderMetadata();
        updateRecordMetadata();

        // delete record category and folder with rm_admin_deleted
        rmRolesAndActionsAPI.deleteAllItemsInContainer(deletedUser.get().getUsername(), deletedUser.get().getPassword(),
                RM_SITE_ID, editedFolderName);
        rmRolesAndActionsAPI.deleteAllItemsInContainer(deletedUser.get().getUsername(), deletedUser.get().getPassword(),
                RM_SITE_ID, editedCategoryName);

        // delete the user
        Optional.of(deletedUser).ifPresent(x -> getDataUser().deleteUser(x.get()));

        // check for RM-5235 fix
        List<AuditEntry> auditEntries = rmAuditService.getAuditEntriesFilteredByEvent(getDataUser().usingAdmin().getAdminUser(),
                DELETE_PERSON);

        assertTrue("Delete user event not found in the audit log.", auditEntries.stream().anyMatch(
                auditEntry -> auditEntry.getEvent().equals(DELETE_USER_EVENT)));
    }

    @Test(priority = 2)
    public void filterEventsByLoginSuccessful()
    {
        createRMSiteIfNotExists();
        List<AuditEntry> auditEntries = rmAuditService.getAuditEntriesFilteredByEvent(getDataUser().usingAdmin().getAdminUser(),
                LOGIN_SUCCESSFUL);

        assertFalse("Audit results should contain at least one Login Successful event",
                auditEntries.isEmpty());

        assertTrue("Audit results contain only Login Successful events",
                auditEntries.stream()
                        .allMatch(e -> e.getEvent().startsWith(LOGIN_SUCCESSFUL.toString()) || e.getEvent().startsWith(login_successfull)));
    }

    /**
     * Creates the required precondition for the test
     * <p/>
     * See Precondition in current class JavaDoc
     */
    private void createTestPrecondition()
    {
        createRMSiteIfNotExists();

        // create "rm deleted user" user if it does not exist and assign it to RM Administrator role
        createDeletedUser();

        // create category and folder
        categoryAll = createCategoryIfDoesNotExist(categoryName, deletedUser.get());
        createRecordFolderInCategory(folderName, categoryAll, deletedUser.get());
        // upload an electronic record

        recordsAPI.uploadElectronicRecord(deletedUser.get().getUsername(), deletedUser.get().getPassword(), getDefaultElectronicRecordProperties(record1), folderName, CMISUtil.DocumentType.TEXT_PLAIN);
        // upload another electronic record and classify it
        recordsAPI.uploadElectronicRecord(deletedUser.get().getUsername(), deletedUser.get().getPassword(), getDefaultElectronicRecordProperties(classifiedRecord), folderName, CMISUtil.DocumentType.TEXT_PLAIN);
    }

    private void createDeletedUser()
    {
        // create Deleted User
        deletedUser = Optional.ofNullable(getDataUser().createRandomTestUser());
        rmRolesAndActionsAPI.assignRoleToUser(
                getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(),
                deletedUser.get().getUsername(),
                ADMIN);
    }

    private void updateCategoryMetadata()
    {
        HashMap<BaseAPI.RMProperty, String> categoryProperties = new HashMap<>();
        categoryProperties.put(BaseAPI.RMProperty.NAME, editedCategoryName);
        categoryProperties.put(BaseAPI.RMProperty.TITLE, "edited " + TITLE);
        categoryProperties.put(BaseAPI.RMProperty.DESCRIPTION, "edited " + DESCRIPTION);

        // edit some category's properties
        String categoryNodeRef = NODE_PREFIX + rmRolesAndActionsAPI.getItemNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), "/" + categoryName);
        rmRolesAndActionsAPI.updateMetadata(deletedUser.get().getUsername(), deletedUser.get().getPassword(), categoryNodeRef, categoryProperties);
    }

    private void updateFolderMetadata()
    {
        HashMap<BaseAPI.RMProperty, String> folderProperties = new HashMap<>();
        folderProperties.put(BaseAPI.RMProperty.NAME, editedFolderName);
        folderProperties.put(BaseAPI.RMProperty.TITLE, "edited " + TITLE);
        folderProperties.put(BaseAPI.RMProperty.DESCRIPTION, "edited " + DESCRIPTION);

        // edit some folder's properties
        String folderNodeRef = NODE_PREFIX + rmRolesAndActionsAPI.getItemNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), "/" + editedCategoryName + "/" + folderName);
        rmRolesAndActionsAPI.updateMetadata(deletedUser.get().getUsername(), deletedUser.get().getPassword(), folderNodeRef, folderProperties);
    }

    private void updateRecordMetadata()
    {
        HashMap<BaseAPI.RMProperty, String> recordProperties = new HashMap<>();
        recordProperties.put(BaseAPI.RMProperty.NAME, editedRecordName);
        recordProperties.put(BaseAPI.RMProperty.TITLE, "edited " + TITLE);
        recordProperties.put(BaseAPI.RMProperty.AUTHOR, "edited author");
        recordProperties.put(BaseAPI.RMProperty.DESCRIPTION, "edited " + DESCRIPTION);

        // edit some record's properties
        String recordName = recordsAPI.getRecordFullName(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), editedFolderName, record1);
        String recordNodeRef = NODE_PREFIX + rmRolesAndActionsAPI.getItemNodeRef(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), "/" + editedCategoryName + "/" + editedFolderName + "/" + recordName);
        rmRolesAndActionsAPI.updateMetadata(deletedUser.get().getUsername(), deletedUser.get().getPassword(), recordNodeRef, recordProperties);
    }

    private RecordCategory createCategoryIfDoesNotExist(String CATEGORY_ALL, UserModel deletedUser)
    {
        return createRootCategory(deletedUser, CATEGORY_ALL);
    }

    private RecordCategoryChild createRecordFolderInCategory(String FOLDER_SEARCH, RecordCategory recordCategory, UserModel deletedUser)
    {
        return createFolder(deletedUser, recordCategory.getId(), FOLDER_SEARCH);
    }

    private Map<BaseAPI.RMProperty, String> getDefaultElectronicRecordProperties(String recordName)
    {
        Map<BaseAPI.RMProperty, String> defaultProperties = new HashMap<>();
        defaultProperties.put(BaseAPI.RMProperty.NAME, recordName);
        defaultProperties.put(BaseAPI.RMProperty.TITLE, TITLE);
        defaultProperties.put(BaseAPI.RMProperty.DESCRIPTION, DESCRIPTION);
        defaultProperties.put(BaseAPI.RMProperty.CONTENT, TEST_CONTENT);
        return defaultProperties;
    }
}
