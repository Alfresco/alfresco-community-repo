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

import static org.alfresco.rest.rm.community.base.TestData.CATEGORY_NAME;
import static org.alfresco.rest.rm.community.base.TestData.FOLDER_NAME;
import static org.alfresco.rest.rm.community.base.TestData.FOLDER_TITLE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.IS_CLOSED;
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
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.rm.community.base.BaseRestTest;
import org.alfresco.rest.rm.community.base.TestData;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentsCollection;
import org.alfresco.rest.rm.community.model.fileplancomponents.ReviewPeriod;
import org.alfresco.rest.rm.community.requests.FilePlanComponentAPI;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.report.Bug;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * This class contains the tests for the
 * the Record Folder CRUD API
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class RecordFolderTests extends BaseRestTest
{
    @Autowired
    public FilePlanComponentAPI filePlanComponentAPI;

    @Autowired
    public DataUser dataUser;

    private static final int NUMBER_OF_FOLDERS= 5;
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
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        FilePlanComponent filePlanComponent=createCategory(FILE_PLAN_ALIAS.toString(), CATEGORY);

        FilePlanComponentProperties filePlanComponentProperties = new FilePlanComponentProperties(FOLDER_TITLE);
        FilePlanComponent recordFolder = new FilePlanComponent(FOLDER_NAME,RECORD_FOLDER_TYPE.toString(), filePlanComponentProperties);

        // Create the record folder
        FilePlanComponent folder = filePlanComponentAPI.createFilePlanComponent(recordFolder, filePlanComponent.getId());

        //filePlanComponentAPI.createFilePlanComponent(recordFolderProperties, filePlanComponent.getId());
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
        FilePlanComponent recordFolder= new FilePlanComponent(FOLDER_NAME,RECORD_FOLDER_TYPE.toString(),
                                             new FilePlanComponentProperties(FOLDER_TITLE));
        // Create a record folder
        filePlanComponentAPI.createFilePlanComponent(recordFolder, componentID);

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
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
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
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        FilePlanComponent category = createCategory(FILE_PLAN_ALIAS.toString(), CATEGORY);
        FilePlanComponent folder = createFolder(category.getId(), FOLDER_NAME);

        // Create record category first
        String folderDescription = "The folder description is updated" + getRandomAlphanumeric();
        String folderName= "The folder name is updated" + getRandomAlphanumeric();
        String folderTitle = "Update title " + getRandomAlphanumeric();
        String location="Location"+getRandomAlphanumeric();

        String review_period="month|1";

        FilePlanComponentProperties filePlanComponentProperties= new FilePlanComponentProperties(folderTitle, folderDescription);
        filePlanComponentProperties.setVitalRecord(true);
        filePlanComponentProperties.setReviewPeriod( new ReviewPeriod("month","1"));
        filePlanComponentProperties.setLocation(location);
        FilePlanComponent recordFolder = new FilePlanComponent(folderName,filePlanComponentProperties);
        // Update the record category
        FilePlanComponent folderUpdated = filePlanComponentAPI.updateFilePlanComponent(recordFolder, folder.getId());

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
        for (int i = 0; i < NUMBER_OF_FOLDERS; i++)
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

                    }
                    catch (NoSuchElementException e)
                    {
                        fail("No child element for " + filePlanComponent.getId());
                    }
                }
            );

    }
    //@AfterClass (alwaysRun = true)
    public void tearDown() throws Exception
    {
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        filePlanComponentAPI.listChildComponents(FILE_PLAN_ALIAS.toString()).getEntries().forEach(filePlanComponentEntry ->
        {
            try
            {
                filePlanComponentAPI.deleteFilePlanComponent(filePlanComponentEntry.getFilePlanComponent().getId());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }



}
