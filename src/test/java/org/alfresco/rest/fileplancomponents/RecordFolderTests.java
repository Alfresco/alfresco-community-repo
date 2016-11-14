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

import static org.alfresco.rest.base.TestData.CATEGORY_NAME;
import static org.alfresco.rest.base.TestData.FOLDER_NAME;
import static org.alfresco.rest.base.TestData.FOLDER_TITLE;
import static org.alfresco.rest.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.model.fileplancomponents.FilePlanComponentFields.IS_CLOSED;
import static org.alfresco.rest.model.fileplancomponents.FilePlanComponentFields.NAME;
import static org.alfresco.rest.model.fileplancomponents.FilePlanComponentFields.NODE_TYPE;
import static org.alfresco.rest.model.fileplancomponents.FilePlanComponentFields.PROPERTIES;
import static org.alfresco.rest.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_DESCRIPTION;
import static org.alfresco.rest.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_LOCATION;
import static org.alfresco.rest.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_REVIEW_PERIOD;
import static org.alfresco.rest.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_TITLE;
import static org.alfresco.rest.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_VITAL_RECORD_INDICATOR;
import static org.alfresco.rest.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.jglue.fluentjson.JsonBuilderFactory.buildObject;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.google.gson.JsonObject;

import org.alfresco.rest.base.BaseRestTest;
import org.alfresco.rest.base.TestData;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.model.fileplancomponents.FilePlanComponentsCollection;
import org.alfresco.rest.requests.FilePlanComponentAPI;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.report.Bug;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * This class contains the tests for the
 * the Record Folder CRUD API
 *
 * @author Rodica Sutu
 * @since 1.0
 */
public class RecordFolderTests extends BaseRestTest
{
    @Autowired
    public FilePlanComponentAPI filePlanComponentAPI;

    @Autowired
    public DataUser dataUser;

    private static final int NUMBER_OF_FOLDER= 5;
    /**
     * Given that a record category exists
     * When I use the API to create a new record folder
     * Then it is created within the record category
     */
    @Test
    (
        description = "Create a folder into a record category"
    )
    public void createFolderTest() throws Exception
    {
        String CATEGORY = CATEGORY_NAME + getRandomAlphanumeric();
        // Authenticate with admin user
        RestWrapper restWrapper = filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        FilePlanComponent filePlanComponent=createCategory(FILE_PLAN_ALIAS.toString(), CATEGORY);

        // Build the record category properties
        JsonObject recordFolderProperties = buildObject()
                .add(NAME, FOLDER_NAME)
                .add(NODE_TYPE, RECORD_FOLDER_TYPE.toString())
                .addObject(PROPERTIES)
                .add(PROPERTIES_TITLE, FOLDER_TITLE)
                .end()
                .getJson();

        // Create the record folder
        FilePlanComponent folder = filePlanComponentAPI.createFilePlanComponent(recordFolderProperties, filePlanComponent.getId());
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(CREATED);
        // Check folder has been created  within the category created
        assertEquals(filePlanComponent.getId(),folder.getParentId());
        // Verify the returned properties for the file plan component - record folder
        assertFalse(folder.isIsCategory());
        assertFalse(folder.isIsFile());
        assertTrue(folder.isIsRecordFolder());

        assertEquals(folder.getName(), FOLDER_NAME);
        assertEquals(folder.getNodeType(), RECORD_FOLDER_TYPE.toString());
        assertEquals(folder.getCreatedByUser().getId(), dataUser.getAdminUser().getUsername());

        // Verify the returned file plan component properties
        FilePlanComponentProperties folderProperties = folder.getProperties();
        assertEquals(folderProperties.getTitle(), FOLDER_TITLE);
    }

    /**
     * Given that RM site is created
     * When I use the API to create a new record folder into transfers container/holds container/unfiled
     * Then the operation fails
     */
    @Test
    (
        description = "Create a folder into hold/transfers/unfiled/file plan  container",
        dataProviderClass = TestData.class,
        dataProvider = "getContainers"
    )
    @Bug(id="RM-4327")
    public void createFolderIntoSpecialContainers(String filePlanComponent) throws Exception
    {
        // Authenticate with admin user
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        String componentID = filePlanComponentAPI.getFilePlanComponent(filePlanComponent).getId();

        // Build the record category properties
        JsonObject recordFolderProperties = buildObject()
                .add(NAME, FOLDER_NAME)
                .add(NODE_TYPE, RECORD_FOLDER_TYPE.toString())
                .addObject(PROPERTIES)
                .add(PROPERTIES_TITLE, FOLDER_TITLE)
                .end()
                .getJson();

        // Create a record folder
        filePlanComponentAPI.createFilePlanComponent(recordFolderProperties, componentID);

        // Check the API Response code
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(UNPROCESSABLE_ENTITY);
    }

    /**
     * Given that a record folder exists
     * When I ask for the details of a record folder
     * Then I am given the details of a record folder
     */
    @Test
    (
        description = "Check the details returned for a record folder"
    )
    public void checkTheRecordFolderProperties() throws Exception
    {
        String CATEGORY=CATEGORY_NAME + getRandomAlphanumeric();
        // Authenticate with admin user
        RestWrapper restWrapper = filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        FilePlanComponent category = createCategory(FILE_PLAN_ALIAS.toString(), CATEGORY);
        FilePlanComponent folder =createFolder(category.getId(),FOLDER_NAME);

        FilePlanComponent folderDetails=filePlanComponentAPI.withParams("include="+IS_CLOSED).getFilePlanComponent(folder.getId());

        // Verify the returned properties for the file plan component - record folder
        assertEquals(RECORD_FOLDER_TYPE.toString(),folderDetails.getNodeType());
        assertTrue(folderDetails.isIsRecordFolder());
        assertFalse(folderDetails.isIsCategory());
        assertFalse(folderDetails.isIsFile());
        assertFalse(folderDetails.isClosed());

        assertEquals(FOLDER_NAME,folderDetails.getName());
        assertEquals(dataUser.getAdminUser().getUsername(),folderDetails.getCreatedByUser().getId());
        assertEquals(dataUser.getAdminUser().getUsername(), folderDetails.getModifiedByUser().getId());
        assertEquals(FOLDER_TITLE,folderDetails.getProperties().getTitle());

    }


    /**
     * Given that a record folder exists
     * When I use the API to update its details
     * Then the details of the record folder are updated
     * The above test does treat any  custom metadata
     * Note: the details of the record folder includes any custom meta-data
     */
    @Test
    (
        description = "Update the details returned for a record folder"
    )
    public void updateTheRecordFolderProperties() throws Exception
    {
        String CATEGORY = CATEGORY_NAME + getRandomAlphanumeric();
        // Authenticate with admin user
        RestWrapper restWrapper = filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        FilePlanComponent category = createCategory(FILE_PLAN_ALIAS.toString(), CATEGORY);
        FilePlanComponent folder = createFolder(category.getId(), FOLDER_NAME);

        // Create record category first
        String folderDescription = "The folder description is updated" + getRandomAlphanumeric();
        String folderName= "The folder name is updated" + getRandomAlphanumeric();
        String folderTitle = "Update title " + getRandomAlphanumeric();
        String location="Location"+getRandomAlphanumeric();
        String review_period="month|1";

        // Build the file plan root properties
        JsonObject folderProperties = buildObject()
                .add(NAME, folderName)
                .addObject(PROPERTIES)
                .add(PROPERTIES_TITLE, folderTitle)
                .add(PROPERTIES_DESCRIPTION, folderDescription)
                .add(PROPERTIES_VITAL_RECORD_INDICATOR,true)
                .add(PROPERTIES_REVIEW_PERIOD, review_period)
                .add(PROPERTIES_LOCATION, location)
                .end()
                .getJson();

        // Update the record category
        FilePlanComponent folderUpdated = filePlanComponentAPI.updateFilePlanComponent(folderProperties, folder.getId());

        // Check the Response Status Code
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(OK);

        // Verify the returned properties for the file plan component - record folder
        assertEquals(folderName, folderUpdated.getName());
        assertEquals(folderDescription, folderUpdated.getProperties().getDescription());
        assertEquals(folderTitle, folderUpdated.getProperties().getTitle());
        assertTrue(folderUpdated.getProperties().isVitalRecord());
        assertEquals(location, folderUpdated.getProperties().getLocation());
        assertNotNull(folderUpdated.getProperties().getReviewPeriod().getPeriodType());
        assertNotNull(folderUpdated.getProperties().getReviewPeriod().getExpression());


    }

    /**
     * Given that a record folder exists
     * When I use the API to delete the record folder
     * Then it deleted according to the normal rules governing the deletion of record folders
     */
    @Test
    (
        description = "Delete record folder"
    )
    public void deleteRecordFolder() throws Exception
    {
        String CATEGORY = CATEGORY_NAME + getRandomAlphanumeric();

        // Authenticate with admin user
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        FilePlanComponent category = createCategory(FILE_PLAN_ALIAS.toString(), CATEGORY);
        FilePlanComponent folder = createFolder(category.getId(), FOLDER_NAME);
        // Delete the Record folder
        filePlanComponentAPI.deleteFilePlanComponent(folder.getId());
        // Check the Response Status Code
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(NO_CONTENT);
        // Check the File Plan Component is not found
        filePlanComponentAPI.getFilePlanComponent(folder.getId());
        // Check the Response Status Code
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(NOT_FOUND);
    }

    /**
     * Given that a record category exists
     * And contains several record folders
     * When I use the APi to get the file plan component children for the existing category
     * Then I am provided with a list of the contained record folders
     * And their details
    */
    @Test
    (
        description = "List children of a category"
    )
    public void listFolders() throws Exception
    {

        String CATEGORY = CATEGORY_NAME + getRandomAlphanumeric();

        // Authenticate with admin user
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        FilePlanComponent category = createCategory(FILE_PLAN_ALIAS.toString(), CATEGORY);

        // Add child olders
        ArrayList<FilePlanComponent> children = new ArrayList<FilePlanComponent>();
        for (int i = 0; i < NUMBER_OF_FOLDER; i++)
        {
            // Create a child
            FilePlanComponent child = createFolder(category.getId(),
                    getRandomAlphanumeric());
            assertNotNull(child.getId());
            children.add(child);
        }

        // Authenticate with admin user
        RestWrapper restWrapper = filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // List children from API
        FilePlanComponentsCollection apiChildren = filePlanComponentAPI.listChildComponents(category.getId());

        // Check status code
        restWrapper.assertStatusCodeIs(OK);

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

                        // Is parent Id set correctly
                        assertEquals(filePlanComponent.getParentId(), category.getId());
                        assertFalse(filePlanComponent.isIsFile());

                        // Boolean properties related to node type
                        assertTrue(filePlanComponent.isIsRecordFolder());
                        assertFalse(filePlanComponent.isIsCategory());

                        assertEquals(createdComponent.getName(), filePlanComponent.getName());
                        assertEquals(createdComponent.getNodeType(), filePlanComponent.getNodeType());

                    } catch (NoSuchElementException e)
                    {
                        fail("No child element for " + filePlanComponent.getId());
                    }
                }
            );

    }
    @AfterClass (alwaysRun = true)
    public void tearDown() throws Exception
    {
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        filePlanComponentAPI.listChildComponents(FILE_PLAN_ALIAS.toString()).getEntries().forEach(filePlanComponentEntry ->
        {
            try
            {
                filePlanComponentAPI.deleteFilePlanComponent(filePlanComponentEntry.getFilePlanComponent().getId());
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }



}
