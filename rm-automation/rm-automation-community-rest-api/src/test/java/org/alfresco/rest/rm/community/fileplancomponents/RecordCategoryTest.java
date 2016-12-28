/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
import java.util.NoSuchElementException;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.base.TestData;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentsCollection;
import org.alfresco.rest.rm.community.requests.igCoreAPI.FilePlanComponentAPI;
import org.alfresco.utility.report.Bug;
import org.testng.annotations.Test;

/**
 * Record category related API tests
 *
 * @author Kristijan Conkas
 * @author Tuna Aksoy
 * @since 2.6
 */
public class RecordCategoryTest extends BaseRMRestTest
{
    // Number of children (for children creation test)
    private static final int NUMBER_OF_CHILDREN = 10;

    /**
     * <pre>
     * Given that a file plan exists
     * When I ask the API to create a root record category
     * Then it is created as a root record category
     */
    @Test
    (
        description = "Create root category"
    )
    public void createCategoryTest() throws Exception
    {
        String categoryName = "Category name " + getRandomAlphanumeric();
        String categoryTitle = "Category title " + getRandomAlphanumeric();

        // Build the record category properties
        FilePlanComponent recordCategory = FilePlanComponent.builder()
            .name(categoryName)
            .nodeType(RECORD_CATEGORY_TYPE)
            .properties(
                    FilePlanComponentProperties.builder()
                        .title(categoryTitle)
                        .build())
            .build();

        // Create the record category
        FilePlanComponent filePlanComponent = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(recordCategory, FILE_PLAN_ALIAS);

        // Verify the status code
        assertStatusCode(CREATED);

        // Verify the returned file plan component
        assertTrue(filePlanComponent.getIsCategory());
        assertFalse(filePlanComponent.getIsFile());
        assertFalse(filePlanComponent.getIsRecordFolder());

        assertEquals(filePlanComponent.getName(), categoryName);
        assertEquals(filePlanComponent.getNodeType(), RECORD_CATEGORY_TYPE);

        assertEquals(filePlanComponent.getCreatedByUser().getId(), getAdminUser().getUsername());

        // Verify the returned file plan component properties
        FilePlanComponentProperties filePlanComponentProperties = filePlanComponent.getProperties();
        assertEquals(filePlanComponentProperties.getTitle(), categoryTitle);

        logger.info("Aspects: " + filePlanComponent.getAspectNames());
    }

    /**
     * <pre>
     * Given that a record category exists
     * When I ask the API to update the details of the record category
     * Then the details of the record category are updated
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

        // Build the record category properties
        FilePlanComponent recordCategory = FilePlanComponent.builder()
            .name(categoryName)
            .nodeType(RECORD_CATEGORY_TYPE)
            .properties(
                    FilePlanComponentProperties.builder()
                        .title(categoryTitle)
                        .build())
            .build();

        // Create the record category
        FilePlanComponentAPI filePlanComponentsAPI = getRestAPIFactory().getFilePlanComponentsAPI();
        FilePlanComponent filePlanComponent = filePlanComponentsAPI.createFilePlanComponent(recordCategory, FILE_PLAN_ALIAS);

        String newCategoryName = "Rename " + categoryName;

        // Build the properties which will be updated
        FilePlanComponent recordCategoryUpdated = FilePlanComponent.builder().name(newCategoryName).build();

        // Update the record category
        FilePlanComponent renamedFilePlanComponent = filePlanComponentsAPI.updateFilePlanComponent(recordCategoryUpdated, filePlanComponent.getId());

        // Verify the status code
        assertStatusCode(OK);

        // Verify the returned file plan component
        assertEquals(renamedFilePlanComponent.getName(), newCategoryName);

        // Get actual FILE_PLAN_ALIAS id
        FilePlanComponent parentComponent = filePlanComponentsAPI.getFilePlanComponent(FILE_PLAN_ALIAS);

        // verify renamed component still has this parent
        assertEquals(renamedFilePlanComponent.getParentId(), parentComponent.getId());
    }

    /**
     * <pre>
     * Given that a record category exists
     * When I ask the API to delete the record category
     * Then the record category and all its contents is deleted
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

        // Build the record category properties
        FilePlanComponent recordCategory = FilePlanComponent.builder()
                .name(categoryName)
                .nodeType(RECORD_CATEGORY_TYPE)
                .properties(
                        FilePlanComponentProperties.builder()
                            .title(categoryTitle)
                            .build())
                .build();

        // Create the record category
        FilePlanComponentAPI filePlanComponentsAPI = getRestAPIFactory().getFilePlanComponentsAPI();
        FilePlanComponent filePlanComponent = filePlanComponentsAPI.createFilePlanComponent(recordCategory, FILE_PLAN_ALIAS);

        // Delete the record category
        filePlanComponentsAPI.deleteFilePlanComponent(filePlanComponent.getId());

        // Verify the status code
        assertStatusCode(NO_CONTENT);

        // Deleted component should no longer be retrievable
        filePlanComponentsAPI.getFilePlanComponent(filePlanComponent.getId());
        assertStatusCode(NOT_FOUND);
    }

    /**
     * <pre>
     * Given that a record category exists
     * When I ask the API to create a record category
     * Then it is created within the record category
     */
    @Test
    (
        description = "Create child category"
    )
    public void createSubcategory() throws Exception
    {
        // Create root level category
        FilePlanComponent rootCategory = createCategory(FILE_PLAN_ALIAS, getRandomAlphanumeric());
        assertNotNull(rootCategory.getId());

        // Create subcategory as a child of rootCategory
        FilePlanComponent childCategory = createCategory(rootCategory.getId(), getRandomAlphanumeric());

        // Child category created?
        assertNotNull(childCategory.getId());

        // Verify child category
        assertEquals(childCategory.getParentId(), rootCategory.getId());
        assertTrue(childCategory.getIsCategory());
        assertFalse(childCategory.getIsFile());
        assertFalse(childCategory.getIsRecordFolder());
        assertEquals(childCategory.getNodeType(), RECORD_CATEGORY_TYPE);
    }

    /**
     * <pre>
     * Given that a record category exists
     * And contains a number of record categories and record folders
     * When I ask the APi to get me the children of the record category
     * Then I am returned the contained record categories and record folders
     * And their details
     */
    @Test
    (
        description = "List children of a category"
    )
    public void listChildren() throws Exception
    {
        // Create root level category
        FilePlanComponent rootCategory = createCategory(FILE_PLAN_ALIAS, getRandomAlphanumeric());
        assertNotNull(rootCategory.getId());

        // Add child categories/folders
        ArrayList<FilePlanComponent> children = new ArrayList<FilePlanComponent>();
        for (int i=0; i < NUMBER_OF_CHILDREN; i++)
        {
            // Create a child
            FilePlanComponent child = createComponent(rootCategory.getId(),
                getRandomAlphanumeric(),
                // half of the children should be subcategories, the other subfolders
                (i <= NUMBER_OF_CHILDREN / 2) ? RECORD_CATEGORY_TYPE : RECORD_FOLDER_TYPE);
            assertNotNull(child.getId());
            children.add(child);
        }

        // List children from API
        FilePlanComponentsCollection apiChildren = getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(rootCategory.getId());

        // Check status code
        assertStatusCode(OK);
        logger.info("parent: " + rootCategory.getId());

        // Check listed children against created list
        apiChildren.getEntries().forEach(c ->
        {
            FilePlanComponent filePlanComponent = c.getFilePlanComponentModel();
            assertNotNull(filePlanComponent.getId());
            logger.info("Checking child " + filePlanComponent.getId());

            try
            {
                // Find this child in created children list
                FilePlanComponent createdComponent = children.stream()
                    .filter(child -> child.getId().equals(filePlanComponent.getId()))
                    .findFirst()
                    .get();

                // Created by
                assertEquals(filePlanComponent.getCreatedByUser().getId(), getAdminUser().getUsername());

                // Is parent Id set correctly?
                assertEquals(filePlanComponent.getParentId(), rootCategory.getId());

                // Only categories or folders have been created
                assertFalse(filePlanComponent.getIsFile());

                // Boolean properties related to node type
                // Only RECORD_CATEGORY_TYPE and RECORD_FOLDER_TYPE have been created
                if (filePlanComponent.getNodeType().equals(RECORD_CATEGORY_TYPE))
                {
                    assertTrue(filePlanComponent.getIsCategory());
                    assertFalse(filePlanComponent.getIsRecordFolder());
                }
                else
                {
                    assertTrue(filePlanComponent.getIsRecordFolder());
                    assertFalse(filePlanComponent.getIsCategory());
                }

                // Does returned object have the same contents as the created one?
                assertEquals(createdComponent.getName(), filePlanComponent.getName());
                assertEquals(createdComponent.getNodeType(), filePlanComponent.getNodeType());

                // Verify properties
                // FIXME: Verify properties
            }
            catch (NoSuchElementException e)
            {
                fail("No child element for " + filePlanComponent.getId());
            }
        });
    }

    /**
     * Given that a record category exists
     * When I ask to create a  object type which is not  a record category or a record folder as a child
     * Then the children are not created  and the 422 response code is returned
     */
    @Test
    (
        description = "Create node types not allowed inside a category",
        dataProviderClass = TestData.class,
        dataProvider = "childrenNotAllowedForCategory"

    )
    @Bug (id="RM-4367")
    public void createTypesNotAllowedInCategory(String nodeType) throws Exception
    {
        String COMPONENT_NAME = "Component"+getRandomAlphanumeric();

        //Create the category
        FilePlanComponent category = createCategory(FILE_PLAN_ALIAS, COMPONENT_NAME);

        //Build node  properties
        FilePlanComponent recordCategory = FilePlanComponent.builder()
                .name(COMPONENT_NAME)
                .nodeType(nodeType)
                .properties(
                        FilePlanComponentProperties.builder()
                            .title("Title for " + COMPONENT_NAME)
                            .build())
                .build();

        //create the invalid node type
        getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(recordCategory, category.getId());
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }


    /**
     * Helper method to create child category
     *
     * @param parentCategoryId The id of the parent category
     * @param categoryName The name of the category
     * @return The created category
     * @throws Exception on unsuccessful component creation
     */
    public FilePlanComponent createCategory(String parentCategoryId, String categoryName) throws Exception
    {
        return createComponent(parentCategoryId, categoryName, RECORD_CATEGORY_TYPE);
    }

    /**
     * Helper method to create generic child component
     *
     * @param parentComponentId The id of the parent file plan component
     * @param componentName The name of the file plan component
     * @param componentType The name of the file plan component
     * @return The created file plan component
     * @throws Exception
     */
    private FilePlanComponent createComponent(String parentComponentId, String componentName, String componentType) throws Exception
    {
        // Build node  properties
        FilePlanComponent component = FilePlanComponent.builder()
                .name(componentName)
                .nodeType(componentType)
                .properties(FilePlanComponentProperties.builder()
                        .title("Title for " + componentName)
                        .build())
                .build();

        FilePlanComponent filePlanComponent = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(component, parentComponentId);
        assertStatusCode(CREATED);

        return filePlanComponent;
    }
}
