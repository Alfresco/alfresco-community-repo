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
import static org.alfresco.com.fileplancomponents.FilePlanComponentFields.PROPERTIES_DESCRIPTION;
import static org.alfresco.com.fileplancomponents.FilePlanComponentFields.PROPERTIES_TITLE;
import static org.jglue.fluentjson.JsonBuilderFactory.buildObject;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import com.google.gson.JsonObject;

import org.alfresco.com.fileplancomponents.FilePlanComponentAlias;
import org.alfresco.com.fileplancomponents.FilePlanComponentType;
import org.alfresco.rest.BaseRestTest;
import org.alfresco.rest.TestData;
import org.alfresco.rest.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.requests.FilePlanComponentApi;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * This class contains the test for testing
 * the File Plan CRUD API
 *
 * @author Rodica Sutu
 * @since 1.0
 */
public class FilePlanTests extends BaseRestTest
{
    @Autowired
    private FilePlanComponentApi filePlanComponentApi;

    @Autowired
    private DataUser dataUser;

    /**
     * Given that the RM site doesn't exist
     * When I use the API to get the File Plan/holds/unfiled/transfers
     * Then I get the 404 response code
     */
    @Test
    (
        description = "Check the GET response code when the RM site doesn't exist",
        dataProviderClass = TestData.class,
        dataProvider = "getContainers"
    )
    public void getFilePlanComponentWhenRMIsNotCreated(String filePlanAlias) throws Exception
    {
        //check RM Site Exist
        if (siteRMExist())
        {
            //delete RM Site
            rmSiteAPI.deleteRMSite();
        }
        filePlanComponentApi.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        //get the file plan component
        filePlanComponentApi.getFilePlanComponent(filePlanAlias.toString());
        //check the response code is NOT_FOUND
        filePlanComponentApi.usingRestWrapper().assertStatusCodeIs(NOT_FOUND);
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
    public void getFilePlanComponentWhenRMIsCreated(FilePlanComponentAlias filePlanAlias, FilePlanComponentType rmType) throws Exception
    {
        //create RM Site if doesn't exist
        createRMSiteIfNotExists();
        //authenticate with admin user
        filePlanComponentApi.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        //get the file plan special container
        FilePlanComponent filePlanComponent=filePlanComponentApi.getFilePlanComponent(filePlanAlias.toString());
        //check the response code
        filePlanComponentApi.usingRestWrapper().assertStatusCodeIs(OK);
        //check the response contains the right node type
        assertEquals(filePlanComponent.getNodeType(), rmType.toString());
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
        //create RM Site if doesn't exist
        createRMSiteIfNotExists();
        filePlanComponentApi.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        //GET the file plan special containers with the optional parameter allowableOperations
        FilePlanComponent filePlanComponent = filePlanComponentApi.withParams("include=allowableOperations").getFilePlanComponent(specialContainerAlias);
        //Check the list of allowableOperations returned
        assertTrue(filePlanComponent.getAllowableOperations().containsAll(Arrays.asList("update", "create")),
                "Wrong list of the allowable operations is return" + filePlanComponent.getAllowableOperations().toString());
        //check the list of allowableOperations doesn't contains DELETE operation
        assertFalse(filePlanComponent.getAllowableOperations().contains("delete"),
                "The list of allowable operations contains delete option"+ filePlanComponent.getAllowableOperations().toString());
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
        String FILE_PLAN_DESCRIPTION="Description updated " +randomUUID().toString().substring(0, 4);
        String FILE_PLAN_TITLE = "Title updated " + randomUUID().toString().substring(0, 4);
        //create RM Site if doesn't exist
        createRMSiteIfNotExists();
        filePlanComponentApi.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        //Build the file plan root properties
        JsonObject filePlanProperties = buildObject()
                .addObject(PROPERTIES)
                    .add(PROPERTIES_TITLE, FILE_PLAN_TITLE)
                    .add(PROPERTIES_DESCRIPTION, FILE_PLAN_DESCRIPTION)
                    .end()
                .getJson();


        // Update the record category
        FilePlanComponent renamedFilePlanComponent = filePlanComponentApi.updateFilePlanComponent(filePlanProperties,FILE_PLAN_ALIAS.toString());

        // Verify the response status code
        filePlanComponentApi.usingRestWrapper().assertStatusCodeIs(OK);

        // Verify the returned description field for the file plan component
        assertEquals(renamedFilePlanComponent.getProperties().getDescription(), FILE_PLAN_DESCRIPTION);
        // Verify the returned title field for the file plan component
        assertEquals(renamedFilePlanComponent.getProperties().getTitle(), FILE_PLAN_TITLE);
    }

    /**
     * Given that a file plan exists
     * When I ask the API to delete the file plan
     * Then the 403 response code is returned.
     */
    @Test
    (
        description = "Check the response code when deleting the special file plan components",
        dataProviderClass = TestData.class,
        dataProvider = "getContainers"
    )
    public void deleteFilePlanSpecialComponents(String filePlanAlias) throws Exception
    {
        createRMSiteIfNotExists();
        filePlanComponentApi.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        //get the file plan component
        filePlanComponentApi.deleteFilePlanComponent(filePlanAlias.toString());
        //check the DELETE response status code
        filePlanComponentApi.usingRestWrapper().assertStatusCodeIs(FORBIDDEN);
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
    @Bug(id="RM-4296")
    public void createFilePlanSpecialContainerWhenExists(FilePlanComponentAlias filePlanAlias, FilePlanComponentType rmType) throws Exception
    {
        String rmSiteId=rmSiteAPI.getSite().getGuid();

        //create RM Site if doesn't exist
        createRMSiteIfNotExists();
        filePlanComponentApi.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        String COMPONENT_TITLE = filePlanAlias + randomUUID().toString().substring(0, 4);
        //Build the file plan root properties
        JsonObject componentProperties = buildObject()
                .add(NAME, COMPONENT_TITLE)
                .add(NODE_TYPE, rmType.toString())
                .getJson();
        //create the special containers into RM site - parent folder
        filePlanComponentApi.createFilePlanComponent(componentProperties, rmSiteId);
        filePlanComponentApi.usingRestWrapper().assertStatusCodeIs(FORBIDDEN);

        //create the special containers into RM site - parent folder
        filePlanComponentApi.createFilePlanComponent(componentProperties, FILE_PLAN_ALIAS.toString());
        filePlanComponentApi.usingRestWrapper().assertStatusCodeIs(FORBIDDEN);

        //create the special containers into the root of special containers containers
        filePlanComponentApi.createFilePlanComponent(componentProperties, filePlanAlias.toString());
        filePlanComponentApi.usingRestWrapper().assertStatusCodeIs(FORBIDDEN);
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
    public void getSpecialFilePlanComponentsWithNonRMuser(String filePlanAlias) throws Exception
    {
        //create RM Site if doesn't exist
        createRMSiteIfNotExists();
        rmSiteAPI.usingRestWrapper().disconnect();
        restClient.authenticateUser(dataUser.getAdminUser());
        //create a random user
        UserModel nonRMuser = dataUser.createRandomTestUser("testUser");
        //authenticate using the random user
        filePlanComponentApi.usingRestWrapper().authenticateUser(nonRMuser);
        //get the special file plan components
        filePlanComponentApi.getFilePlanComponent(filePlanAlias.toString());
        //check the response status code -FORBIDDEN
        filePlanComponentApi.usingRestWrapper().assertStatusCodeIs(FORBIDDEN);
    }
}
