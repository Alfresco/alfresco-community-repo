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

import static java.time.LocalDateTime.now;
import static org.alfresco.rest.rm.community.base.TestData.RECORD_CATEGORY_NAME;
import static org.alfresco.rest.rm.community.base.TestData.RECORD_FOLDER_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.IS_CLOSED;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PATH;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.TITLE_PREFIX;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.base.TestData;
import org.alfresco.rest.rm.community.model.common.ReviewPeriod;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChildCollection;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChildProperties;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolder;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolderProperties;
import org.alfresco.rest.rm.community.requests.gscore.api.FilePlanAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordCategoryAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.utility.report.Bug;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * This class contains the tests for the Record Folder CRUD API
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class RecordFolderTests extends BaseRMRestTest
{
    private static final int NUMBER_OF_FOLDERS = 5;

    /**
     * <pre>
     * Given that a record category exists
     * When I use the API to create a new record folder
     * Then it is created within the record category
     * </pre>
     * <pre>
     * Given that a record category exists
     * When I use the API to create a folder (cm:folder type)
     * Then the folder is converted to rma:recordFolder within the record category
     * (see RM-4572 comments)
     * </pre>
     */
    @Test
    (
        description = "Create a record folder into a record category.",
        dataProviderClass = TestData.class,
        dataProvider = "folderTypes"
    )
    @Bug (id = "RM-4572")
    public void createFolderTest(String folderType) throws Exception
    {
        // Authenticate with admin user
        RecordCategory rootRecordCategory = createRootCategory(RECORD_CATEGORY_NAME + getRandomAlphanumeric());

        // Create the record folder
        RecordCategoryChild recordFolder = createRecordCategoryChild(rootRecordCategory.getId(), RECORD_FOLDER_NAME, folderType);

        // Assert status code
        assertStatusCode(CREATED);

        // Check record folder has been created within the record category
        AssertJUnit.assertEquals(rootRecordCategory.getId(), recordFolder.getParentId());

        // Verify the returned values for the record folder
        assertFalse(recordFolder.getIsRecordCategory());
        assertTrue(recordFolder.getIsRecordFolder());
        AssertJUnit.assertEquals(recordFolder.getName(), RECORD_FOLDER_NAME);
        AssertJUnit.assertEquals(recordFolder.getNodeType(), RECORD_FOLDER_TYPE);
        AssertJUnit.assertEquals(recordFolder.getCreatedByUser().getId(), getAdminUser().getUsername());

        // Verify the returned record folder properties
        RecordCategoryChildProperties folderProperties = recordFolder.getProperties();
        AssertJUnit.assertEquals(folderProperties.getTitle(), TITLE_PREFIX + RECORD_FOLDER_NAME);
        assertNotNull(folderProperties.getIdentifier());
    }

    /**
     * <pre>
     * Given that RM site is created
     * When I use the API to create a new record folder into transfers/holds/unfiled containers
     * Then the operation fails
     * </pre>
     */
    @Test
    (
        description = "Create a record folder into transfers/unfiled/file plan container",
        dataProviderClass = TestData.class,
        dataProvider = "getContainers"
    )
    @Bug(id="RM-4327")
    public void createRecordFolderIntoSpecialContainers(String containerAlias) throws Exception
    {
        String containerId;
        if (FILE_PLAN_ALIAS.equalsIgnoreCase(containerAlias))
        {
            containerId = getRestAPIFactory().getFilePlansAPI().getFilePlan(containerAlias).getId();
        }
        else if(TRANSFERS_ALIAS.equalsIgnoreCase(containerAlias))
        {
            containerId = getRestAPIFactory().getTransferContainerAPI().getTransferContainer(containerAlias).getId();
        }
        else
        {
            //is unfiled container
            containerId = getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainer(containerAlias).getId();;
        }

        // Create a record folder
        createRecordFolder(containerId, RECORD_FOLDER_NAME);

        // Check the API Response code
        assertStatusCode(BAD_REQUEST);
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
    public void checkRecordFolderDetails() throws Exception
    {
        // Create a category
        RecordCategory rootRecordCategory = createRootCategory(RECORD_CATEGORY_NAME + getRandomAlphanumeric());

        // Create a folder
        RecordCategoryChild recordCategoryChild = createRecordFolder(rootRecordCategory.getId(), RECORD_FOLDER_NAME);

        // Get the folder including extra information
        RecordFolder recordFolder = getRestAPIFactory().getRecordFolderAPI().getRecordFolder(recordCategoryChild.getId(), "include=" + IS_CLOSED);

        // Verify the returned record folder details
        AssertJUnit.assertEquals(recordFolder.getNodeType(), RECORD_FOLDER_TYPE);
        assertTrue(RECORD_FOLDER_TYPE.equals(recordFolder.getNodeType()));
        AssertJUnit.assertEquals(recordFolder.getName(), RECORD_FOLDER_NAME);
        AssertJUnit.assertEquals(recordFolder.getCreatedByUser().getId(), getAdminUser().getUsername());
        AssertJUnit.assertEquals(recordFolder.getModifiedByUser().getId(), getAdminUser().getUsername());
        AssertJUnit.assertEquals(recordFolder.getProperties().getTitle(), TITLE_PREFIX + RECORD_FOLDER_NAME);
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
    public void updateRecordFolderDetails() throws Exception
    {
        // Create a record category
        RecordCategory rootRecordCategory = createRootCategory(RECORD_CATEGORY_NAME + getRandomAlphanumeric());

        // Create a record folder
        RecordCategoryChild recordCategoryChild = createRecordFolder(rootRecordCategory.getId(), RECORD_FOLDER_NAME);

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
     * Given that a record folder exists
     * When I use the API to delete the record folder
     * Then it is deleted according to the normal rules governing the deletion of record folders
     * </pre>
     */
    @Test
    (
        description = "Delete record folder"
    )
    public void deleteRecordFolder() throws Exception
    {
        // Create the record category
        RecordCategory rootRecordCategory = createRootCategory(RECORD_CATEGORY_NAME + getRandomAlphanumeric());

        // Create the record folder
        RecordCategoryChild recordFolder = createRecordFolder(rootRecordCategory.getId(), RECORD_FOLDER_NAME);

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
     * <pre>
     * Given that a record category exists
     * And contains several record folders
     * When I use the API to get the record category children for an existing record category
     * Then I am provided with a list of the contained record category children and their details
     * </pre>
    */
    @Test
    (
        description = "Get children of a record category"
    )
    public void getFolders() throws Exception
    {
        // Authenticate with admin user
        RecordCategory rootRecordCategory = createRootCategory(RECORD_CATEGORY_NAME + getRandomAlphanumeric());

        // Add child folders
        ArrayList<RecordCategoryChild> children = new ArrayList<RecordCategoryChild>();
        for (int i = 0; i < NUMBER_OF_FOLDERS; i++)
        {
            // Create a record folder
            RecordCategoryChild recordCategoryChild = createRecordFolder(rootRecordCategory.getId(), getRandomAlphanumeric());
            assertNotNull(recordCategoryChild.getId());
            children.add(recordCategoryChild);
        }

        // Get record category children from API
        RecordCategoryChildCollection recordCategoryChildren = getRestAPIFactory().getRecordCategoryAPI().getRecordCategoryChildren(rootRecordCategory.getId(), "include=isRecordCategory,isRecordFolder");

        // Check status code
        assertStatusCode(OK);

        // Check children against created list
        recordCategoryChildren.getEntries().forEach(c ->
                {
                    RecordCategoryChild recordCategoryChild = c.getEntry();
                    String recordCategoryChildId = recordCategoryChild.getId();
                    assertNotNull(recordCategoryChildId);
                    logger.info("Checking child " + recordCategoryChildId);

                    try
                    {
                        // Find this child in created children list
                        RecordCategoryChild createdComponent = children.stream()
                                                                     .filter(child -> child.getId().equals(recordCategoryChildId))
                                                                     .findFirst()
                                                                     .get();

                        // Created by
                        assertEquals(recordCategoryChild.getCreatedByUser().getId(), getAdminUser().getUsername());

                        // Is parent id set correctly
                        assertEquals(recordCategoryChild.getParentId(), rootRecordCategory.getId());

                        // Boolean properties related to node type
                        assertTrue(recordCategoryChild.getIsRecordFolder());
                        assertFalse(recordCategoryChild.getIsRecordCategory());

                        assertEquals(createdComponent.getName(), recordCategoryChild.getName());
                        assertEquals(createdComponent.getNodeType(), recordCategoryChild.getNodeType());

                    }
                    catch (NoSuchElementException e)
                    {
                        fail("No child element for " + recordCategoryChildId);
                    }
                }
            );

    }

    /**
     * <pre>
     * Given that I want to create a record folder
     * When I use the API with the relativePath
     * Then the categories specified in the relativePath that don't exist are created
     * </pre>
     */
    @Test
    (
        description = "Create a folder using record-categories endpoint, based on the relativePath. " +
            "Containers in the relativePath that do not exist are created before the node is created"
    )
    public void createRecordFolderWithRelativePath() throws Exception
    {
        // The record category to be created
        RecordCategory recordCategoryModel = RecordCategory.builder()
                .name(RECORD_CATEGORY_NAME)
                .nodeType(RECORD_CATEGORY_TYPE)
                .build();
        FilePlanAPI filePlansAPI = getRestAPIFactory().getFilePlansAPI();
        RecordCategory createRootRecordCategory = filePlansAPI.createRootRecordCategory(recordCategoryModel, FILE_PLAN_ALIAS, "include=" + PATH);
        // Check the API response code
        assertStatusCode(CREATED);
        String recordCategoryId = createRootRecordCategory.getId();

        // relativePath specify the container structure to create relative to the record folder to be created
        String relativePath = now().getYear() + "/" + now().getMonth() + "/" + now().getDayOfMonth();

        // The record folder to be created
        RecordCategoryChild recordFolderModel = RecordCategoryChild.builder()
                .name(RECORD_FOLDER_NAME)
                .nodeType(RECORD_FOLDER_TYPE)
                .relativePath(relativePath)
                .build();

        // Create the record folder
        RecordCategoryAPI recordCategoryAPI = getRestAPIFactory().getRecordCategoryAPI();
        RecordCategoryChild recordCategoryChild = recordCategoryAPI.createRecordCategoryChild(recordFolderModel, recordCategoryId, "include=" + PATH);

        // Check the API response code
        assertStatusCode(CREATED);

        // Verify the returned details for the record folder
        assertFalse(recordCategoryChild.getIsRecordCategory());
        assertTrue(recordCategoryChild.getIsRecordFolder());

        // Check the path return contains the relativePath
        AssertJUnit.assertTrue(recordCategoryChild.getPath().getName().contains(relativePath));

        // Check the parent is a category
        assertNotNull(recordCategoryAPI.getRecordCategory(recordCategoryChild.getParentId()).getId());

        // Check the created folder from the server
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        RecordFolder recordFolder = recordFolderAPI.getRecordFolder(recordCategoryChild.getId(), "include=" + PATH);

        // Check the API response code
        assertStatusCode(OK);

        // Verify the returned details for the record folder
        assertTrue(RECORD_FOLDER_TYPE.equals(recordFolder.getNodeType()));

        // Check the path return contains the relativePath
        AssertJUnit.assertTrue(recordFolder.getPath().getName().contains(relativePath));

        // New relative path only a part of containers need to be created before the record folder
        String newRelativePath = now().getYear() + "/" + now().getMonth() + "/" + (now().getDayOfMonth() + 1);

        // The record folder to be created
        RecordCategoryChild newRecordFolderModel = RecordCategoryChild.builder()
                .name(RECORD_FOLDER_NAME)
                .nodeType(RECORD_FOLDER_TYPE)
                .relativePath(newRelativePath)
                .build();

        // Create the record folder
        RecordCategoryChild newRecordCategoryChild = recordCategoryAPI.createRecordCategoryChild(newRecordFolderModel, recordCategoryId, "include=" + PATH);

        // Check the API response code
        assertStatusCode(CREATED);

        // Verify the returned properties for the file plan component - record folder
        assertFalse(newRecordCategoryChild.getIsRecordCategory());
        assertTrue(newRecordCategoryChild.getIsRecordFolder());

        // Check the path return contains the newRelativePath
        AssertJUnit.assertTrue(newRecordCategoryChild.getPath().getName().contains(newRelativePath));

        // Check the parent is a category
        assertNotNull(recordCategoryAPI.getRecordCategory(newRecordCategoryChild.getParentId()).getId());

        // Check the folder created on the server
        RecordFolder newRecordFolder = recordFolderAPI.getRecordFolder(newRecordCategoryChild.getId(), "include=" + PATH);

        // Check the API response code
        assertStatusCode(OK);

        // Verify the returned details for the record folder
        assertTrue(RECORD_FOLDER_TYPE.equals(recordFolder.getNodeType()));

        // Check the path return contains the newRelativePath
        AssertJUnit.assertTrue(newRecordFolder.getPath().getName().contains(newRelativePath));
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
    public void openClosedRecordFolder() throws Exception
    {
        // Create a record folder
        RecordCategoryChild recordFolder = createCategoryFolderInFilePlan();

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

        //FIXME - remove this workaround after RM-4921 is fixed.
        updatedRecordFolder = recordFolderAPI.getRecordFolder(updatedRecordFolder.getId());

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

        //FIXME - remove this workaround after RM-4921 is fixed.
        updatedRecordFolder = recordFolderAPI.getRecordFolder(updatedRecordFolder.getId());

        // Verify that the record folder is open now
        assertFalse(updatedRecordFolder.getProperties().getIsClosed());
    }

    @AfterMethod
    @AfterClass (alwaysRun = true)
    public void tearDown() throws Exception
    {
        FilePlanAPI filePlansAPI = getRestAPIFactory().getFilePlansAPI();
        RecordCategoryAPI recordCategoryAPI = getRestAPIFactory().getRecordCategoryAPI();

        filePlansAPI.getRootRecordCategories(FILE_PLAN_ALIAS).getEntries().forEach(recordCategoryEntry ->
        {
            recordCategoryAPI.deleteRecordCategory(recordCategoryEntry.getEntry().getId());
        });
    }
}
