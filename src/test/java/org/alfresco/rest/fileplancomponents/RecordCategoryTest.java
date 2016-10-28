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
package org.alfresco.rest.fileplancomponents;

import static java.util.UUID.randomUUID;

import static org.alfresco.com.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.com.fileplancomponents.FilePlanComponentFields.NAME;
import static org.alfresco.com.fileplancomponents.FilePlanComponentFields.NODE_TYPE;
import static org.alfresco.com.fileplancomponents.FilePlanComponentFields.PROPERTIES;
import static org.alfresco.com.fileplancomponents.FilePlanComponentFields.PROPERTIES_TITLE;
import static org.alfresco.com.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.com.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
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

import org.alfresco.com.fileplancomponents.FilePlanComponentType;
import org.alfresco.rest.BaseRestTest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.FilePlanComponentsCollection;
import org.alfresco.rest.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.requests.FilePlanComponentApi;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.data.RandomData;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * FIXME: Document me :)
 *
 * @author Kristijan Conkas
 * @author Tuna Aksoy
 * @since 1.0
 */
public class RecordCategoryTest extends BaseRestTest
{
    @Autowired
    private FilePlanComponentApi filePlanComponentApi;

    @Autowired
    private DataUser dataUser;

    // for children creation test
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
        RestWrapper restWrapper = filePlanComponentApi.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        String categoryName = "Category name " + randomUUID().toString().substring(0, 8);
        String categoryTitle = "Category title " + randomUUID().toString().substring(0, 8);

        // Build the record category properties
        JsonObject recordCategoryProperties = buildObject().
                add(NAME, categoryName).
                add(NODE_TYPE, RECORD_CATEGORY_TYPE.toString()).
                addObject(PROPERTIES).
                    add(PROPERTIES_TITLE, categoryTitle).
                    end().
                getJson();

        // Create the record category
        FilePlanComponent filePlanComponent = filePlanComponentApi.createFilePlanComponent(recordCategoryProperties, FILE_PLAN_ALIAS.toString());

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
        
        logger.info("aspects: " + filePlanComponent.getAspectNames());
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
        RestWrapper restWrapper = filePlanComponentApi.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Create record category first
        String categoryName = "Category name " + randomUUID().toString().substring(0, 8);
        String categoryTitle = "Category title " + randomUUID().toString().substring(0, 8);

        // Build the record category properties
        JsonObject recordCategoryProperties = buildObject().
                add(NAME, categoryName).
                add(NODE_TYPE, RECORD_CATEGORY_TYPE.toString()).
                addObject(PROPERTIES).
                    add(PROPERTIES_TITLE, categoryTitle).
                    end().
                getJson();

        // Create the record category
        FilePlanComponent filePlanComponent = filePlanComponentApi.createFilePlanComponent(recordCategoryProperties, FILE_PLAN_ALIAS.toString());

        String newCategoryName = "Rename " + categoryName;

        // Build the properties which will be updated
        JsonObject updateRecordCategoryProperties = buildObject().
                add(NAME, newCategoryName).
                getJson();

        // Update the record category
        FilePlanComponent renamedFilePlanComponent = filePlanComponentApi.updateFilePlanComponent(updateRecordCategoryProperties, filePlanComponent.getId());

        // Verify the status code
        restWrapper.assertStatusCodeIs(OK);

        // Verify the returned file plan component
        assertEquals(renamedFilePlanComponent.getName(), newCategoryName);
        
        // get actual FILE_PLAN_ALIAS id
        FilePlanComponent parentComponent = filePlanComponentApi.getFilePlanComponent(FILE_PLAN_ALIAS.toString());
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
        RestWrapper restWrapper = filePlanComponentApi.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // Create record category first
        String categoryName = "Category name " + randomUUID().toString().substring(0, 8);
        String categoryTitle = "Category title " + randomUUID().toString().substring(0, 8);

        // Build the record category properties
        JsonObject recordCategoryProperties = buildObject().
                add(NAME, categoryName).
                add(NODE_TYPE, RECORD_CATEGORY_TYPE.toString()).
                addObject(PROPERTIES).
                    add(PROPERTIES_TITLE, categoryTitle).
                    end().
                getJson();

        // Create the record category
        FilePlanComponent filePlanComponent = filePlanComponentApi.createFilePlanComponent(recordCategoryProperties, FILE_PLAN_ALIAS.toString());

        // Delete the record category
        filePlanComponentApi.deleteFilePlanComponent(filePlanComponent.getId());

        // Verify the status code
        restWrapper.assertStatusCodeIs(NO_CONTENT);
        
        // deleted component should no longer be retrievable
        filePlanComponentApi.getFilePlanComponent(filePlanComponent.getId());
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
        // create root level category
        FilePlanComponent rootCategory = createCategory(FILE_PLAN_ALIAS.toString(), RandomData.getRandomAlphanumeric());
        assertNotNull(rootCategory.getId());

        // create subcategory as a child of rootCategory
        FilePlanComponent childCategory = createCategory(rootCategory.getId(), RandomData.getRandomAlphanumeric());

        // child category created?
        assertNotNull(childCategory.getId());
        
        // verify child category
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
        // create root level category
        FilePlanComponent rootCategory = createCategory(FILE_PLAN_ALIAS.toString(), RandomData.getRandomAlphanumeric());
        assertNotNull(rootCategory.getId());   

        // add child categories/folders
        ArrayList<FilePlanComponent> children = new ArrayList<FilePlanComponent>();
        for (int i=0; i < NUMBER_OF_CHILDREN; i++)
        {
            // create a child
            FilePlanComponent child = createComponent(rootCategory.getId(),
                RandomData.getRandomAlphanumeric(),
                // half of the children should be subcategories, the other subfolders
                (i <= NUMBER_OF_CHILDREN / 2) ? RECORD_CATEGORY_TYPE : RECORD_FOLDER_TYPE);
            assertNotNull(child.getId());
            children.add(child);
        }

        // list children from API
        RestWrapper restWrapper = filePlanComponentApi.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        FilePlanComponentsCollection apiChildren = filePlanComponentApi.listChildComponents(rootCategory.getId());
        restWrapper.assertStatusCodeIs(OK);
        logger.info("parent: " + rootCategory.getId());
        
        // check listed children against created list
        apiChildren.getEntries().forEach(c -> 
        {
            FilePlanComponent filePlanComponent = c.getFilePlanComponent();
            assertNotNull(filePlanComponent.getId());
            logger.info("checking child " + filePlanComponent.getId());
            
            try 
            {
                // find this child in created children list
                FilePlanComponent createdComponent = children.stream()
                    .filter(child -> child.getId().equals(filePlanComponent.getId()))
                    .findFirst()
                    .get();

                // created by
                assertEquals(filePlanComponent.getCreatedByUser().getId(), dataUser.getAdminUser().getUsername());
                
                // is parent Id set correctly?
                assertEquals(filePlanComponent.getParentId(), rootCategory.getId());
                
                // only categories or folders have been created
                assertFalse(filePlanComponent.isIsFile());
     
                // boolean properties related to node type
                // only RECORD_CATEGORY_TYPE and RECORD_FOLDER_TYPE have been created
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

                // does returned object have the same contents as the created one?
                assertEquals(createdComponent.getName(), filePlanComponent.getName());
                assertEquals(createdComponent.getNodeType(), filePlanComponent.getNodeType());
                
                // verify properties
            } 
            catch (NoSuchElementException e)
            {
                fail("No child element for " + filePlanComponent.getId());
            }
        });
    }

    /**
     * Helper method to create child category
     * @param parentCategoryId
     * @param categoryName
     * @throws Exception on unsuccessful component creation
     */
    private FilePlanComponent createCategory(String parentCategoryId, String categoryName) throws Exception
    {
        return createComponent(parentCategoryId, categoryName, RECORD_CATEGORY_TYPE);
    }

    /**
     * Helper method to create generic child component
     * @param parentComponentId
     * @param componentName
     * @param componentType
     * @return
     * @throws Exception
     */
    private FilePlanComponent createComponent(String parentComponentId, String componentName, FilePlanComponentType componentType) throws Exception
    {
        RestWrapper restWrapper = filePlanComponentApi.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        JsonObject componentProperties = buildObject().
            add(NAME, componentName).
            add(NODE_TYPE, componentType.toString()).
            addObject(PROPERTIES).
                add(PROPERTIES_TITLE, "Title for " + componentName).
                end().
            getJson();

        FilePlanComponent fpc = filePlanComponentApi.createFilePlanComponent(componentProperties, parentComponentId);
        restWrapper.assertStatusCodeIs(CREATED);
        return fpc;
    }
}
