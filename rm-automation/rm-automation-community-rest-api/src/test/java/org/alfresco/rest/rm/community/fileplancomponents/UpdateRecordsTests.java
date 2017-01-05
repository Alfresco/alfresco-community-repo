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

import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicRecordModel;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
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
 * Update records tests
 * <br>
 * These tests only test the update of electronic and non-electronic tests
 * <p>
 * @author Kristijan Conkas
 * @since 2.6
 */
public class UpdateRecordsTests extends BaseRMRestTest
{
    @Autowired
    private RMUserAPI rmUserAPI;
    
    /* to be used to append to modifications */
    private final String MODIFIED_PREFIX = "modified_";
    
    /**
     * <pre>
     * Given an incomplete record
     * When I try to update the records meta-data
     * Then the record is successfully updated
     * </pre>
     * @throws Exception
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Incomplete records can be updated"
    )
    @AlfrescoTest(jira="RM-4362")
    public void incompleteRecordsCanBeUpdated(FilePlanComponent recordFolder) throws Exception
    {
        FilePlanComponentAPI filePlanComponentsAPI = getRestAPIFactory().getFilePlanComponentsAPI();
        
        // create electronic and non-electronic records in a folder
        FilePlanComponent electronicRecord = filePlanComponentsAPI.createElectronicRecord(createElectronicRecordModel(), IMAGE_FILE, recordFolder.getId());
        assertStatusCode(CREATED);
        FilePlanComponent nonElectronicRecord = filePlanComponentsAPI.createFilePlanComponent(createNonElectronicRecordModel(), recordFolder.getId());
        assertStatusCode(CREATED);
        
        for (FilePlanComponent record: Arrays.asList(electronicRecord, nonElectronicRecord)) {            
            // generate update metadata
            String newName = getModifiedPropertyValue(record.getName());
            String newTitle = getModifiedPropertyValue(record.getProperties().getTitle());
            String newDescription = getModifiedPropertyValue(record.getProperties().getDescription());

            FilePlanComponent updateRecord = FilePlanComponent.builder()
                .name(newName)
                .properties(FilePlanComponentProperties.builder()
                    .description(newDescription)
                    .title(newTitle)
                    .build())
                .build();

            // update record
            filePlanComponentsAPI.updateFilePlanComponent(updateRecord, record.getId());
            assertStatusCode(OK);

            // verify the update got applied
            FilePlanComponent updatedRecord = filePlanComponentsAPI.getFilePlanComponent(record.getId());
            assertEquals(updatedRecord.getName(), newName);
            assertEquals(updatedRecord.getProperties().getTitle(), newTitle);
            assertEquals(updatedRecord.getProperties().getDescription(), newDescription);
        }
    }
    
    /**
     * <pre>
     * Given an incomplete record
     * And that I am a non-admin user with metadata update capabilities
     * When I try to update the records meta-data
     * Then the record is successfully updated
     * </pre>
     * @throws Exception
     */
    @Test
    (
        description = "User with Edit Metadata capabilities can update incomplete record's metadata"
    )
    @AlfrescoTest(jira="RM-4362")
    public void userWithEditMetadataCapsCanUpdateMetadata() throws Exception
    {   
        // create test user and add it with collab. privileges
        UserModel updateUser = getDataUser().createRandomTestUser("updateuser");
        updateUser.setUserRole(UserRole.SiteCollaborator);
        getDataUser().addUserToSite(updateUser, new SiteModel(getRestAPIFactory().getRMSiteAPI().getSite().getId()), UserRole.SiteCollaborator);

        // RM Security Officer is the lowest role with Edit Record Metadata capabilities
        rmUserAPI.assignRoleToUser(updateUser.getUsername(), UserRoles.ROLE_RM_SECURITY_OFFICER);
        rmUserAPI.usingRestWrapper().assertStatusCodeIs(OK);

        // create random folder
        FilePlanComponent randomFolder = createCategoryFolderInFilePlan();
        logger.info("random folder:" + randomFolder.getName());

        // grant updateUser Filing privileges on randomFolder category, this will be
        // inherited to randomFolder
        FilePlanComponentAPI filePlanComponentsAPIAsAdmin = getRestAPIFactory().getFilePlanComponentsAPI();
        rmUserAPI.addUserPermission(filePlanComponentsAPIAsAdmin.getFilePlanComponent(randomFolder.getParentId()),
            updateUser, UserPermissions.PERMISSION_FILING);
        rmUserAPI.usingRestWrapper().assertStatusCodeIs(OK);
        
        // create electronic and non-electronic records in a folder
        FilePlanComponentAPI filePlanComponentsAPI = getRestAPIFactory().getFilePlanComponentsAPI();
        FilePlanComponent electronicRecord = filePlanComponentsAPI.createElectronicRecord(createElectronicRecordModel(), IMAGE_FILE, randomFolder.getId());
        assertStatusCode(CREATED);
        FilePlanComponent nonElectronicRecord = filePlanComponentsAPI.createFilePlanComponent(createNonElectronicRecordModel(), randomFolder.getId());
        assertStatusCode(CREATED);
        
        // get FilePlanComponentAPI instance initialised to updateUser
        FilePlanComponentAPI filePlanComponentsAPIAsUser = getRestAPIFactory().getFilePlanComponentsAPI(updateUser);
        
        for (FilePlanComponent record: Arrays.asList(electronicRecord, nonElectronicRecord)) {
            filePlanComponentsAPIAsUser.getFilePlanComponent(record.getId());
            assertStatusCode(OK);
            
            // generate update metadata
            String newName = getModifiedPropertyValue(record.getName());
            String newTitle = getModifiedPropertyValue(record.getProperties().getTitle());
            String newDescription = getModifiedPropertyValue(record.getProperties().getDescription());

            FilePlanComponent updateRecord = FilePlanComponent.builder()
                .name(newName)
                .properties(FilePlanComponentProperties.builder()
                    .description(newDescription)
                    .title(newTitle)
                    .build())
                .build();

            // update record
            filePlanComponentsAPIAsUser.updateFilePlanComponent(updateRecord, record.getId());
            assertStatusCode(OK);

            // verify the update got applied
            FilePlanComponent updatedRecord = filePlanComponentsAPIAsUser.getFilePlanComponent(record.getId());
            assertEquals(updatedRecord.getName(), newName);
            assertEquals(updatedRecord.getProperties().getTitle(), newTitle);
            assertEquals(updatedRecord.getProperties().getDescription(), newDescription);
            assertEquals(updatedRecord.getModifiedByUser().getId(), updateUser.getUsername());
        }
    }
    
    /**
     * <pre>
     * Given a complete record
     * When I try to update the records meta-data
     * Then it fails
     * And and the records meta-data is unchanged
     * </pre>
     * @throws Exception
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Complete records can't be updated"
    )
    @AlfrescoTest(jira="RM-4362")
    public void completeRecordsCantBeUpdated(FilePlanComponent recordFolder) throws Exception
    {
        FilePlanComponentAPI filePlanComponentsAPI = getRestAPIFactory().getFilePlanComponentsAPI();
        
        // create electronic and non-electronic records in a folder
        FilePlanComponent electronicRecord = filePlanComponentsAPI.createElectronicRecord(createElectronicRecordModel(), IMAGE_FILE, recordFolder.getId());
        assertStatusCode(CREATED);
        closeRecord(electronicRecord);
       
        FilePlanComponent nonElectronicRecord = filePlanComponentsAPI.createFilePlanComponent(createNonElectronicRecordModel(), recordFolder.getId());
        assertStatusCode(CREATED);
        closeRecord(nonElectronicRecord);
        
        for (FilePlanComponent record: Arrays.asList(electronicRecord, nonElectronicRecord)) {
            // generate update metadata
            String newName = getModifiedPropertyValue(record.getName());
            String newTitle = getModifiedPropertyValue(record.getProperties().getTitle());
            String newDescription = getModifiedPropertyValue(record.getProperties().getDescription());

            FilePlanComponent updateRecord = FilePlanComponent.builder()
                .name(newName)
                .properties(FilePlanComponentProperties.builder()
                    .description(newDescription)
                    .title(newTitle)
                    .build())
                .build();

            // attempt to update record
            filePlanComponentsAPI.updateFilePlanComponent(updateRecord, record.getId());
            assertStatusCode(BAD_REQUEST);

            // verify the original record metatada has been retained
            FilePlanComponent updatedRecord = filePlanComponentsAPI.getFilePlanComponent(record.getId());
            assertEquals(updatedRecord.getName(), record.getName());
            assertEquals(updatedRecord.getProperties().getTitle(), record.getProperties().getTitle());
            assertEquals(updatedRecord.getProperties().getDescription(), record.getProperties().getTitle());
        }
    }
    
    /**
     * Helper method to generate modified property value based on original value
     * @param originalValue original value
     * @return modified value
     */
    private String getModifiedPropertyValue(String originalValue)
    {
        return MODIFIED_PREFIX + originalValue;
    }
}
