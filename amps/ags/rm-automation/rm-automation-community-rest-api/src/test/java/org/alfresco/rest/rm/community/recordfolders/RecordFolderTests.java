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

import static org.alfresco.rest.rm.community.base.TestData.RECORD_FOLDER_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.IS_CLOSED;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.FILE_PLAN_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.TRANSFER_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.TRANSFER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.TITLE_PREFIX;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createTempFile;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.common.ReviewPeriod;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.record.RecordProperties;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolder;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolderCollection;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolderProperties;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.utility.report.Bug;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class contains the tests for the Record Folder CRUD API
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class RecordFolderTests extends BaseRMRestTest
{
    public static final String ELECTRONIC_RECORD_NAME = "Record electronic" + getRandomAlphanumeric();
    public static final String NONELECTRONIC_RECORD_NAME = "Record nonelectronic" + getRandomAlphanumeric();
    public static final String RECORD_CATEGORY_NAME = "CATEGORY NAME" + getRandomAlphanumeric();
    private RecordCategory rootCategory;

    @BeforeClass (alwaysRun = true)
    public void preconditionRecordFolderTests()
    {
        rootCategory = createRootCategory(RECORD_CATEGORY_NAME);
    }

    /**
     * Data Provider with:
     * with the object types not allowed as children for a record folder
     *
     * @return node type to be created
     */
    @DataProvider
    public static Object[][] childrenNotAllowedForFolder()
    {
        return new String[][] {
                { FILE_PLAN_TYPE },
                { TRANSFER_CONTAINER_TYPE },
                { UNFILED_CONTAINER_TYPE },
                { UNFILED_RECORD_FOLDER_TYPE },
                { TRANSFER_TYPE },
                { RECORD_CATEGORY_TYPE },
                { FOLDER_TYPE }
        };
    }

    /**
     * Invalid  containers that cannot be updated/deleted with record folder endpoint
     */
    @DataProvider
    public Object[][] getInvalidNodesForRecordFolders()
    {
        return new String[][] {
                { getRestAPIFactory().getFilePlansAPI().getFilePlan(FILE_PLAN_ALIAS).getId()},
                { getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainer(UNFILED_RECORDS_CONTAINER_ALIAS).getId() },
                { getRestAPIFactory().getTransferContainerAPI().getTransferContainer(TRANSFERS_ALIAS).getId() },
                // an arbitrary record category
                { rootCategory.getId()},
                // an arbitrary unfiled records folder
                { createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, "Unfiled Folder " + getRandomAlphanumeric(), UNFILED_RECORD_FOLDER_TYPE).getId() },
                { createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, "Unfiled Record " + getRandomAlphanumeric(), CONTENT_TYPE).getId()}
        };
    }


    /**
     * <pre>
     * Given that RM site is created
     * When I use the API to create a children of wrong type inside a record folder
     * Then the operation fails
     * </pre>
     */
    //TODO enable this test when REPO-2454 is fixed
    @Test
    (
        enabled = false,
        description = "Create invalid types as children for a record folder",
        dataProvider = "childrenNotAllowedForFolder"
    )
    public void createInvalidChildrenForFolder(String nodeType)
    {
        //create a record folder
        RecordCategoryChild folder = createRecordFolder(rootCategory.getId(), getRandomName("recFolder"));
        Record record = Record.builder()
                            .name(ELECTRONIC_RECORD_NAME)
                            .nodeType(nodeType)
                            .build();
        //create invalid child type for the record folder
        getRestAPIFactory().getRecordFolderAPI().createRecord(record,folder.getId());
        // Check the API Response code
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    /**
     * <pre>
     * Given that a record folder exists
     * When I ask for the details of a record folder
     * Then I am given the details of a record folder
     * </pre>
     */
    @Test
    (
        description = "Check the details of a record folder"
    )
    public void checkRecordFolderDetails()
    {
        // Create a folder
        RecordCategoryChild recordCategoryChild = createRecordFolder(rootCategory.getId(), RECORD_FOLDER_NAME);

        // Get the folder including extra information
        RecordFolder recordFolder = getRestAPIFactory().getRecordFolderAPI().getRecordFolder(recordCategoryChild.getId(), "include=" + IS_CLOSED);

        // Verify the returned record folder details
        assertEquals(recordFolder.getNodeType(), RECORD_FOLDER_TYPE);
        assertEquals(recordFolder.getName(), RECORD_FOLDER_NAME);
        assertEquals(recordFolder.getCreatedByUser().getId(), getAdminUser().getUsername());
        assertEquals(recordFolder.getModifiedByUser().getId(), getAdminUser().getUsername());
        assertEquals(recordFolder.getProperties().getTitle(), TITLE_PREFIX + RECORD_FOLDER_NAME);
        assertNotNull(recordFolder.getProperties().getIdentifier(),"The record folder doesn't have a identifier");
        assertFalse(recordFolder.getProperties().getVitalRecordIndicator(), "The record folder has the vital record identifier");
        assertFalse(recordFolder.getProperties().getIsClosed(), "The record folder is closed");
    }

    /**
     * <pre>
     * Given that a record folder exists
     * When I use the API to update its details
     * Then the details of the record folder are updated
     * The above test does treat any custom metadata
     * Note: The details of the record folder includes any custom meta-data
     * </pre>
     */
    @Test
    (
        description = "Update the details of a record folder"
    )
    public void updateRecordFolderDetails()
    {
        // Create a record folder
        RecordCategoryChild recordCategoryChild = createRecordFolder(rootCategory.getId(), getRandomName("recFolder"));

        // Create record category first
        String folderDescription = "The folder description is updated" + getRandomAlphanumeric();
        String folderName = "The folder name is updated" + getRandomAlphanumeric();
        String folderTitle = "Update title " + getRandomAlphanumeric();
        String location = "Location "+ getRandomAlphanumeric();

        // Create the record folder properties to update
        RecordFolder recordFolder = RecordFolder.builder()
                                                .name(folderName)
                                                .properties(RecordFolderProperties.builder()
                                                        .title(folderTitle)
                                                        .description(folderDescription)
                                                        .vitalRecordIndicator(true)
                                                        .reviewPeriod(new ReviewPeriod("month","1"))
                                                        .location(location)
                                                        .build())
                                                .build();

        // Update the record folder
        RecordFolder updatedRecordFolder = getRestAPIFactory().getRecordFolderAPI().updateRecordFolder(recordFolder, recordCategoryChild.getId());

        // Check the Response Status Code
        assertStatusCode(OK);

        // Verify the returned details for the record folder
        AssertJUnit.assertEquals(updatedRecordFolder.getName(), folderName);
        RecordFolderProperties recordFolderProperties = updatedRecordFolder.getProperties();
        AssertJUnit.assertEquals(recordFolderProperties.getDescription(), folderDescription);
        AssertJUnit.assertEquals(recordFolderProperties.getTitle(), folderTitle);
        assertTrue(recordFolderProperties.getVitalRecordIndicator());
        AssertJUnit.assertEquals(recordFolderProperties.getLocation(), location);
        assertNotNull(recordFolderProperties.getReviewPeriod().getPeriodType());
        assertNotNull(recordFolderProperties.getReviewPeriod().getExpression());
    }

    /**
     * <pre>
     * Given other nodes type than record folders exists
     * When I use the API to update its details
     * Then the request fails
     * </pre>
     */
    @Test
    (
        description = "Update the details for other nodes than record folder with the request used for record-folders ",
        dataProvider = "getInvalidNodesForRecordFolders"
    )
    public void updateOtherNodeTypesDetails(String nodeId)
    {
        // Create record category first
        String nodeDescription = "The folder description is updated" + getRandomAlphanumeric();
        String nodeName = "The folder name is updated" + getRandomAlphanumeric();
        String nodeTitle = "Update title " + getRandomAlphanumeric();


        // Create the record folder properties to update
        RecordFolder recordFolder = RecordFolder.builder()
                                                .name(nodeName)
                                                .properties(RecordFolderProperties.builder()
                                                                                  .title(nodeTitle)
                                                                                  .description(nodeDescription)
                                                                                  .vitalRecordIndicator(true)
                                                                                  .reviewPeriod(new ReviewPeriod("month", "1"))
                                                                                  .build())
                                                .build();

        // Update the record folder
        getRestAPIFactory().getRecordFolderAPI().updateRecordFolder(recordFolder, nodeId);

        // Check the Response Status Code
        assertStatusCode(BAD_REQUEST);
    }

    /**
     * <pre>
     * Given other nodes type than record folders exists
     * When I use the API from record-folders to delete the nodes
     * Then the request fails
     * </pre>
     */
    @Test
    (
        description = "Delete invalid nodes type with the DELETE record folders request",
        dataProvider = "getInvalidNodesForRecordFolders"
    )
    public void deleteInvalidNodesRecordFolder(String nodeId)
    {
        // Delete the nodes with record-folders end-point
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        recordFolderAPI.deleteRecordFolder(nodeId);

        // Check the response status code
        assertStatusCode(BAD_REQUEST);
    }

    /**
     * <pre>
     * Given that a record folder exists
     * When I use the API to delete the record folder
     * Then it is deleted according to the normal rules governing the deletion of record folders
     * </pre>
     */
    @Test
    (
        description = "Delete record folder"
    )
    public void deleteRecordFolder()
    {
        // Create the record folder
        RecordCategoryChild recordFolder = createRecordFolder(rootCategory.getId(), getRandomName("recFolder"));

        // Delete the record folder
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        String recordFolderId = recordFolder.getId();
        recordFolderAPI.deleteRecordFolder(recordFolderId);

        // Check the response status code
        assertStatusCode(NO_CONTENT);

        // Check the record folder is not found
        recordFolderAPI.getRecordFolder(recordFolderId);

        // Check the response status code
        assertStatusCode(NOT_FOUND);
    }

    /**
     * Given that a record folder exists
     * When the record folder is closed
     * Then a request can be made to reopen it
     */
    @Test
    (
        description = "A closed record folder can be reopened"
    )
    @Bug(id="RM-4808")
    public void openClosedRecordFolder()
    {
        // Create a record folder
        RecordCategoryChild recordFolder = createRecordFolder(rootCategory.getId(), getRandomName("recFolder"));

        // Assert that the record folder is not closed
        assertFalse(recordFolder.getProperties().getIsClosed());

        // Get the record folder API
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();

        // Create a record folder model to close it
        RecordFolder recordFolderModel = RecordFolder.builder()
                                                     .properties(RecordFolderProperties.builder()
                                                                                       .isClosed(true)
                                                                                       .build())
                                                     .build();

        // Make a request to close the record folder
        RecordFolder updatedRecordFolder = recordFolderAPI.updateRecordFolder(recordFolderModel, recordFolder.getId());

        // Verify that the record folder is closed now
        assertTrue(updatedRecordFolder.getProperties().getIsClosed());

        // Create a record folder model to reopen it
        recordFolderModel = RecordFolder.builder()
                                        .properties(RecordFolderProperties.builder()
                                                                          .isClosed(false)
                                                                          .build())
                                        .build();

        // Make a request to reopen the record folder
        updatedRecordFolder = recordFolderAPI.updateRecordFolder(recordFolderModel, recordFolder.getId());

        // Verify that the record folder is open now
        assertFalse(updatedRecordFolder.getProperties().getIsClosed());
    }

    /**
     * Given a container that is a record folder
     * When I try to list the records from the record folder
     * Then I receive a list of all the records contained within the record folder
     */
    @Test
    public void listRecordsFromRecordFolder()
    {
        final int NUMBER_OF_RECORDS = 5;
        String containerId = createRecordFolder(rootCategory.getId(), getRandomName("recFolder")).getId();
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        // Create Electronic Records
        ArrayList<Record> children = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_RECORDS; i++)
        {
            //build the electronic record
            Record record = Record.builder()
                                  .name(ELECTRONIC_RECORD_NAME + i)
                                  .nodeType(CONTENT_TYPE)
                                  .build();
            //create a child
            Record child = recordFolderAPI.createRecord(record, containerId, createTempFile(ELECTRONIC_RECORD_NAME + i, ELECTRONIC_RECORD_NAME + i));

            children.add(child);
        }
        //Create NonElectronicRecords
        for (int i = 0; i < NUMBER_OF_RECORDS; i++)
        {
            Record nonelectronicRecord = Record.builder()
                                               .properties(RecordProperties.builder()
                                                                           .description("Description")
                                                                           .title("Title")
                                                                           .build())
                                               .name(NONELECTRONIC_RECORD_NAME + i)
                                               .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                               .build();
            //create records
            Record child = recordFolderAPI.createRecord(nonelectronicRecord, containerId);

            children.add(child);
        }

        // List children from API
        RecordFolderCollection apiChildren = (RecordFolderCollection) recordFolderAPI.getRecordFolderChildren(containerId,"include=properties").assertThat().entriesListIsNotEmpty();

        // Check status code
        assertStatusCode(OK);


        // Check listed children against created list
        apiChildren.getEntries().forEach(c ->
        {
            Record record = c.getEntry();
            assertNotNull(record.getId());
            logger.info("Checking child " + record.getId());

            try
            {
                // Find this child in created children list
                Record createdComponent = children.stream()
                                                  .filter(child -> child.getId().equals(record.getId()))
                                                  .findFirst()
                                                  .orElseThrow();

                // Created by
                assertEquals(record.getCreatedByUser().getId(), getAdminUser().getUsername());

                // Is parent Id set correctly
                assertEquals(record.getParentId(), containerId);

                //check the record name
                assertEquals(createdComponent.getName(), record.getName(),
                        "Record Name" + record.getName() + " doesn't match the one returned on create");
                assertTrue(createdComponent.getName().contains(createdComponent.getProperties().getIdentifier()),
                        "Record Name"+ createdComponent.getName()+" doesn't contain the record identifier in response when creating");
                assertEquals(createdComponent.getNodeType(), record.getNodeType());

            } catch (NoSuchElementException e)
            {
                fail("No child element for " + record.getId());
            }
        });
    }

    @AfterClass (alwaysRun = true)
    public void tearDown()
    {
        deleteRecordCategory(rootCategory.getId());
    }
}
