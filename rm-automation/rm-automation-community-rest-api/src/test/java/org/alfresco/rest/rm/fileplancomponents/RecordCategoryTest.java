/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.rm.fileplancomponents;

import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.NAME;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.NODE_TYPE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.PROPERTIES;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_TITLE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.jglue.fluentjson.JsonBuilderFactory.buildObject;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.google.gson.JsonObject;

import org.alfresco.rest.rm.base.BaseRestTest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType;
import org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentsCollection;
import org.alfresco.rest.rm.requests.FilePlanComponentAPI;
import org.alfresco.utility.data.DataUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * Record category related API tests
 *
 * @author Kristijan Conkas
 * @author Tuna Aksoy
 * @since 2.6
 */
public class RecordCategoryTest extends BaseRestTest
{
    @Autowired
    private FilePlanComponentAPI filePlanComponentAPI;

    @Autowired
    private DataUser dataUser;

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
        // Authenticate with admin user
        RestWrapper restWrapper = filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        String categoryName = "Category name " + getRandomAlphanumeric();
        String categoryTitle = "Category title " + getRandomAlphanumeric();

        // Build the record category properties
        JsonObject recordCategoryProperties = buildObject().
                add(NAME, categoryName).
                add(NODE_TYPE, RECORD_CATEGORY_TYPE.toString()).
                addObject(PROPERTIES).
                    add(PROPERTIES_TITLE, categoryTitle).
                    end().
                getJson();

        // Create the record category
        FilePlanComponent filePlanComponent = filePlanComponentAPI.createFilePlanComponent(recordCategoryProperties, FILE_PLAN_ALIAS.toString());

        // Verify the status code
        restWrapper.assertStatusCodeIs(CREATED);

        // Verify the returned file plan component
        assertTrue(filePlanComponent.isIsCategory());
        assertFalse(filePlanComponent.isIsFile());
        assertFalse(filePlanComponent.isIsRecordFolder());

        assertEquals(filePlanComponent.getName(), categoryName);
        assertEquals(filePlanComponent.getNodeType(), RECORD_CATEGORY_TYPE.toString());
        assertFalse(filePlanComponent.isHasRetentionSchedule());

        assertEquals(filePlanComponent.getCreatedByUser().getId(), dataUser.getAdminUser().getUsername());

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
        // Authenticate with admin user
        RestWrapper restWrapper = filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Create record category first
        String categoryName = "Category name " + getRandomAlphanumeric();
        String categoryTitle = "Category title " + getRandomAlphanumeric();

        // Build the record category properties
        JsonObject recordCategoryProperties = buildObject().
                add(NAME, categoryName).
                add(NODE_TYPE, RECORD_CATEGORY_TYPE.toString()).
                addObject(PROPERTIES).
                    add(PROPERTIES_TITLE, categoryTitle).
                    end().
                getJson();

        // Create the record category
        FilePlanComponent filePlanComponent = filePlanComponentAPI.createFilePlanComponent(recordCategoryProperties, FILE_PLAN_ALIAS.toString());

        String newCategoryName = "Rename " + categoryName;

        // Build the properties which will be updated
        JsonObject updateRecordCategoryProperties = buildObject().
                add(NAME, newCategoryName).
                getJson();

        // Update the record category
        FilePlanComponent renamedFilePlanComponent = filePlanComponentAPI.updateFilePlanComponent(updateRecordCategoryProperties, filePlanComponent.getId());

        // Verify the status code
        restWrapper.assertStatusCodeIs(OK);

        // Verify the returned file plan component
        assertEquals(renamedFilePlanComponent.getName(), newCategoryName);

        // Get actual FILE_PLAN_ALIAS id
        FilePlanComponent parentComponent = filePlanComponentAPI.getFilePlanComponent(FILE_PLAN_ALIAS.toString());

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
        // Authenticate with admin user
        RestWrapper restWrapper = filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Create record category first
        String categoryName = "Category name " + getRandomAlphanumeric();
        String categoryTitle = "Category title " + getRandomAlphanumeric();

        // Build the record category properties
        JsonObject recordCategoryProperties = buildObject().
                add(NAME, categoryName).
                add(NODE_TYPE, RECORD_CATEGORY_TYPE.toString()).
                addObject(PROPERTIES).
                    add(PROPERTIES_TITLE, categoryTitle).
                    end().
                getJson();

        // Create the record category
        FilePlanComponent filePlanComponent = filePlanComponentAPI.createFilePlanComponent(recordCategoryProperties, FILE_PLAN_ALIAS.toString());

        // Delete the record category
        filePlanComponentAPI.deleteFilePlanComponent(filePlanComponent.getId());

        // Verify the status code
        restWrapper.assertStatusCodeIs(NO_CONTENT);

        // Deleted component should no longer be retrievable
        filePlanComponentAPI.getFilePlanComponent(filePlanComponent.getId());
        restWrapper.assertStatusCodeIs(NOT_FOUND);
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
        FilePlanComponent rootCategory = createCategory(FILE_PLAN_ALIAS.toString(), getRandomAlphanumeric());
        assertNotNull(rootCategory.getId());

        // Create subcategory as a child of rootCategory
        FilePlanComponent childCategory = createCategory(rootCategory.getId(), getRandomAlphanumeric());

        // Child category created?
        assertNotNull(childCategory.getId());

        // Verify child category
        assertEquals(childCategory.getParentId(), rootCategory.getId());
        assertTrue(childCategory.isIsCategory());
        assertFalse(childCategory.isIsFile());
        assertFalse(childCategory.isIsRecordFolder());
        assertEquals(childCategory.getNodeType(), RECORD_CATEGORY_TYPE.toString());
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
        FilePlanComponent rootCategory = createCategory(FILE_PLAN_ALIAS.toString(), getRandomAlphanumeric());
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

        // Authenticate with admin user
        RestWrapper restWrapper = filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // List children from API
        FilePlanComponentsCollection apiChildren = filePlanComponentAPI.listChildComponents(rootCategory.getId());

        // Check status code
        restWrapper.assertStatusCodeIs(OK);
        logger.info("parent: " + rootCategory.getId());

        // Check listed children against created list
        apiChildren.getEntries().forEach(c ->
        {
            FilePlanComponent filePlanComponent = c.getFilePlanComponent();
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
                assertEquals(filePlanComponent.getCreatedByUser().getId(), dataUser.getAdminUser().getUsername());

                // Is parent Id set correctly?
                assertEquals(filePlanComponent.getParentId(), rootCategory.getId());

                // Only categories or folders have been created
                assertFalse(filePlanComponent.isIsFile());

                // Boolean properties related to node type
                // Only RECORD_CATEGORY_TYPE and RECORD_FOLDER_TYPE have been created
                if (filePlanComponent.getNodeType().equals(RECORD_CATEGORY_TYPE.toString()))
                {
                    assertTrue(filePlanComponent.isIsCategory());
                    assertFalse(filePlanComponent.isIsRecordFolder());
                }
                else
                {
                    assertTrue(filePlanComponent.isIsRecordFolder());
                    assertFalse(filePlanComponent.isIsCategory());
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
    private FilePlanComponent createComponent(String parentComponentId, String componentName, FilePlanComponentType componentType) throws Exception
    {
        RestWrapper restWrapper = filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        JsonObject componentProperties = buildObject().
            add(NAME, componentName).
            add(NODE_TYPE, componentType.toString()).
            addObject(PROPERTIES).
                add(PROPERTIES_TITLE, "Title for " + componentName).
                end().
            getJson();

        FilePlanComponent fpc = filePlanComponentAPI.createFilePlanComponent(componentProperties, parentComponentId);
        restWrapper.assertStatusCodeIs(CREATED);
        return fpc;
    }
}
