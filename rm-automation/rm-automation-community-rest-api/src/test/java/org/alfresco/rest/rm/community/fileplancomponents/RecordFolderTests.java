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
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PATH;
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

import java.time.LocalDateTime;
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
        FilePlanComponent filePlanComponent = createCategory(FILE_PLAN_ALIAS.toString(), CATEGORY);

        FilePlanComponent recordFolder = FilePlanComponent.builder()
                .name(FOLDER_NAME)
                .nodeType(RECORD_FOLDER_TYPE.toString())
                .properties(FilePlanComponentProperties.builder()
                        .title(FOLDER_TITLE)
                        .build())
                .build();

        // Create the record folder
        FilePlanComponent folder = filePlanComponentAPI.createFilePlanComponent(recordFolder, filePlanComponent.getId());


        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(CREATED);

        // Check folder has been created  within the category created
        assertEquals(filePlanComponent.getId(),folder.getParentId());
        // Verify the returned properties for the file plan component - record folder
        assertFalse(folder.getIsCategory());
        assertFalse(folder.getIsFile());
        assertTrue(folder.getIsRecordFolder());

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
        FilePlanComponent recordFolder = FilePlanComponent.builder()
                .name(FOLDER_NAME)
                .nodeType(RECORD_FOLDER_TYPE.toString())
                .properties(FilePlanComponentProperties.builder()
                                .title(FOLDER_TITLE)
                                .build())
                .build();

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
        String CATEGORY = CATEGORY_NAME + getRandomAlphanumeric();
        // Authenticate with admin user
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        FilePlanComponent category = createCategory(FILE_PLAN_ALIAS.toString(), CATEGORY);
        FilePlanComponent folder = createFolder(category.getId(),FOLDER_NAME);

        FilePlanComponent folderDetails = filePlanComponentAPI.withParams("include="+IS_CLOSED).getFilePlanComponent(folder.getId());

        // Verify the returned properties for the file plan component - record folder
        assertEquals(RECORD_FOLDER_TYPE.toString(),folderDetails.getNodeType());
        assertTrue(folderDetails.getIsRecordFolder());
        assertFalse(folderDetails.getIsCategory());
        assertFalse(folderDetails.getIsFile());
        assertFalse(folderDetails.getIsClosed());

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
        //Create a record category
        FilePlanComponent category = createCategory(FILE_PLAN_ALIAS.toString(), CATEGORY);

        //Create a record folder
        FilePlanComponent folder = createFolder(category.getId(), FOLDER_NAME);

        // Create record category first
        String folderDescription = "The folder description is updated" + getRandomAlphanumeric();
        String folderName = "The folder name is updated" + getRandomAlphanumeric();
        String folderTitle = "Update title " + getRandomAlphanumeric();
        String location = "Location"+getRandomAlphanumeric();

        //Create the file plan component properties to update
        FilePlanComponent recordFolder = FilePlanComponent.builder()
                .name(folderName)
                .properties(FilePlanComponentProperties.builder()
                                .title(folderTitle)
                                .description(folderDescription)
                                .vitalRecord(true)
                                .reviewPeriod(new ReviewPeriod("month","1"))
                                .location(location)
                                .build())
                .build();

        // Update the record category
        FilePlanComponent folderUpdated = filePlanComponentAPI.updateFilePlanComponent(recordFolder, folder.getId());

        // Check the Response Status Code
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(OK);

        // Verify the returned properties for the file plan component - record folder
        assertEquals(folderName, folderUpdated.getName());
        assertEquals(folderDescription, folderUpdated.getProperties().getDescription());
        assertEquals(folderTitle, folderUpdated.getProperties().getTitle());
        assertTrue(folderUpdated.getProperties().getVitalRecord());
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
        // Create the record category
        FilePlanComponent category = createCategory(FILE_PLAN_ALIAS.toString(), CATEGORY);

        // Create the record folder
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
                        assertFalse(filePlanComponent.getIsFile());

                        // Boolean properties related to node type
                        assertTrue(filePlanComponent.getIsRecordFolder());
                        assertFalse(filePlanComponent.getIsCategory());

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


    /**
     * Given that I want to create a record folder
     * When I use the API with the relativePath
     * Then the categories specified in the relativePath that don't exist are created within the record folder
     *
     * Containers in the relativePath that do not exist are created before the node is created
     */
    @Test
    (
        description = "Create a folder based on the relativePath. " +
            "Containers in the relativePath that do not exist are created before the node is created"
    )
    public void createFolderWithRelativePath() throws Exception
    {
        //RelativePath specifies the container structure to create relative to the node nodeId.
        String RELATIVE_PATH = LocalDateTime.now().getYear()+"/"+ LocalDateTime.now().getMonth()+"/"+ LocalDateTime.now().getDayOfMonth();

        // Authenticate with admin user
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        //The record folder to be created
        FilePlanComponent recordFolder = FilePlanComponent.builder()
                                                          .name(FOLDER_NAME)
                                                          .nodeType(RECORD_FOLDER_TYPE.toString())
                                                          .relativePath(RELATIVE_PATH)
                                                          .build();

        // Create the record folder
        FilePlanComponent folder = filePlanComponentAPI.withParams("include="+ PATH).createFilePlanComponent(recordFolder,FILE_PLAN_ALIAS.toString());
        //Check the API response code
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(CREATED);

        // Verify the returned properties for the file plan component - record folder
        assertFalse(folder.getIsCategory());
        assertFalse(folder.getIsFile());
        assertTrue(folder.getIsRecordFolder());

        //Check the path return contains the RELATIVE_PATH
        assertTrue(folder.getPath().getName().contains(RELATIVE_PATH));
        //check the parent is a category
        assertTrue(filePlanComponentAPI.getFilePlanComponent(folder.getParentId()).getIsCategory());
        //New Relative Path only a part of containers need to be created before the recrod folder
        String NEW_RELATIVE_PATH = LocalDateTime.now().getYear() + "/" + LocalDateTime.now().getMonth() + "/" +( LocalDateTime.now().getDayOfMonth()+1);
        //The record folder to be created
        FilePlanComponent recordFolder2 = FilePlanComponent.builder()
                                                          .name(FOLDER_NAME)
                                                          .nodeType(RECORD_FOLDER_TYPE.toString())
                                                          .relativePath(NEW_RELATIVE_PATH)
                                                          .build();

        // Create the record folder
        FilePlanComponent folder2 = filePlanComponentAPI.withParams("include=" + PATH).createFilePlanComponent(recordFolder2, FILE_PLAN_ALIAS.toString());
        //Check the API response code
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(CREATED);

        // Verify the returned properties for the file plan component - record folder
        assertFalse(folder2.getIsCategory());
        assertFalse(folder2.getIsFile());
        assertTrue(folder2.getIsRecordFolder());
        //Check the path return contains the NEW_RELATIVE_PATH
        assertTrue(folder2.getPath().getName().contains(NEW_RELATIVE_PATH));

        //check the parent is a category
        assertTrue(filePlanComponentAPI.getFilePlanComponent(folder.getParentId()).getIsCategory());

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
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }



}
