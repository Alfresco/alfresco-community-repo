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

import static org.alfresco.rest.rm.community.base.TestData.ELECTRONIC_RECORD_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicUnfiledContainerChildModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createTempFile;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.getFile;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledContainerAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledRecordFolderAPI;
import org.alfresco.utility.report.Bug;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Create/File electronic records tests
 *
 * @author Kristijan Conkas
 * @since 2.6
 */
public class ElectronicRecordTests extends BaseRMRestTest
{
    /** Invalid parent containers where electronic records can't be created */
    @DataProvider(name = "invalidParentContainers")
    public  Object[][] invalidParentContainers()
    {
        return new String[][]
        {
            // record category
            { createCategoryFolderInFilePlan().getParentId() },
            // file plan root
            { FILE_PLAN_ALIAS },
            // transfers
            { TRANSFERS_ALIAS }
        };
    }

    /**
     * <pre>
     * Given a parent container that is NOT a record folder or an unfiled record folder
     * When I try to create an electronic record within the parent container
     * Then nothing happens
     * And an error is reported
     * </pre>
     * @param container The parent container
     */
    @Test
    (
        dataProvider = "invalidParentContainers",
        description = "Electronic records can't be created in invalid parent containers"
    )
    public void cantCreateElectronicRecordsInInvalidContainers(String container)
    {
        // Create an electronic record in the given container, this should throw an IllegalArgumentException
        getRestAPIFactory().getRecordFolderAPI().createRecord(createElectronicRecordModel(), container, getFile(IMAGE_FILE));

        // Verify the create request status code
        assertStatusCode(BAD_REQUEST);
    }

    /**
     * <pre>
     * Given a parent container that is a record folder
     * And the record folder is closed
     * When I try to create an electronic record within the parent container
     * Then nothing happens
     * And an error is reported
     * </pre>
     */
    @Test
    (
        description = "Electronic record can't be created in closed record folder"
    )
    public void cantCreateElectronicRecordInClosedFolder()
    {
        RecordCategoryChild recordFolder = createCategoryFolderInFilePlan();

        // The folder should be open
        assertFalse(recordFolder.getProperties().getIsClosed());

        // Close the folder
        closeFolder(recordFolder.getId());

        // Try to create an electronic record, this should throw IllegalArgumentException
        getRestAPIFactory().getRecordFolderAPI().createRecord(createElectronicRecordModel(), recordFolder.getId(), getFile(IMAGE_FILE));

        // Verify the status code
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    /**
     * <pre>
     * Given a parent container that is a record folder
     * And the record folder is open
     * When I try to create an electronic record within the parent container
     * And I do not provide all the required mandatory property values
     * Then nothing happens
     * And an error is reported
     * </pre>
     * and
     * <pre>
     *
     *
     * Given a parent container that is an unfiled record folder or the root unfiled record container
     * When I try to create an electronic record within the parent container
     * And I do not provide all the required mandatory property values
     * Then nothing happens
     * And an error is reported
     * </pre>
     * @param folderId The folder, which the record will be created in
     * @param type The type of the record folder, which the record will be created in
     * @throws Exception if record can't be created
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Electronic record can only be created if all mandatory properties are given"
    )
    public void canCreateElectronicRecordOnlyWithMandatoryProperties(String folderId, String type) throws Exception
    {
        logger.info("Root container:\n" + toJson(folderId));

        if (RECORD_FOLDER_TYPE.equalsIgnoreCase(type))
        {
            // Only record folders can be opened or closed
            RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
            assertFalse(recordFolderAPI.getRecordFolder(folderId).getProperties().getIsClosed());

            // Record without name
            Record recordModel = Record.builder().nodeType(CONTENT_TYPE).build();

            // Try to create it
            recordFolderAPI.createRecord(recordModel, folderId);
        }
        else if(UNFILED_CONTAINER_TYPE.equalsIgnoreCase(type))
        {
            UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();
            UnfiledContainerChild recordModel = UnfiledContainerChild.builder().nodeType(CONTENT_TYPE).build();
            unfiledContainersAPI.createUnfiledContainerChild(recordModel, folderId);
        }
        else if(UNFILED_RECORD_FOLDER_TYPE.equalsIgnoreCase(type))
        {
            UnfiledRecordFolderAPI unfiledRecordFoldersAPI = getRestAPIFactory().getUnfiledRecordFoldersAPI();
            UnfiledContainerChild recordModel = UnfiledContainerChild.builder().nodeType(CONTENT_TYPE).build();
            unfiledRecordFoldersAPI.createUnfiledRecordFolderChild(recordModel, folderId);
        }
        else
        {
            throw new Exception("Unsuported type = " + type);
        }

        // Verify the status code is BAD_REQUEST
        assertStatusCode(BAD_REQUEST);
    }

    /**
     * <pre>
     * Given a parent container that is a record folder
     * And the record folder is open
     * When I try to create an electronic record within the parent container
     * Then the electronic record is created
     * And the details of the new record are returned
     * </pre>
     * and
     * <pre>
     *
     * Given a parent container that is an unfiled record folder or the root unfiled record container
     * When I try to create an electronic record within the parent container
     * Then the electronic record is created
     * And the details of the new record are returned
     * </pre>
     * @param folderId The folder, which the record will be created in
     * @param type The type of the folder, which the record will be created in
     * @throws Exception if record can't be created
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Electronic records can be created in record folders, unfiled record folders or unfiled record folder root"
    )
    public void canCreateElectronicRecordsInValidContainers(String folderId, String type) throws Exception
    {
        String newRecordId;
        String expectedName;
        if (RECORD_FOLDER_TYPE.equalsIgnoreCase(type))
        {
            RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
            Record recordModel = createElectronicRecordModel();
            newRecordId = recordFolderAPI.createRecord(recordModel, folderId, getFile(IMAGE_FILE)).getId();
            expectedName = recordModel.getName();
        }
        else if(UNFILED_CONTAINER_TYPE.equalsIgnoreCase(type))
        {
            UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();
            UnfiledContainerChild recordModel = createElectronicUnfiledContainerChildModel();
            newRecordId = unfiledContainersAPI.uploadRecord(recordModel, folderId, getFile(IMAGE_FILE)).getId();
            expectedName = recordModel.getName();
        }
        else if(UNFILED_RECORD_FOLDER_TYPE.equalsIgnoreCase(type))
        {
            UnfiledRecordFolderAPI unfiledRecordFoldersAPI = getRestAPIFactory().getUnfiledRecordFoldersAPI();
            UnfiledContainerChild recordModel = createElectronicUnfiledContainerChildModel();
            newRecordId = unfiledRecordFoldersAPI.uploadRecord(recordModel, folderId, getFile(IMAGE_FILE)).getId();
            expectedName = recordModel.getName();
        }
        else
        {
            throw new Exception("Unsuported type = " + type);
        }
        // Verify the create request status code
        assertStatusCode(CREATED);

        // Get newly created electronic record and verify its properties
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        Record record = recordsAPI.getRecord(newRecordId);
        String recordName = record.getName();

        // Created record will have record identifier inserted in its name but will be prefixed with the name it was created as
        assertTrue(recordName.startsWith(expectedName));
        assertTrue(recordName.contains(record.getProperties().getIdentifier()));
    }

    /**
     * <pre>
     * Given that a record name isn't specified
     * When I create an electronic record
     * Then the record name defaults to filed file name.
     * </pre>
     * @param folderId The folder, which the record will be created in
     * @param type The type of the folder, which the record will be created in
     * @throws Exception if record can't be created
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Electronic records can be created in unfiled record folder or unfiled record root"
    )
    public void recordNameDerivedFromFileName(String folderId, String type) throws Exception
    {
        String newRecordId;
        if (RECORD_FOLDER_TYPE.equalsIgnoreCase(type))
        {
            // Create a record model without a name
            Record recordModel = Record.builder().nodeType(CONTENT_TYPE).build();

            // Create an electronic record
            RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
            newRecordId = recordFolderAPI.createRecord(recordModel, folderId, getFile(IMAGE_FILE)).getId();
        }
        else if(UNFILED_CONTAINER_TYPE.equalsIgnoreCase(type))
        {
            UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();
            UnfiledContainerChild recordModel = UnfiledContainerChild.builder().nodeType(CONTENT_TYPE).build();
            newRecordId = unfiledContainersAPI.uploadRecord(recordModel, folderId, getFile(IMAGE_FILE)).getId();
        }
        else if(UNFILED_RECORD_FOLDER_TYPE.equalsIgnoreCase(type))
        {
            UnfiledRecordFolderAPI unfiledRecordFoldersAPI = getRestAPIFactory().getUnfiledRecordFoldersAPI();
            UnfiledContainerChild recordModel = UnfiledContainerChild.builder().nodeType(CONTENT_TYPE).build();
            newRecordId = unfiledRecordFoldersAPI.uploadRecord(recordModel, folderId, getFile(IMAGE_FILE)).getId();
        }
        else
        {
            throw new Exception("Unsuported type = " + type);
        }
        // Verify the create request status code
        assertStatusCode(CREATED);

        // Get newly created electronic record and verify its properties
        Record electronicRecord = getRestAPIFactory().getRecordsAPI().getRecord(newRecordId);

        // Record will have record identifier inserted in its name but will for sure start with file name and end with its extension
        assertTrue(electronicRecord.getName().startsWith(IMAGE_FILE.substring(0, IMAGE_FILE.indexOf("."))));
        assertTrue(electronicRecord.getName().contains(electronicRecord.getProperties().getIdentifier()));
    }

    /**
     * <pre>
     * Given that I want to create an electronic record in one unfiled record folder
     * When I use the path relative to the one unfiled record folder
     * Then the containers in the relativePath that don't exist are created before creating the electronic record
     * <pre>
     */
    @Test
    @Bug (id = "RM-4568")
    public void createElectronicRecordWithRelativePath()
    {
        // The containers specified on the relativePath parameter don't exist on server
        String parentUbnfiledRecordFolderName = "ParentUnfiledRecordFolder" + getRandomAlphanumeric();
        String unfiledRecordFolderPathEl1 = "UnfiledRecordFolderPathEl1" + getRandomAlphanumeric();
        String unfiledRecordFolderPathEl2 = "UnfiledRecordFolderPathEl2" + getRandomAlphanumeric();
        String unfiledRecordFolderPathEl3 = "UnfiledRecordFolderPathEl3" + getRandomAlphanumeric();

        String parentUnfiledRecordFolderId = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, parentUbnfiledRecordFolderName, UNFILED_RECORD_FOLDER_TYPE).getId();

        String relativePath = unfiledRecordFolderPathEl1 + "/" + unfiledRecordFolderPathEl2 + "/" + unfiledRecordFolderPathEl3;
        UnfiledContainerChild unfiledContainerChildModel= UnfiledContainerChild.builder()
                                                                                .name(ELECTRONIC_RECORD_NAME)
                                                                                .nodeType(CONTENT_TYPE)
                                                                                .relativePath(relativePath)
                                                                                .build();



        UnfiledRecordFolderAPI unfiledRecordFoldersAPI = getRestAPIFactory().getUnfiledRecordFoldersAPI();
        UnfiledContainerChild recordCreated = unfiledRecordFoldersAPI.uploadRecord(unfiledContainerChildModel, parentUnfiledRecordFolderId, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME));

        // Verify the create request status code
        assertStatusCode(CREATED);

        // Get newly created electronic record and verify its properties
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        Record record = recordsAPI.getRecord(recordCreated.getId());

        assertTrue(record.getName().startsWith(ELECTRONIC_RECORD_NAME));
        assertEquals(unfiledRecordFoldersAPI.getUnfiledRecordFolder(record.getParentId()).getName(), unfiledRecordFolderPathEl3);

        // The first relative path element exists and the second one does not exist
        String unfiledRecordFolderPathEl4 = "UnfiledRecordFolderPathEl4" + getRandomAlphanumeric();
        relativePath = unfiledRecordFolderPathEl1 + "/" + unfiledRecordFolderPathEl4;
        unfiledContainerChildModel.setRelativePath(relativePath);
        recordCreated = unfiledRecordFoldersAPI.uploadRecord(unfiledContainerChildModel, parentUnfiledRecordFolderId, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME));
        // verify the create request status code
        assertStatusCode(CREATED);

        // get newly created electronic record and verify its properties
        record = recordsAPI.getRecord(recordCreated.getId());

        assertTrue(record.getName().startsWith(ELECTRONIC_RECORD_NAME));
        assertTrue(unfiledRecordFoldersAPI.getUnfiledRecordFolder(record.getParentId()).getName().equals(unfiledRecordFolderPathEl4));

        //the containers from the RELATIVE PATH exists
        unfiledContainerChildModel.setName(ELECTRONIC_RECORD_NAME + getRandomAlphanumeric());
        recordCreated = unfiledRecordFoldersAPI.uploadRecord(unfiledContainerChildModel, parentUnfiledRecordFolderId, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME));
        // verify the create request status code
        assertStatusCode(CREATED);
        // get newly created electronic record and verify its properties
        record = recordsAPI.getRecord(recordCreated.getId());

        assertTrue(record.getName().startsWith(ELECTRONIC_RECORD_NAME));

        assertTrue(unfiledRecordFoldersAPI.getUnfiledRecordFolder(record.getParentId()).getName().equals(unfiledRecordFolderPathEl4));
    }

    /**
     * <pre>
     * Given a parent container that is a record folder
     * When I try to create a record with name1 and create another one with the same given name
     * Then the second record is created with success
     * </pre>
     */
    @Test(description = "Electronic records can be created in record folder with duplicate name")
    @Bug(id ="RM-5116, RM-5012")
    public void canCreateElectronicRecordsWithDuplicateName()
    {
        RecordCategoryChild recordFolder = createCategoryFolderInFilePlan();

        // Create an electronic record with the name "Record 1"
        Record recordModel = Record.builder().name("Record 1").nodeType(CONTENT_TYPE).build();
        getRestAPIFactory().getRecordFolderAPI().createRecord(recordModel, recordFolder.getId());
        // Verify the status code
        assertStatusCode(CREATED);

        // Try to create another electronic record with the same name
        getRestAPIFactory().getRecordFolderAPI().createRecord(recordModel, recordFolder.getId());

        // Verify the status code
        assertStatusCode(CREATED);
    }
}
