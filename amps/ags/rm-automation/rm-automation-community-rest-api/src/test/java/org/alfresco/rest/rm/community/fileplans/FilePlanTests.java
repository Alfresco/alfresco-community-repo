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
package org.alfresco.rest.rm.community.fileplans;

import static java.util.Arrays.asList;

import static org.alfresco.rest.rm.community.base.AllowableOperations.CREATE;
import static org.alfresco.rest.rm.community.base.AllowableOperations.DELETE;
import static org.alfresco.rest.rm.community.base.AllowableOperations.UPDATE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.ALLOWABLE_OPERATIONS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.FILE_PLAN_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.TRANSFER_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.TRANSFER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_FILING;
import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_MANAGER;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.base.DataProviderClass;
import org.alfresco.rest.rm.community.model.fileplan.FilePlan;
import org.alfresco.rest.rm.community.model.fileplan.FilePlanProperties;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryCollection;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryProperties;
import org.alfresco.rest.rm.community.requests.gscore.api.RMSiteAPI;
import org.alfresco.utility.constants.ContainerName;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class contains the tests for the File Plan CRUD API
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class FilePlanTests extends BaseRMRestTest
{
    //** Number of children (for children creation test) */
    private static final int NUMBER_OF_CHILDREN = 10;

    /**
     * Data Provider with:
     * with the object types not allowed as children for a record category
     *
     * @return file plan component alias
     */
    @DataProvider
    public static Object[][] childrenNotAllowedForFilePlan()
    {
        return new String[][] {
                { FILE_PLAN_TYPE },
                { TRANSFER_CONTAINER_TYPE },
                { UNFILED_CONTAINER_TYPE },
                { UNFILED_RECORD_FOLDER_TYPE },
                { TRANSFER_TYPE },
                { CONTENT_TYPE },
                { NON_ELECTRONIC_RECORD_TYPE},
                { RECORD_FOLDER_TYPE}
        };
    }

    /**
     * <pre>
     * Given that the RM site doesn't exist
     * When I use the API to get the File Plan
     * Then I get the 404 response code
     * </pre>
     */
    @Test (priority = 1)
    // Set priority to 1 in order for this test to run last one in this class. The rm site is created only once at the
    // beginning of the class and because this test deletes the rm site, the other tests might be affected
    public void getFilePlanWhenRMIsNotCreated()
    {
        RMSiteAPI rmSiteAPI = getRestAPIFactory().getRMSiteAPI();

        // Check RM Site Exist
        if (rmSiteAPI.existsRMSite())
        {
            // Delete RM Site
            rmSiteAPI.deleteRMSite();
        }
        //get file plan
        getRestAPIFactory().getFilePlansAPI().getFilePlan(FILE_PLAN_ALIAS);

        // Check the response code is NOT_FOUND
        assertStatusCode(NOT_FOUND);
    }

    /**
     * <pre>
     * Given that a file plan exists
     * When I ask the API for the details of the file plan
     * Then I am given the details of the file plan
     * </pre>
     */
    @Test
    public void getFilePlanWhenRMIsCreated()
    {
        // Create RM Site if doesn't exist
        createRMSiteIfNotExists();

        FilePlan filePlan = getRestAPIFactory().getFilePlansAPI().getFilePlan(FILE_PLAN_ALIAS);

        // Check the response code
        assertStatusCode(OK);
        //check file plan details
        assertEquals(FILE_PLAN_TYPE, filePlan.getNodeType());
        assertEquals(ContainerName.documentLibrary.toString(), filePlan.getName());
    }

    /**
     * <pre>
     * Given that a file plan exists
     * When I ask the API for the details of the file plan to include the allowableOperations property
     * Then I am given the allowableOperations property with the update and create operations.
     * </pre>
     */
    @Test
    public void includeAllowableOperations()
    {
        // Check the list of allowableOperations returned
        FilePlan filePlan = getRestAPIFactory().getFilePlansAPI().getFilePlan(FILE_PLAN_ALIAS, "include=" + ALLOWABLE_OPERATIONS);

        assertTrue(filePlan.getAllowableOperations().containsAll(asList(UPDATE, CREATE)),
                "Wrong list of the allowable operations is return" + filePlan.getAllowableOperations().toString());

        // Check the list of allowableOperations doesn't contain DELETE operation
        assertFalse(filePlan.getAllowableOperations().contains(DELETE),
                "The list of allowable operations contains delete option" + filePlan.getAllowableOperations().toString());
    }

    /**
     * <pre>
     * Given that RM site exists
     * When a non-RM user asks the API for the details of the file plan
     * Then the status code 403 (Permission denied) is return
     * </pre>
     */
    @Test
    public void getFilePlanWithNonRMuser()
    {
        // Create a random user
        UserModel nonRMuser = getDataUser().createRandomTestUser("testUser");

        // Get the special file plan components
        getRestAPIFactory().getFilePlansAPI(nonRMuser).getFilePlan(FILE_PLAN_ALIAS);

        // Check the response status code is FORBIDDEN
        assertStatusCode(FORBIDDEN);
    }

    /**
     * Given that a file plan exists
     * When I ask the API to modify the details of the file plan
     * Then the details of the file are modified
     * Note: the details of the file plan are limited to title and description.
     */
    @Test
    @Bug (id = "RM-4295")
    public void updateFilePlan()
    {
        String FILE_PLAN_DESCRIPTION = "Description updated " + getRandomAlphanumeric();
        String FILE_PLAN_TITLE = "Title updated " + getRandomAlphanumeric();

        // Build object for updating the filePlan
        FilePlan filePlanComponent = FilePlan.builder()
                                             .properties(FilePlanProperties.builder()
                                                                           .title(FILE_PLAN_TITLE)
                                                                           .description(FILE_PLAN_DESCRIPTION)
                                                                           .build())
                                             .build();
        // Create a random user
        UserModel nonRMuser = getDataUser().createRandomTestUser("testUser");

        // Update the file plan
         getRestAPIFactory().getFilePlansAPI(nonRMuser).updateFilePlan(filePlanComponent, FILE_PLAN_ALIAS);

        //Check the response status code is FORBIDDEN
        assertStatusCode(FORBIDDEN);

        // Update the file plan
        FilePlan renamedFilePlan = getRestAPIFactory().getFilePlansAPI().updateFilePlan(filePlanComponent, FILE_PLAN_ALIAS);

        // Verify the response status code
        assertStatusCode(OK);

        // Verify the returned description field for the file plan component
        assertEquals(FILE_PLAN_DESCRIPTION, renamedFilePlan.getProperties().getDescription());

        // Verify the returned title field for the file plan component
        assertEquals(FILE_PLAN_TITLE, renamedFilePlan.getProperties().getTitle());
    }

    /**
     * Given that a file plan exists
     * When I ask the API to modify the name of the file plan
     * Then a error is returned (422 response code)
     */
    @Test
    @Bug (id = "RM-4295")
    public void updateFilePlanName()
    {
        // Build object for updating the filePlan
        FilePlan filePlanComponent = FilePlan.builder()
                                             .name(getRandomName("File Plan name updated "))
                                             .build();

        // Update the file plan
        getRestAPIFactory().getFilePlansAPI().updateFilePlan(filePlanComponent, FILE_PLAN_ALIAS);

        // Verify the response status code
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

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
        dataProviderClass = DataProviderClass.class,
        dataProvider = "categoryTypes"
    )
    public void createFilePlanChildren(String nodeType)
    {
        String categoryName = "Category name " + getRandomAlphanumeric();
        String categoryTitle = "Category title " + getRandomAlphanumeric();

        // Create the root record category
        RecordCategory recordCategory = RecordCategory.builder()
                                                      .name(categoryName)
                                                      .properties(RecordCategoryProperties.builder()
                                                                           .title(categoryTitle)
                                                                           .build())
                                                      .nodeType(nodeType)
                                                      .build();
        RecordCategory rootRecordCategory = getRestAPIFactory().getFilePlansAPI()
                                                               .createRootRecordCategory(recordCategory,FILE_PLAN_ALIAS);

        // Verify the status code
        assertStatusCode(CREATED);

        assertEquals(rootRecordCategory.getName(), categoryName);
        assertEquals(rootRecordCategory.getNodeType(), RECORD_CATEGORY_TYPE);

        assertEquals(rootRecordCategory.getCreatedByUser().getId(), getAdminUser().getUsername());

        // Verify the returned root record category properties
        RecordCategoryProperties rootRecordCategoryProperties = rootRecordCategory.getProperties();
        assertEquals(rootRecordCategoryProperties.getTitle(), categoryTitle);
        assertNotNull(rootRecordCategoryProperties.getIdentifier());
    }

    /**
     * <pre>
     * Given a root category
     * When I ask the API to create a root category having the same name
     * Then  the response code received is 409 - name clashes with an existing node
     *</pre>
     * <pre>
     * Given a root category
     * When I ask the API to create a root category having the same name  with autoRename parameter on true
     * Then the record category is created the record category has a unique name by adding an integer suffix
     * </pre>
     */
    @Test
    @Bug(id = "RM-5116")
    public void createDuplicateCategories()
    {
        String categoryName = "Category name " + getRandomAlphanumeric();
        String categoryTitle = "Category title " + getRandomAlphanumeric();


        // Create the root record category
        RecordCategory recordCategory = RecordCategory.builder()
                                                      .name(categoryName)
                                                      .properties(RecordCategoryProperties.builder()
                                                                           .title(categoryTitle)
                                                                           .build())
                                                      .build();
        // Create the root record category
        RecordCategory rootRecordCategory = getRestAPIFactory().getFilePlansAPI().createRootRecordCategory(recordCategory,FILE_PLAN_ALIAS);

        // Verify the status code
        assertStatusCode(CREATED);
        assertEquals(rootRecordCategory.getName(), categoryName);

        // Create the same root record category
        getRestAPIFactory().getFilePlansAPI().createRootRecordCategory(recordCategory, FILE_PLAN_ALIAS);

        // Verify the status code
        assertStatusCode(CONFLICT);

        //create the same category with autoRename parameter on true
        RecordCategory rootRecordCategoryAutoRename = getRestAPIFactory().getFilePlansAPI()
                                                                         .createRootRecordCategory(recordCategory, FILE_PLAN_ALIAS,"autoRename=true");

        // Verify the status code
        assertStatusCode(CREATED);
        assertNotEquals(rootRecordCategoryAutoRename.getName(), categoryName);
        assertTrue(rootRecordCategoryAutoRename.getName().startsWith(categoryName));
    }

    @Test
    public void listFilePlanChildren()
    {
        //delete all the root categories
        getRestAPIFactory().getFilePlansAPI().getRootRecordCategories(FILE_PLAN_ALIAS).getEntries().forEach(recordCategoryEntry ->
                deleteRecordCategory(recordCategoryEntry.getEntry().getId()));
        // Add child folders
        ArrayList<RecordCategory> children = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_CHILDREN; i++)
        {
            String categoryName = "Category name " + getRandomAlphanumeric();
            String categoryTitle = "Category title " + getRandomAlphanumeric();
            // Create a record folder
            RecordCategory recordCategory = createRootCategory(categoryName, categoryTitle);
            assertNotNull(recordCategory.getId());
            children.add(recordCategory);
        }

        // Get record category children from API
        RecordCategoryCollection recordCategoryChildren = getRestAPIFactory().getFilePlansAPI()
                                                                             .getRootRecordCategories(FILE_PLAN_ALIAS, "include=aspects,properties");

        // Check status code
        assertStatusCode(OK);

        // Check children against created list
        recordCategoryChildren.getEntries().forEach(c ->
                {
                    RecordCategory recordCategoryChild = c.getEntry();
                    String recordCategoryChildId = recordCategoryChild.getId();
                    assertNotNull(recordCategoryChildId);
                    logger.info("Checking child " + recordCategoryChildId);

                    try
                    {
                        // Find this child in created children list
                        RecordCategory createdComponent = children.stream()
                                                                       .filter(child -> child.getId().equals(recordCategoryChildId))
                                                                       .findFirst()
                                                                       .orElseThrow();

                        // Created by
                        assertEquals(recordCategoryChild.getCreatedByUser().getId(), getAdminUser().getUsername());

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
     * Given that RM site is created
     * When I use the API to create invalid types inside a file plan
     * Then the node type provided is converted to a record category
     * </pre>
     */
    @Test
    (
        description = "Create a record folder/unfiled container/unfiled folder/record/file plan container",
        dataProvider = "childrenNotAllowedForFilePlan"
    )
    public void createChildrenNotAllowedInFilePlan(String nodeType)
    {
        String componentName = "Component" + getRandomAlphanumeric();

        // Create the root record category
        RecordCategory component = RecordCategory.builder()
                                                      .name(componentName)
                                                      .nodeType(nodeType)
                                                 .build();
        // Create the invalid node type
        RecordCategory rootRecordCategory = getRestAPIFactory().getFilePlansAPI()
                                                               .createRootRecordCategory(component, FILE_PLAN_ALIAS);
        //check the response status code
        assertStatusCode(CREATED);
        assertEquals(rootRecordCategory.getName(), componentName);
        assertEquals(rootRecordCategory.getNodeType(), RECORD_CATEGORY_TYPE);

        assertEquals(rootRecordCategory.getCreatedByUser().getId(), getAdminUser().getUsername());

        // Verify the returned root record category properties
        assertNotNull(rootRecordCategory.getProperties().getIdentifier());
    }

    @Test
    public  void listChildrenUserPermission()
    {
        // Create a random user
        UserModel managerUser = getDataUser().createRandomTestUser("managerUser");

        // Add child folders
        ArrayList<RecordCategory> children = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_CHILDREN/2; i++)
        {
            String categoryName = "Category name " + getRandomAlphanumeric();
            String categoryTitle = "Category title " + getRandomAlphanumeric();
            // Create a record folder
            RecordCategory recordCategory = createRootCategory(categoryName, categoryTitle);
            assertNotNull(recordCategory.getId());
            children.add(recordCategory);
        }

        getRestAPIFactory().getRMUserAPI().assignRoleToUser(managerUser.getUsername(), ROLE_RM_MANAGER.roleId);
        // Get record category children from API
        getRestAPIFactory().getFilePlansAPI(managerUser).getRootRecordCategories(FILE_PLAN_ALIAS)
                           .assertThat().entriesListIsEmpty().assertThat().paginationExist();

        ArrayList<RecordCategory> childrenManager = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_CHILDREN / 2; i++)
        {
            String categoryName = "Category for manager " + getRandomAlphanumeric();
            String categoryTitle = "Category for manager " + getRandomAlphanumeric();
            // Create a record folder
            RecordCategory recordCategory = createRootCategory(categoryName, categoryTitle);
            assertNotNull(recordCategory.getId());
            getRestAPIFactory().getRMUserAPI().addUserPermission(recordCategory.getId(), managerUser, PERMISSION_FILING);
            childrenManager.add(recordCategory);
        }
        // Get record category children from API
        RecordCategoryCollection recordCategoryChildren = getRestAPIFactory().getFilePlansAPI(managerUser).getRootRecordCategories(FILE_PLAN_ALIAS);

        //Check children against created list
        recordCategoryChildren.getEntries().forEach(c ->
        {
            RecordCategory recordCategoryChild = c.getEntry();
            String recordCategoryChildId = recordCategoryChild.getId();
            assertNotNull(recordCategoryChildId);
            logger.info("Checking child " + recordCategoryChildId);

            try
            {
                // Find this child in created children list
                assertTrue(childrenManager.stream()
                                          .anyMatch(child -> child.getId().equals(recordCategoryChildId))
                             );
                assertFalse(children.stream()
                                    .anyMatch(child -> child.getId().equals(recordCategoryChildId))

                             );
            } catch (NoSuchElementException e)
            {
                fail("No child element for " + recordCategoryChildId);
            }
        }
        );
    }


}
