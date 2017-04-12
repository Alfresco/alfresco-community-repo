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

import static org.alfresco.rest.rm.community.base.TestData.RECORD_CATEGORY_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.CREATED;
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
import java.util.List;
import java.util.NoSuchElementException;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.base.TestData;
import org.alfresco.rest.rm.community.model.fileplan.FilePlan;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChildCollection;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryProperties;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordCategoryAPI;
import org.alfresco.utility.report.Bug;
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
    /** Number of children (for children creation test) */
    private static final int NUMBER_OF_CHILDREN = 10;

    /**
     * <pre>
     * Given that a file plan exists
     * When I ask the API to create a root record category
     * Then it is created as a root record category
     * </pre>
     * <pre>
     * Given that a file plan exists
     * When I use the API to create a folder (cm:folder type) into the fileplan
     * Then the folder is converted to rma:recordCategory
     * (see RM-4572 comments)
     * </pre>
     */
    @Test
    (
        description = "Create root category",
        dataProviderClass= TestData.class,
        dataProvider = "categoryTypes"
    )
    public void createCategoryTest(String nodeType) throws Exception
    {
        String categoryName = "Category name " + getRandomAlphanumeric();
        String categoryTitle = "Category title " + getRandomAlphanumeric();

        // Create the root record category
        RecordCategory rootRecordCategory = createRootCategory(categoryName, categoryTitle);

        // Verify the status code
        assertStatusCode(CREATED);

        assertEquals(rootRecordCategory.getName(), categoryName);
        assertEquals(rootRecordCategory.getNodeType(), RECORD_CATEGORY_TYPE);

        assertEquals(rootRecordCategory.getCreatedByUser().getId(), getAdminUser().getUsername());

        // Verify the returned root record category properties
        RecordCategoryProperties rootRecordCategoryProperties = rootRecordCategory.getProperties();
        assertEquals(rootRecordCategoryProperties.getTitle(), categoryTitle);
        assertNotNull(rootRecordCategoryProperties.getIdentifier());
        logger.info("Aspects: " + rootRecordCategory.getAspectNames());
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
    public void renameCategory() throws Exception
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
    public void deleteCategory() throws Exception
    {
        // Create record category first
        String categoryName = "Category name " + getRandomAlphanumeric();
        String categoryTitle = "Category title " + getRandomAlphanumeric();

        // Create the root record category
        RecordCategory rootRecordCategory = createRootCategory(categoryName, categoryTitle);

        // Delete the record category
        RecordCategoryAPI recordCategoryAPI = getRestAPIFactory().getRecordCategoryAPI();
        String recordCategoryId = rootRecordCategory.getId();
        recordCategoryAPI.deleteRecordCategory(recordCategoryId);

        // Verify the status code
        assertStatusCode(NO_CONTENT);

        // Deleted component should no longer be retrievable
        recordCategoryAPI.getRecordCategory(recordCategoryId);
        assertStatusCode(NOT_FOUND);
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
    public void createSubcategory() throws Exception
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
    }

    /**
     * <pre>
     * Given that a record category exists
     * And contains a number of record categories and record folders
     * When I ask the API to get me the children of the record category
     * Then I am returned the contained record categories and record folders and their details
     * </pre>
     */
    @Test
    (
        description = "Get children of a record category"
    )
    public void getRecordCategoryChildren() throws Exception
    {
        // Create root level category
        RecordCategory rootRecordCategory = createRootCategory(getRandomAlphanumeric());
        assertNotNull(rootRecordCategory.getId());

        // Add record category children
        List<RecordCategoryChild> children = new ArrayList<RecordCategoryChild>();
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
                    .get();

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

                // FIXME: Verify properties
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
        dataProviderClass = TestData.class,
        dataProvider = "childrenNotAllowedForCategory"
    )
    @Bug (id="RM-4367, RM-4572")
    public void createTypesNotAllowedInCategory(String nodeType) throws Exception
    {
        String componentName = "Component" + getRandomAlphanumeric();

        // Create the category
        RecordCategory rootRecordCategory = createRootCategory(componentName);

        // Create the invalid node type
        createRecordCategoryChild(rootRecordCategory.getId(), componentName, nodeType);
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }
}
