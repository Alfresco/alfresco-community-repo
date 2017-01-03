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

import static java.util.Arrays.asList;

import static org.alfresco.rest.rm.community.base.AllowableOperations.CREATE;
import static org.alfresco.rest.rm.community.base.AllowableOperations.DELETE;
import static org.alfresco.rest.rm.community.base.AllowableOperations.UPDATE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.ALLOWABLE_OPERATIONS;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.base.TestData;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.community.requests.igCoreAPI.FilePlanComponentAPI;
import org.alfresco.rest.rm.community.requests.igCoreAPI.RMSiteAPI;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.testng.annotations.Test;

/**
 * This class contains the tests for
 * the File Plan CRUD API
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class FilePlanTests extends BaseRMRestTest
{
    /**
     * Given that the RM site doesn't exist
     * When I use the API to get the File Plan/Holds/Unfiled Records Container/Transfers
     * Then I get the 404 response code
     */
    @Test
    (
        description = "Check the GET response code when the RM site doesn't exist",
        dataProviderClass = TestData.class,
        dataProvider = "getContainers"
    )
    public void getFilePlanComponentWhenRMIsNotCreated(String filePlanComponentAlias) throws Exception
    {
        RMSiteAPI rmSiteAPI = getRestAPIFactory().getRMSiteAPI();

        // Check RM Site Exist
        if (rmSiteAPI.existsRMSite())
        {
            // Delete RM Site
            rmSiteAPI.deleteRMSite();
        }

        // Get the file plan component
        getRestAPIFactory().getFilePlanComponentsAPI().getFilePlanComponent(filePlanComponentAlias);

        //check the response code is NOT_FOUND
        assertStatusCode(NOT_FOUND);
    }

    /**
     * Given that a file plan exists
     * When I ask the API for the details of the file plan
     * Then I am given the details of the file plan
     */
    @Test
    (
        description = "Check the GET response for special file plan components when the RM site exit",
        dataProviderClass = TestData.class,
        dataProvider = "getContainersAndTypes"
    )
    public void getFilePlanComponentWhenRMIsCreated(String filePlanComponentAlias, String filePlanComponentType) throws Exception
    {
        // Create RM Site if doesn't exist
        createRMSiteIfNotExists();

        // Get the file plan special container
        FilePlanComponent filePlanComponent = getRestAPIFactory().getFilePlanComponentsAPI().getFilePlanComponent(filePlanComponentAlias);

        // Check the response code
        assertStatusCode(OK);

        // Check the response contains the right node type
        assertEquals(filePlanComponent.getNodeType(), filePlanComponentType);
    }

    /**
     * Given that a file plan exists
     * When I ask the API for the details of the file plan to include the allowableOperations property
     * Then I am given the allowableOperations property with the update and create operations.
     */
    @Test
    (
        description = "Check the allowableOperations list returned ",
        dataProviderClass = TestData.class,
        dataProvider = "getContainers"
    )
    public void includeAllowableOperations(String specialContainerAlias) throws Exception
    {
        // Create RM Site if doesn't exist
        createRMSiteIfNotExists();

        // Get the file plan special containers with the optional parameter allowableOperations
        FilePlanComponent filePlanComponent = getRestAPIFactory().getFilePlanComponentsAPI().getFilePlanComponent(specialContainerAlias, "include=" + ALLOWABLE_OPERATIONS);

        // Check the list of allowableOperations returned
        if(specialContainerAlias.equals(TRANSFERS_ALIAS))
        {
            assertTrue(filePlanComponent.getAllowableOperations().containsAll(asList(UPDATE)),
                    "Wrong list of the allowable operations is return" + filePlanComponent.getAllowableOperations().toString());
        }
        else
        {
            assertTrue(filePlanComponent.getAllowableOperations().containsAll(asList(UPDATE, CREATE)),
                "Wrong list of the allowable operations is return" + filePlanComponent.getAllowableOperations().toString());
        }

        // Check the list of allowableOperations doesn't contains DELETE operation
        assertFalse(filePlanComponent.getAllowableOperations().contains(DELETE),
                "The list of allowable operations contains delete option" + filePlanComponent.getAllowableOperations().toString());
    }

    /**
     * Given that a file plan exists
     * When I ask the API to modify the details of the file plan
     * Then the details of the file are modified
     * Note: the details of the file plan are limited to title and description.
     */
    @Test
    @Bug (id = "RM-4295")
    public void updateFilePlan() throws Exception
    {
        String FILE_PLAN_DESCRIPTION = "Description updated " + getRandomAlphanumeric();
        String FILE_PLAN_TITLE = "Title updated " + getRandomAlphanumeric();

        // Create RM Site if doesn't exist
        createRMSiteIfNotExists();

        // Build object for updating the filePlan
        FilePlanComponent filePlanComponent = FilePlanComponent.builder()
            .properties(FilePlanComponentProperties.builder()
                            .title(FILE_PLAN_TITLE)
                            .description(FILE_PLAN_DESCRIPTION)
                            .build())
            .build();

        // Update the record category
        FilePlanComponent renamedFilePlanComponent = getRestAPIFactory().getFilePlanComponentsAPI().updateFilePlanComponent(filePlanComponent, FILE_PLAN_ALIAS);

        // Verify the response status code
        assertStatusCode(OK);

        // Verify the returned description field for the file plan component
        assertEquals(renamedFilePlanComponent.getProperties().getDescription(), FILE_PLAN_DESCRIPTION);

        // Verify the returned title field for the file plan component
        assertEquals(renamedFilePlanComponent.getProperties().getTitle(), FILE_PLAN_TITLE);
    }

    /**
     * Given that a file plan exists
     * When I ask the API to delete the file plan
     * Then the 422 response code is returned.
     */
    @Test
    (
        description = "Check the response code when deleting the special file plan components",
        dataProviderClass = TestData.class,
        dataProvider = "getContainers"
    )
    public void deleteFilePlanSpecialComponents(String filePlanComponentAlias) throws Exception
    {
        // Create RM Site if doesn't exist
        createRMSiteIfNotExists();

        // Delete the file plan component
        getRestAPIFactory().getFilePlanComponentsAPI().deleteFilePlanComponent(filePlanComponentAlias);

        // Check the DELETE response status code
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    /**
     * Given that a file plan exists and I am a non RM user
     * When I ask the API to delete the file plan
     * Then the 403 response code is returned.
     */
    @Test
    (
        description = "Check the response code when deleting the special file plan components with non RM user",
        dataProviderClass = TestData.class,
        dataProvider = "getContainers"
    )
    public void deleteFilePlanSpecialComponentsNonRMUser(String filePlanComponentAlias) throws Exception
    {
        // Create RM Site if doesn't exist
        createRMSiteIfNotExists();

        // Create a random user
        UserModel nonRMuser = getDataUser().createRandomTestUser("testUser");

        // Delete the file plan component
        getRestAPIFactory().getFilePlanComponentsAPI(nonRMuser).deleteFilePlanComponent(filePlanComponentAlias);

        // Check the DELETE response status code
        assertStatusCode(FORBIDDEN);
    }

    /**
     * Given that RM site exists
     * When I ask to create the file plan
     * Then the 403 response code is returned.
     */
    @Test
    (
        description = "Check the response code when creating the special file plan components",
        dataProviderClass = TestData.class,
        dataProvider = "getContainersAndTypes"
    )
    @Bug(id = "RM-4296")
    public void createFilePlanSpecialContainerWhenExists(String filePlanComponentAlias, String filePlanComponentType) throws Exception
    {
        // Create RM Site if doesn't exist
        createRMSiteIfNotExists();

        // Get the RM site ID
        String rmSiteId = getRestAPIFactory().getRMSiteAPI().getSite().getGuid();
        String name = filePlanComponentAlias + getRandomAlphanumeric();

        // Build the file plan root properties
        FilePlanComponent filePlanComponent = FilePlanComponent.builder()
                .name(name)
                .nodeType(filePlanComponentType)
                .properties(FilePlanComponentProperties.builder()
                                .build())
                .build();

        FilePlanComponentAPI filePlanComponentsAPI = getRestAPIFactory().getFilePlanComponentsAPI();

        // Create the special containers into RM site - parent folder
        filePlanComponentsAPI.createFilePlanComponent(filePlanComponent, rmSiteId);
        assertStatusCode(UNPROCESSABLE_ENTITY);

        // Create the special containers into RM site - parent folder
        filePlanComponentsAPI.createFilePlanComponent(filePlanComponent, FILE_PLAN_ALIAS);
        assertStatusCode(UNPROCESSABLE_ENTITY);

        // Create the special containers into the root of special containers containers
        filePlanComponentsAPI.createFilePlanComponent(filePlanComponent, filePlanComponentAlias);
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    /**
     * Given that RM site  exists
     * When a non-RM user ask the API for the details of the file plan
     * Then the status code 403 (Permission denied) is return
     */
    @Test
    (
        description = "Check the response code when the RM site containers are get with non rm users",
        dataProviderClass = TestData.class,
        dataProvider = "getContainers"
    )
    public void getSpecialFilePlanComponentsWithNonRMuser(String filePlanComponentAlias) throws Exception
    {
        // Create RM Site if doesn't exist
        createRMSiteIfNotExists();

        // Create a random user
        UserModel nonRMuser = getDataUser().createRandomTestUser("testUser");

        // Get the special file plan components
        getRestAPIFactory().getFilePlanComponentsAPI(nonRMuser).getFilePlanComponent(filePlanComponentAlias);

        // Check the response status code is FORBIDDEN
        assertStatusCode(FORBIDDEN);
    }
}
