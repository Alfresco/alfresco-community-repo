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
package org.alfresco.rest.rm.community.recordcategories;

import static java.time.LocalDateTime.now;

import static org.alfresco.rest.rm.community.base.TestData.RECORD_FOLDER_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PATH;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.TITLE_PREFIX;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createRecordCategoryChildModel;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import org.alfresco.rest.core.v0.BaseAPI.RETENTION_SCHEDULE;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.base.DataProviderClass;
import org.alfresco.rest.rm.community.model.fileplan.FilePlan;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChildCollection;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChildProperties;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolder;
import org.alfresco.rest.rm.community.requests.gscore.api.FilePlanAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordCategoryAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.v0.RecordCategoriesAPI;
import org.alfresco.utility.report.Bug;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Record category related API tests
 *
 * @author Kristijan Conkas
 * @author Tuna Aksoy
 * @since 2.6
 */
public class RecordCategoryTests extends BaseRMRestTest
{
    public static final String RECORD_CATEGORY_NAME = "CATEGORY NAME" + getRandomAlphanumeric();

    /** Number of children (for children creation test) */
    private static final int NUMBER_OF_CHILDREN = 10;
    private static final int NUMBER_OF_FOLDERS = 5;

    @Autowired
    private RecordCategoriesAPI recordCategoriesAPI;

    /**
     * Invalid  containers that cannot be deleted with record category end-point
     */
    @DataProvider (name = "invalidContainersToDelete")
    public Object[][] getNodesToDelete()
    {
        return new String[][] {
                { FILE_PLAN_ALIAS },
                { UNFILED_RECORDS_CONTAINER_ALIAS },
                { TRANSFERS_ALIAS },
                // an arbitrary record category
                { createCategoryFolderInFilePlan(getAdminUser()).getId() },
                // an arbitrary unfiled records folder
                { createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, "Unfiled Folder " + getRandomAlphanumeric(), UNFILED_RECORD_FOLDER_TYPE).getId() }
        };
    }

    /**
     * <pre>
     * Given that a record category exists
     * When I ask the API to update the details of the record category
     * Then the details of the record category are updated
     * </pre>
     */
    @Test
    (
        description = "Rename root category"
    )
    public void renameCategory()
    {
        // Create record category first
        String categoryName = "Category name " + getRandomAlphanumeric();
        String categoryTitle = "Category title " + getRandomAlphanumeric();

        // Create the root record category
        RecordCategory rootRecordCategory = createRootCategory(categoryName, categoryTitle);

        String newCategoryName = "Rename " + categoryName;

        // Build the properties which will be updated
        RecordCategory recordCategoryUpdated = RecordCategory.builder().name(newCategoryName).build();

        // Update the record category
        RecordCategory renamedRecordCategory = getRestAPIFactory().getRecordCategoryAPI().updateRecordCategory(recordCategoryUpdated, rootRecordCategory.getId());

        // Verify the status code
        assertStatusCode(OK);

        // Verify the returned file plan component
        assertEquals(renamedRecordCategory.getName(), newCategoryName);

        // Get actual FILE_PLAN_ALIAS id
        FilePlan filePlan = getRestAPIFactory().getFilePlansAPI().getFilePlan(FILE_PLAN_ALIAS);

        // verify renamed component still has this parent
        assertEquals(renamedRecordCategory.getParentId(), filePlan.getId());
    }

    /**
     * <pre>
     * Given that a record category exists
     * When I ask the API to delete the record category
     * Then the record category and all its contents are deleted
     * </pre>
     */
    @Test
    (
        description = "Delete category"
    )
    public void deleteCategory()
    {
        // Create record category first
        String categoryName = "Category name " + getRandomAlphanumeric();
        String categoryTitle = "Category title " + getRandomAlphanumeric();

        // Create the root record category
        RecordCategory rootRecordCategory = createRootCategory(categoryName, categoryTitle);

        int totalEntries= getRestAPIFactory().getFilePlansAPI().getRootRecordCategories(FILE_PLAN_ALIAS).getPagination().getTotalItems();
        // Delete the record category
        RecordCategoryAPI recordCategoryAPI = getRestAPIFactory().getRecordCategoryAPI();
        String recordCategoryId = rootRecordCategory.getId();
        recordCategoryAPI.deleteRecordCategory(recordCategoryId);

        // Verify the status code
        assertStatusCode(NO_CONTENT);

        // Deleted component should no longer be retrievable
        recordCategoryAPI.getRecordCategory(recordCategoryId);
        assertStatusCode(NOT_FOUND);
        //check the number of entries after delete
        int totalEntriesAfterDelete = getRestAPIFactory().getFilePlansAPI().getRootRecordCategories(FILE_PLAN_ALIAS).getPagination().getTotalItems();
        assertEquals(totalEntriesAfterDelete,(totalEntries-1));
    }

    /**
     * <pre>
     * Given that  nodes that are not record category
     * When I ask  to delete the nodes  with the delete request from the record-categories endpoint
     * Then the request fails
     * </pre>
     */
    @Test
    (
        description = "Delete invalid nodes with delete category endpoint",
        dataProvider = "invalidContainersToDelete"
    )
    public void deleteInvalidNodes(String nodeId)
    {

        // Delete the record category
        RecordCategoryAPI recordCategoryAPI = getRestAPIFactory().getRecordCategoryAPI();
        recordCategoryAPI.deleteRecordCategory(nodeId);

        // Verify the status code
        assertStatusCode(BAD_REQUEST);

    }

    /**
     * <pre>
     * Given that a record category exists
     * When I ask the API to create a record category
     * Then it is created within the record category
     * </pre>
     */
    @Test
    (
        description = "Create child category"
    )
    public void createSubcategory()
    {
        // Create root level category
        RecordCategory rootCategory = createRootCategory(getRandomAlphanumeric());
        assertNotNull(rootCategory.getId());

        // Create sub-category as a child of rootCategory
        RecordCategoryChild recordCategory = createRecordCategoryChild(rootCategory.getId(), RECORD_CATEGORY_NAME, RECORD_CATEGORY_TYPE);

        // Child category created?
        assertNotNull(recordCategory.getId());

        // Verify child category
        assertEquals(recordCategory.getParentId(), rootCategory.getId());
        assertTrue(recordCategory.getIsRecordCategory());
        assertFalse(recordCategory.getIsRecordFolder());
        assertEquals(recordCategory.getNodeType(), RECORD_CATEGORY_TYPE);


        //get the sub-category
        RecordCategory subCategory = getRestAPIFactory().getRecordCategoryAPI().getRecordCategory(recordCategory.getId(),"include=isRecordCategory,isRecordFolder");
        // Verify child category
        assertEquals(subCategory.getParentId(), rootCategory.getId());
        assertEquals(subCategory.getNodeType(), RECORD_CATEGORY_TYPE);
        assertFalse(subCategory.getAspectNames().isEmpty());
        assertNotNull(subCategory.getProperties().getIdentifier());
    }

    /**
     * <pre>
     * Given that a record category exists
     * When I use the API to create children of type record folder
     * Then  a record folder it is created within the record category
     * </pre>
     * <pre>
     * Given that a record category exists
     * When I use the API to create children of type folder (cm:folder type)
     * Then the folder is converted to rma:recordFolder within the record category
     * (see RM-4572 comments)
     * </pre>
     */
    @Test
    (
        description = "Create a record folder into a record category.",
        dataProviderClass = DataProviderClass.class,
        dataProvider = "folderTypes"
    )
    @Bug (id = "RM-4572")
    public void createFolderTest(String folderType)
    {
        // Authenticate with admin user
        RecordCategory rootRecordCategory = createRootCategory(RECORD_CATEGORY_NAME + getRandomAlphanumeric());

        // Create the record folder
        RecordCategoryChild recordFolder = createRecordCategoryChild(rootRecordCategory.getId(), RECORD_FOLDER_NAME, folderType);

        // Assert status code
        assertStatusCode(CREATED);

        // Check record folder has been created within the record category
        assertEquals(rootRecordCategory.getId(), recordFolder.getParentId());

        // Verify the returned values for the record folder
        assertFalse(recordFolder.getIsRecordCategory());
        assertTrue(recordFolder.getIsRecordFolder());
        assertEquals(recordFolder.getName(), RECORD_FOLDER_NAME);
        assertEquals(recordFolder.getNodeType(), RECORD_FOLDER_TYPE);
        assertEquals(recordFolder.getCreatedByUser().getId(), getAdminUser().getUsername());

        // Verify the returned record folder properties
        RecordCategoryChildProperties folderProperties = recordFolder.getProperties();
        assertEquals(folderProperties.getTitle(), TITLE_PREFIX + RECORD_FOLDER_NAME);
        assertNotNull(folderProperties.getIdentifier());
    }
    @Test
    (
        dataProviderClass = DataProviderClass.class,
        dataProvider = "categoryChild"
    )
    @Bug(id = "RM-5116")
    public void createdDuplicateChild(String childType)
    {
        // create a root category
        String rootRecordCategory = createRootCategory(RECORD_CATEGORY_NAME + getRandomAlphanumeric()).getId();

        // Create the record category child
        RecordCategoryChild recordFolder = createRecordCategoryChild(rootRecordCategory, RECORD_FOLDER_NAME, childType);

        // check the response  code
        assertStatusCode(CREATED);
        assertEquals(recordFolder.getName(), RECORD_FOLDER_NAME);

        // Create a record category child with the same name as the exiting one

        RecordCategoryChild recordFolderDuplicate = getRestAPIFactory().getRecordCategoryAPI().createRecordCategoryChild(
                    createRecordCategoryChildModel(RECORD_FOLDER_NAME, childType), rootRecordCategory);

        // check the response  code
        assertStatusCode(CONFLICT);

        // Create a record folder with the same name as the exiting one and with the autoRename parameter on true
        recordFolderDuplicate = getRestAPIFactory().getRecordCategoryAPI()
                                                   .createRecordCategoryChild(createRecordCategoryChildModel(RECORD_FOLDER_NAME,
                                                               childType),
                                                    rootRecordCategory, "autoRename=true");
        // check the response  code
        assertStatusCode(CREATED);
        assertNotEquals(recordFolderDuplicate.getName(), RECORD_FOLDER_NAME);
        assertTrue(recordFolderDuplicate.getName().contains(RECORD_FOLDER_NAME));
    }

    /**
     * <pre>
     * Given that a record category exists
     * And contains a number of record categories and record folders
     * When I ask the API to get me the children of the record category
     * Then I am returned the contained record categories and record folders and their details
     * </pre>
     * <pre>
     * Given that a record category with a disposition schedule exists
     * And contains a number of record categories and record folders
     * When I ask the API to get me the children of the record category
     * Then I am returned the contained record categories and record folders but not the disposition schedule
     * </pre>
     */
    @Test
        (
            description = "Get children of a record category excluding the disposition schedule"
        )
    @Bug (id="RM-5115")
    public void getRecordCategoryChildren()
    {
        // Create root level category
        RecordCategory rootRecordCategory = createRootCategory(getRandomAlphanumeric());
        assertNotNull(rootRecordCategory.getId());

        // Create disposition schedule
        String userName = getAdminUser().getUsername();
        String userPassword = getAdminUser().getPassword();
        String categoryName = rootRecordCategory.getName();
        recordCategoriesAPI.createRetentionSchedule(userName, userPassword, categoryName);

        // Add disposition schedule cut off step
        HashMap<RETENTION_SCHEDULE, String> cutOffStep = new HashMap<>();
        cutOffStep.put(RETENTION_SCHEDULE.NAME, "cutoff");
        cutOffStep.put(RETENTION_SCHEDULE.RETENTION_PERIOD, "day|2");
        cutOffStep.put(RETENTION_SCHEDULE.DESCRIPTION, "Cut off after 2 days");
        recordCategoriesAPI.addDispositionScheduleSteps(userName, userPassword, categoryName, cutOffStep);

        // Add record category children
        List<RecordCategoryChild> children = new ArrayList<>();
        for (int i=0; i < NUMBER_OF_CHILDREN; i++)
        {
            // Create a record category child
            RecordCategoryChild child = createRecordCategoryChild(rootRecordCategory.getId(),
                getRandomAlphanumeric(),
                // half of the children should be sub-categories, the other sub-folders
                (i <= NUMBER_OF_CHILDREN / 2) ? RECORD_CATEGORY_TYPE : RECORD_FOLDER_TYPE);
            assertNotNull(child.getId());
            children.add(child);
        }

        // Get children from API
        RecordCategoryChildCollection recordCategoryChildren = getRestAPIFactory().getRecordCategoryAPI().getRecordCategoryChildren(rootRecordCategory.getId(),"include=isRecordCategory,isRecordFolder");

        // Check status code
        assertStatusCode(OK);
        logger.info("Parent: " + rootRecordCategory.getId());

        // Check listed children against created list
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
                    .orElseThrow();

                // Created by
                assertEquals(recordCategoryChild.getCreatedByUser().getId(), getAdminUser().getUsername());

                // Is parent id set correctly?
                assertEquals(recordCategoryChild.getParentId(), rootRecordCategory.getId());

                // Boolean properties related to node type
                // Only RECORD_CATEGORY_TYPE and RECORD_FOLDER_TYPE have been created
                if (recordCategoryChild.getNodeType().equals(RECORD_CATEGORY_TYPE))
                {
                    assertTrue(recordCategoryChild.getIsRecordCategory());
                    assertFalse(recordCategoryChild.getIsRecordFolder());
                }
                else
                {
                    assertTrue(recordCategoryChild.getIsRecordFolder());
                    assertFalse(recordCategoryChild.getIsRecordCategory());
                }

                // Does returned object have the same contents as the created one?
                assertEquals(createdComponent.getName(), recordCategoryChild.getName());
                assertEquals(createdComponent.getNodeType(), recordCategoryChild.getNodeType());

                // verify the record categories children identifier
                assertNotNull(createdComponent.getProperties().getIdentifier());
            }
            catch (NoSuchElementException e)
            {
                fail("No child element for " + recordCategoryChildId);
            }
        });
    }

    /**
     * <pre>
     * Given that a record category exists
     * When I ask to create an object type which is not a record category or a record folder as a child
     * Then the children are not created and the 422 response code is returned
     * </pre>
     */
    @Test
    (
        description = "Create node types not allowed inside a category",
        dataProviderClass = DataProviderClass.class,
        dataProvider = "childrenNotAllowedForCategory"
    )
    @Bug (id="RM-4367, RM-4572")
    public void createTypesNotAllowedInCategory(String nodeType)
    {
        String componentName = "Component" + getRandomAlphanumeric();

        // Create the category
        RecordCategory rootRecordCategory = createRootCategory(componentName);

        // Create the invalid node type
        createRecordCategoryChild(rootRecordCategory.getId(), componentName, nodeType);
        assertStatusCode(UNPROCESSABLE_ENTITY);
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
    public void getFolders()
    {
        // Authenticate with admin user
        RecordCategory rootRecordCategory = createRootCategory(RECORD_CATEGORY_NAME + getRandomAlphanumeric());

        // Add child folders
        ArrayList<RecordCategoryChild> children = new ArrayList<>();
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
                                                                       .orElseThrow();

                        // Created by
                        assertEquals(recordCategoryChild.getCreatedByUser().getId(), getAdminUser().getUsername());

                        // Is parent id set correctly
                        assertEquals(recordCategoryChild.getParentId(), rootRecordCategory.getId());

                        // Boolean properties related to node type
                        assertTrue(recordCategoryChild.getIsRecordFolder());
                        assertFalse(recordCategoryChild.getIsRecordCategory());

                        assertEquals(createdComponent.getName(), recordCategoryChild.getName());
                        assertEquals(createdComponent.getNodeType(), recordCategoryChild.getNodeType());

                    } catch (NoSuchElementException e)
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
    public void createRecordFolderWithRelativePath()
    {
        // The record category to be created
        RecordCategory recordCategoryModel = RecordCategory.builder()
                                                           .name(RECORD_CATEGORY_NAME + getRandomAlphanumeric())
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
        assertTrue(recordCategoryChild.getPath().getName().contains(relativePath));

        // Check the parent is a category
        assertNotNull(recordCategoryAPI.getRecordCategory(recordCategoryChild.getParentId()).getId());

        // Check the created folder from the server
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        RecordFolder recordFolder = recordFolderAPI.getRecordFolder(recordCategoryChild.getId(), "include=" + PATH);

        // Check the API response code
        assertStatusCode(OK);

        // Verify the returned details for the record folder
        assertEquals(recordFolder.getNodeType(), RECORD_FOLDER_TYPE);

        // Check the path return contains the relativePath
        assertTrue(recordFolder.getPath().getName().contains(relativePath));

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
        assertTrue(newRecordCategoryChild.getPath().getName().contains(newRelativePath));

        // Check the parent is a category
        assertNotNull(recordCategoryAPI.getRecordCategory(newRecordCategoryChild.getParentId()).getId());

        // Check the folder created on the server
        RecordFolder newRecordFolder = recordFolderAPI.getRecordFolder(newRecordCategoryChild.getId(), "include=" + PATH);

        // Check the API response code
        assertStatusCode(OK);

        // Verify the returned details for the record folder
        assertEquals(recordFolder.getNodeType(), RECORD_FOLDER_TYPE);

        // Check the path return contains the newRelativePath
        assertTrue(newRecordFolder.getPath().getName().contains(newRelativePath));
    }


    /**
     * <pre>
     * Given that I want to create a record sub-category
     * When I use the API with the relativePath
     * Then the categories specified in the relativePath that don't exist are created
     * </pre>
     */
    @Test
    (
        description = "Create a sub-category using record-categories endpoint, based on the relativePath. " +
                "Containers in the relativePath that do not exist are created before the node is created"
    )
    public void createRecordSubCategoryWithRelativePath()
    {
        // The record category to be created
        RecordCategory recordCategoryModel = RecordCategory.builder()
                                                           .name(RECORD_CATEGORY_NAME + getRandomAlphanumeric())
                                                           .nodeType(RECORD_CATEGORY_TYPE)
                                                           .build();
        FilePlanAPI filePlansAPI = getRestAPIFactory().getFilePlansAPI();
        RecordCategory createRootRecordCategory = filePlansAPI.createRootRecordCategory(recordCategoryModel, FILE_PLAN_ALIAS, "include=" + PATH);
        // Check the API response code
        assertStatusCode(CREATED);
        String recordCategoryId = createRootRecordCategory.getId();

        // relativePath specify the container structure to create relative to the record folder to be created
        String relativePath = now().getYear() + "/" + now().getMonth() + "/" + now().getDayOfMonth()+ "/"+getRandomAlphanumeric();

        // The record folder to be created
        RecordCategoryChild recordFolderModel = RecordCategoryChild.builder()
                                                                   .name(RECORD_CATEGORY_NAME)
                                                                   .nodeType(RECORD_CATEGORY_TYPE)
                                                                   .relativePath(relativePath)
                                                                   .build();

        // Create the record folder
        RecordCategoryAPI recordCategoryAPI = getRestAPIFactory().getRecordCategoryAPI();
        RecordCategoryChild recordCategoryChild = recordCategoryAPI.createRecordCategoryChild(recordFolderModel, recordCategoryId, "include=" + PATH);

        // Check the API response code
        assertStatusCode(CREATED);

        // Verify the returned details for the record sub-category
        assertTrue(recordCategoryChild.getIsRecordCategory());
        assertFalse(recordCategoryChild.getIsRecordFolder());

        // Check the path return contains the relativePath
        assertTrue(recordCategoryChild.getPath().getName().contains(relativePath));

        // Check the parent is a category
        assertNotNull(recordCategoryAPI.getRecordCategory(recordCategoryChild.getParentId()).getId());

        // Check the created folder from the server
        RecordCategory recordSubCategory = recordCategoryAPI.getRecordCategory(recordCategoryChild.getId(), "include=" + PATH);

        // Check the API response code
        assertStatusCode(OK);

        // Verify the returned details for the record folder
        assertEquals(recordSubCategory.getNodeType(), RECORD_CATEGORY_TYPE);

        // Check the path return contains the relativePath
        assertTrue(recordSubCategory.getPath().getName().contains(relativePath));

        // New relative path only a part of containers need to be created before the record folder
        String newRelativePath = now().getYear() + "/" + now().getMonth() + "/" + (now().getDayOfMonth() + 1) +"/"+getRandomAlphanumeric();

        // The record folder to be created
        RecordCategoryChild newRecordFolderModel = RecordCategoryChild.builder()
                                                                      .name(RECORD_CATEGORY_NAME)
                                                                      .nodeType(RECORD_CATEGORY_TYPE)
                                                                      .relativePath(newRelativePath)
                                                                      .build();

        // Create the record folder
        RecordCategoryChild newRecordCategoryChild = recordCategoryAPI.createRecordCategoryChild(newRecordFolderModel, recordCategoryId, "include=" + PATH);

        // Check the API response code
        assertStatusCode(CREATED);

        // Verify the returned properties for the file plan component - record folder
        assertTrue(newRecordCategoryChild.getIsRecordCategory());
        assertFalse(newRecordCategoryChild.getIsRecordFolder());

        // Check the path return contains the newRelativePath
        assertTrue(newRecordCategoryChild.getPath().getName().contains(newRelativePath));

        // Check the parent is a category
        assertNotNull(recordCategoryAPI.getRecordCategory(newRecordCategoryChild.getParentId()).getId());

        // Check the folder created on the server
        RecordCategory newRecordFolder = recordCategoryAPI.getRecordCategory(newRecordCategoryChild.getId(), "include=" + PATH);

        // Check the API response code
        assertStatusCode(OK);

        // Verify the returned details for the record folder
        assertEquals(recordSubCategory.getNodeType(), RECORD_CATEGORY_TYPE);

        // Check the path return contains the newRelativePath
        assertTrue(newRecordFolder.getPath().getName().contains(newRelativePath));
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
        dataProviderClass = DataProviderClass.class,
        dataProvider = "getContainers"
    )
    @Bug (id = "RM-4327")
    public void createRecordFolderIntoSpecialContainers(String containerAlias)
    {
        String containerId;
        if (FILE_PLAN_ALIAS.equalsIgnoreCase(containerAlias))
        {
            containerId = getRestAPIFactory().getFilePlansAPI().getFilePlan(containerAlias).getId();
        } else if (TRANSFERS_ALIAS.equalsIgnoreCase(containerAlias))
        {
            containerId = getRestAPIFactory().getTransferContainerAPI().getTransferContainer(containerAlias).getId();
        } else
        {
            //is unfiled container
            containerId = getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainer(containerAlias).getId();
        }

        // Create a record folder
        createRecordFolder(containerId, RECORD_FOLDER_NAME);

        // Check the API Response code
        assertStatusCode(BAD_REQUEST);
    }
}
