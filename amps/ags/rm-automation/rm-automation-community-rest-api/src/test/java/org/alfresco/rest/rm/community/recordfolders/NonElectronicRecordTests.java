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
package org.alfresco.rest.rm.community.recordfolders;

import static java.lang.Integer.MAX_VALUE;
import static java.util.Arrays.asList;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createFullNonElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createFullNonElectronicUnfiledContainerChildRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.verifyFullNonElectronicRecord;
import static org.alfresco.utility.constants.UserRole.SiteManager;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertFalse;

import java.util.Random;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.record.RecordProperties;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildProperties;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledContainerAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledRecordFolderAPI;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.Test;

/**
 * Create/File Non-Electronic Record into Unfiled Record Container/Record Folder ReST API tests
 *
 * @author Kristijan Conkas
 * @since 2.6
 */
public class NonElectronicRecordTests extends BaseRMRestTest
{
    /**
     * <pre>
     * Given a parent container that is a record folder
     * And the record folder is open
     * When I try to create a non-electronic record within the parent container
     * Then the non-electronic record is created
     * And the details of the new record are returned
     * <pre>
     * and
     * <pre>
     *
     *
     * Given a parent container that is an unfiled record folder or the root unfiled record container
     * When I try to create a non-electronic record within the parent container
     * Then the non-electronic record is created
     * And the details of the new record are returned
     * </pre>
     * @throws Exception if record can't be created
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Non-electronic records can be created in valid containers"
    )
    public void canCreateInValidContainers(String folderId, String type) throws Exception
    {
        logger.info("Root container:\n" + toJson(folderId));

        // Use these properties for non-electronic record to be created
        String title = "Title " + getRandomAlphanumeric();
        String description = "Description " + getRandomAlphanumeric();
        String box = "Box "+ getRandomAlphanumeric();
        String file = "File " + getRandomAlphanumeric();
        String shelf = "Shelf " + getRandomAlphanumeric();
        String storageLocation = "Storage Location " + getRandomAlphanumeric();
        String name = "Record " + getRandomAlphanumeric();

        Random random = new Random();
        Integer numberOfCopies = random.nextInt(MAX_VALUE);
        Integer physicalSize = random.nextInt(MAX_VALUE);

        String nonElectronicId;
        if (RECORD_FOLDER_TYPE.equalsIgnoreCase(type))
        {
            // Only record folders can be opened or closed
            RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
            assertFalse(recordFolderAPI.getRecordFolder(folderId).getProperties().getIsClosed());

            // Set values of all available properties for the non electronic records
            Record nonElectrinicRecordModel = createFullNonElectronicRecordModel(name, title, description, box, file, shelf, storageLocation, numberOfCopies, physicalSize);

            // Create non-electronic record
            nonElectronicId = recordFolderAPI.createRecord(nonElectrinicRecordModel, folderId).getId();
        }
        else if(UNFILED_CONTAINER_TYPE.equalsIgnoreCase(type))
        {
            // Set values of all available properties for the non electronic records
            UnfiledContainerChild nonElectrinicRecordModel = createFullNonElectronicUnfiledContainerChildRecordModel(name, title, description, box, file, shelf,
                                                                                                                     storageLocation, numberOfCopies, physicalSize);

            // Create non-electronic record
            UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();
            nonElectronicId = unfiledContainersAPI.createUnfiledContainerChild(nonElectrinicRecordModel, folderId).getId();
        }
        else if(UNFILED_RECORD_FOLDER_TYPE.equalsIgnoreCase(type))
        {
            // Set values of all available properties for the non electronic records
            UnfiledContainerChild nonElectrinicRecordModel = createFullNonElectronicUnfiledContainerChildRecordModel(name, title, description, box, file, shelf,
                                                                                                                     storageLocation, numberOfCopies, physicalSize);

            // Create non-electronic record
            UnfiledRecordFolderAPI unfiledRecordFoldersAPI = getRestAPIFactory().getUnfiledRecordFoldersAPI();
            nonElectronicId = unfiledRecordFoldersAPI.createUnfiledRecordFolderChild(nonElectrinicRecordModel, folderId).getId();
        }
        else
        {
            throw new Exception("Unsuported type = " + type);
        }

        // Verify the create request status code
        assertStatusCode(CREATED);

        // Get newly created non-electronic record and verify its properties
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        Record nonElectronicRecord = recordsAPI.getRecord(nonElectronicId);
        verifyFullNonElectronicRecord(nonElectronicRecord, name, title, description, box, file, shelf, storageLocation, numberOfCopies, physicalSize);
    }

    /**
     * <pre>
     * Given a parent container that is a record folder
     * And the record folder is closed
     * When I try to create a non-electronic record within the parent container
     * Then nothing happens
     * And an error is reported
     * </pre>
     */
    @Test(description = "Non-electronic record can't be created in closed record folder")
    public void cantCreateInClosedFolder()
    {
        RecordCategoryChild recordFolder = createCategoryFolderInFilePlan();

        // The folder should be open
        assertFalse(recordFolder.getProperties().getIsClosed());

        // Close the folder
        closeFolder(recordFolder.getId());

        // Try to create it, this should fail and throw an exception
        getRestAPIFactory().getRecordFolderAPI().createRecord(createNonElectronicRecordModel(), recordFolder.getId());

        // Verify the status code
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    /**
     * <pre>
     * Given a parent container that is a record folder
     * And the record folder is open
     * When I try to create a non-electronic record within the parent container
     * And I do not provide all the required mandatory property values
     * Then nothing happens
     * And an error is reported
     * </pre>
     * and
     * <pre>
     *
     * Given a parent container that is an unfiled record folder or the root unfiled record container
     * When I try to create a non-electronic record within the parent container
     * And I do not provide all the required mandatory property values
     * Then nothing happens
     * And an error is reported
     * </pre>
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Non-electronic record can only be created if all mandatory properties are given"
    )
    public void allMandatoryPropertiesRequired(String folderId, String type)
    {
        logger.info("Root container:\n" + toJson(folderId));

        if (type.equals(RECORD_FOLDER_TYPE))
        {
            // Only record folders can be opened or closed
            RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
            assertFalse(recordFolderAPI.getRecordFolder(folderId).getProperties().getIsClosed());
            // Component without name and title
            Record noNameOrTitle = Record.builder().nodeType(NON_ELECTRONIC_RECORD_TYPE).build();

            // Component with title only
            Record titleOnly = Record.builder()
                                .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                .properties(RecordProperties.builder()
                                        .title("Title " + getRandomAlphanumeric())
                                        .build())
                                .build();

            // Try to create invalid components
            asList(noNameOrTitle, titleOnly).forEach(c ->
            {
                logger.info("Creating non-electronic record with body:\n" + toJson(c));

                getRestAPIFactory().getRecordFolderAPI().createRecord(c, folderId);
                // Verify the status code is BAD_REQUEST
                assertStatusCode(BAD_REQUEST);
            });
        }
        else if(UNFILED_CONTAINER_TYPE.equalsIgnoreCase(type))
        {
            // Component without name and title
            UnfiledContainerChild noNameOrTitle = UnfiledContainerChild.builder().nodeType(NON_ELECTRONIC_RECORD_TYPE).build();

            // Component with title only
            UnfiledContainerChild titleOnly = UnfiledContainerChild.builder()
                                .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                .properties(UnfiledContainerChildProperties.builder()
                                        .title("Title " + getRandomAlphanumeric())
                                        .build())
                                .build();

            // Try to create invalid components
            asList(noNameOrTitle, titleOnly).forEach(c ->
            {
                logger.info("Creating non-electronic record with body:\n" + toJson(c));

                getRestAPIFactory().getUnfiledContainersAPI().createUnfiledContainerChild(c, folderId);
                // Verify the status code is BAD_REQUEST
                assertStatusCode(BAD_REQUEST);
            });
        }
        else
        {
            //we have unfiled record folder type
            // Component without name and title
            UnfiledContainerChild noNameOrTitle = UnfiledContainerChild.builder().nodeType(NON_ELECTRONIC_RECORD_TYPE).build();

            // Component with title only
            UnfiledContainerChild titleOnly = UnfiledContainerChild.builder()
                                .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                .properties(UnfiledContainerChildProperties.builder()
                                        .title("Title " + getRandomAlphanumeric())
                                        .build())
                                .build();

            // Try to create invalid components
            asList(noNameOrTitle, titleOnly).forEach(c ->
            {
                logger.info("Creating non-electronic record with body:\n" + toJson(c));

                getRestAPIFactory().getUnfiledRecordFoldersAPI().createUnfiledRecordFolderChild(c, folderId);

                // Verify the status code is BAD_REQUEST
                assertStatusCode(BAD_REQUEST);
            });
        }
    }

    /**
     * <pre>
     * Given that I am a user without RM privileges
     * When I try to create a non-electronic record
     * Then nothing happens
     * And an error is reported
     * </pre>
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Non-electronic record can't be created if user doesn't have RM privileges"
    )
    public void cantCreateIfNoRmPrivileges(String folderId, String type)
    {
        UserModel user = createSiteManager("zzzuser");

        if (type.equals(RECORD_FOLDER_TYPE))
        {
            // Try to create a record model
            Record recordModel = Record.builder()
                                    .properties(RecordProperties.builder()
                                            .description("Description")
                                            .title("Title")
                                            .build())
                                    .name("Record Name")
                                    .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                    .build();

            getRestAPIFactory().getRecordFolderAPI(user).createRecord(recordModel, folderId);
        }
        else if(UNFILED_CONTAINER_TYPE.equalsIgnoreCase(type))
        {
            // Try to create a record model
            UnfiledContainerChild recordModel = UnfiledContainerChild.builder()
                                                                        .properties(UnfiledContainerChildProperties.builder()
                                                                                .description("Description")
                                                                                .title("Title")
                                                                                .build())
                                                                        .name("Record Name")
                                                                        .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                                                        .build();

            getRestAPIFactory().getUnfiledContainersAPI(user).createUnfiledContainerChild(recordModel, folderId);
        }
        else
        {
            // Try to create a record model
            UnfiledContainerChild recordModel = UnfiledContainerChild.builder()
                                                                        .properties(UnfiledContainerChildProperties.builder()
                                                                                .description("Description")
                                                                                .title("Title")
                                                                                .build())
                                                                        .name("Record Name")
                                                                        .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                                                        .build();

            getRestAPIFactory().getUnfiledRecordFoldersAPI(user).createUnfiledRecordFolderChild(recordModel, folderId);
        }
        // User who isn't an RM site member can't access the container path
        assertStatusCode(FORBIDDEN);
    }

    /**
     * Create user with site manager role and add it to RM site
     * <br>
     * Checks whether the user exists in RM site and creates it if required, with password identical
     * to user name. Note the role is a Core API role, not an RM role.
     * <br>
     * For already existing users, no site membership or role verification is performed.
     * <p>
     * @param userName user name to add
     */
    private UserModel createSiteManager(String userName)
    {
        String siteId = getRestAPIFactory().getRMSiteAPI().getSite().getId();

        // Check if user exists
        UserModel user = new UserModel(userName, userName);

        if (!getDataUser().isUserInRepo(userName))
        {
            // User doesn't exist, create it
            user = getDataUser().createUser(userName, userName);
            getDataUser().addUserToSite(user, new SiteModel(siteId), SiteManager);
        }

        return user;
    }
}
