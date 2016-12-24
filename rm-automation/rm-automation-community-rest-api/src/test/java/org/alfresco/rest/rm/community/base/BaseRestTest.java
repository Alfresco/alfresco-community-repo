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
package org.alfresco.rest.rm.community.base;

import static com.jayway.restassured.RestAssured.given;

import static org.alfresco.rest.rm.community.base.TestData.CATEGORY_TITLE;
import static org.alfresco.rest.rm.community.base.TestData.FOLDER_TITLE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.site.RMSiteCompliance.STANDARD;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.jglue.fluentjson.JsonBuilderFactory.buildObject;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.google.gson.JsonObject;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import org.alfresco.dataprep.AlfrescoHttpClient;
import org.alfresco.dataprep.AlfrescoHttpClientFactory;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentModel;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.community.model.site.RMSiteModel;
import org.alfresco.rest.rm.community.model.user.UserPermissions;
import org.alfresco.rest.rm.community.requests.FilePlanComponents;
import org.alfresco.rest.rm.community.requests.RMSite;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

/**
 * Base class for all IG REST API Tests
 *
 * @author Kristijan Conkas
 * @author Tuna Aksoy
 * @since 2.6
 */
public class BaseRestTest extends RestTest
{
    @Autowired
    private RMRestWrapper rmRestWrapper;

    @Autowired
    private DataUser dataUser;

    @Autowired
    private AlfrescoHttpClientFactory alfrescoHttpClientFactory;

    private RMSite rmSite;

    private FilePlanComponents filePlanComponents;

    protected RMSite getRMSiteAPI()
    {
        if (rmSite == null)
        {
            rmSite = getRmRestWrapper().withIGCoreAPI().usingRMSite();
        }

        return rmSite;
    }

    protected FilePlanComponents getFilePlanComponentsAPI()
    {
        if (filePlanComponents == null)
        {
            filePlanComponents = getRmRestWrapper().withIGCoreAPI().usingFilePlanComponents();
        }

        return filePlanComponents;
    }

    /**
     * @return the rmRestWrapper
     */
    protected RMRestWrapper getRmRestWrapper()
    {
        return this.rmRestWrapper;
    }

    // Constants
    public static final String RM_ID = "rm";
    public static final String RM_TITLE = "Records Management";
    public static final String RM_DESCRIPTION = "Records Management Site";

    /** Valid root containers where electronic and non-electronic records can be created */
    @DataProvider(name = "validRootContainers")
    public Object[][] getValidRootContainers() throws Exception
    {
        return new Object[][]
        {
            // an arbitrary record folder
            { createCategoryFolderInFilePlan(dataUser.getAdminUser(), FILE_PLAN_ALIAS) },
            // unfiled records root
            { getFilePlanComponentAsUser(dataUser.getAdminUser(), UNFILED_RECORDS_CONTAINER_ALIAS) },
            // an arbitrary unfiled records folder
            { createUnfiledRecordsFolder(UNFILED_RECORDS_CONTAINER_ALIAS, "Unfiled Folder " + getRandomAlphanumeric()) }
        };
    }

    /**
     * @see org.alfresco.rest.RestTest#checkServerHealth()
     */
    @Override
    @BeforeClass (alwaysRun = true)
    public void checkServerHealth() throws Exception
    {
        // Create RM Site if not exist
        createRMSiteIfNotExists();
    }

    /**
     * FIXME!!!
     *
     * @param userModel FIXME!!!
     */
    protected void authenticateUser(UserModel userModel)
    {
        getRmRestWrapper().authenticateUser(userModel);
    }

    protected void assertStatusCodeIs(HttpStatus httpStatus)
    {
        getRmRestWrapper().assertStatusCodeIs(httpStatus);
    }

    protected void disconnect()
    {
        getRmRestWrapper().disconnect();
    }

    /**
     * Helper method to create the RM Site via the POST request
     * if the site doesn't exist
     */
    public void createRMSiteIfNotExists() throws Exception
    {
        // Check RM site doesn't exist
        if (!rmSite.existsRMSite())
        {
            authenticateUser(dataUser.getAdminUser());

            // Create the RM site
            RMSiteModel rmSiteModel =  RMSiteModel.builder().compliance(STANDARD).build();
            rmSiteModel.setTitle(RM_TITLE);
            rmSiteModel.setDescription(RM_DESCRIPTION);
            rmSite.createRMSite(rmSiteModel);

            // Verify the status code
            assertStatusCodeIs(CREATED);
        }
    }

    /**
     * Helper method to create child category
     *
     * @param parentCategoryId The id of the parent category
     * @param categoryName     The name of the category
     * @return The created category
     * @throws Exception on unsuccessful component creation
     */
    public FilePlanComponentModel createCategory(String parentCategoryId, String categoryName) throws Exception
    {
        return createComponent(parentCategoryId, categoryName, RECORD_CATEGORY_TYPE, CATEGORY_TITLE);
    }

    /**
     * Helper method to create child folder
     *
     * @param parentCategoryId The id of the parent category
     * @param folderName       The name of the category
     * @return The created category
     * @throws Exception on unsuccessful component creation
     */
    public FilePlanComponentModel createFolder(String parentCategoryId, String folderName) throws Exception
    {
        return createComponent(parentCategoryId, folderName, RECORD_FOLDER_TYPE, FOLDER_TITLE);
    }

    /**
     * Helper method to create child unfiled record folder
     *
     * @param parentId The id of the parent folder
     * @param folderName       The name of the folder
     * @return The created folder
     * @throws Exception on unsuccessful component creation
     */
    public FilePlanComponentModel createUnfiledRecordsFolder(String parentId, String folderName) throws Exception
    {
        return createComponent(parentId, folderName, UNFILED_RECORD_FOLDER_TYPE, FOLDER_TITLE);
    }

    /**
     * Helper method to create generic child component
     *
     * @param parentComponentId The id of the parent file plan component
     * @param componentName     The name of the file plan component
     * @param componentType     The name of the file plan component
     * @param componentTitle
     * @return The created file plan component
     * @throws Exception
     */
    private FilePlanComponentModel createComponent(String parentComponentId, String componentName, String componentType, String componentTitle) throws Exception
    {
        authenticateUser(dataUser.getAdminUser());

        FilePlanComponentModel filePlanComponent = FilePlanComponentModel.builder()
            .name(componentName)
            .nodeType(componentType)
            .properties(FilePlanComponentProperties.builder()
                            .title(componentTitle)
                            .build())
            .build();

        FilePlanComponentModel fpc = getFilePlanComponentsAPI().createFilePlanComponent(filePlanComponent, parentComponentId);
        assertStatusCodeIs(CREATED);

        return fpc;
    }

    /**
     * Helper method to close folder
     * @param folderId
     * @return
     * @throws Exception
     */
    public FilePlanComponentModel closeFolder(String folderId) throws Exception
    {
        authenticateUser(dataUser.getAdminUser());

        // build fileplan component + properties for update request
        FilePlanComponentProperties properties = new FilePlanComponentProperties();
        properties.setIsClosed(true);
        FilePlanComponentModel filePlanComponent = new FilePlanComponentModel();
        filePlanComponent.setProperties(properties);

        FilePlanComponentModel updatedComponent = getFilePlanComponentsAPI().updateFilePlanComponent(filePlanComponent, folderId);
        assertStatusCodeIs(OK);
        return updatedComponent;
    }

    /**
     * Helper method to create a randomly-named <category>/<folder> structure in fileplan
     * @param user user under whose privileges this structure is going to be created
     * @param parentId parent container id
     * @return record folder
     * @throws Exception on failed creation
     */
    public FilePlanComponentModel createCategoryFolderInFilePlan(UserModel user, String parentId) throws Exception
    {
        authenticateUser(user);

        // create root category
        FilePlanComponentModel recordCategory = createCategory(parentId, "Category " + getRandomAlphanumeric());

        // and return a folder underneath
        return createFolder(recordCategory.getId(), "Folder " + getRandomAlphanumeric());
    }

    /**
     * Helper method to retieve a fileplan component with user's privilege
     * @param user user under whose privileges a component is to be read
     * @param componentId id of the component to read
     * @return {@link FilePlanComponent} for given componentId
     * @throws Exception if user doesn't have sufficient privileges
     */
    public FilePlanComponentModel getFilePlanComponentAsUser(UserModel user, String componentId) throws Exception
    {
        authenticateUser(user);
        return getFilePlanComponentsAPI().getFilePlanComponent(componentId);
    }

    /**
     * Helper method to add permission on a component to user
     * @param component {@link FilePlanComponent} on which permission should be given
     * @param user {@link UserModel} for a user to be granted permission
     * @param permission {@link UserPermissions} to be granted
     */
     // FIXME: As of December 2016 there is no v1-style API for managing RM permissions.
     // Until such APIs have become available, this method is just a proxy to an "old-style"
     // API call.
    public void addUserPermission(FilePlanComponentModel component, UserModel user, String permission)
    {
        // get an "old-style" REST API client
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();

        JsonObject bodyJson = buildObject()
            .addArray("permissions")
                .addObject()
                    .add("authority", user.getUsername())
                    .add("role", permission)
                    .end()
                    .getJson();

        // override v1 baseURI and basePath
        RequestSpecification spec = new RequestSpecBuilder()
            .setBaseUri(client.getApiUrl())
            .setBasePath("/")
            .build();

        // execute an "old-style" API call
        Response response = given()
            .spec(spec)
            .auth().basic(dataUser.getAdminUser().getUsername(), dataUser.getAdminUser().getPassword())
            .contentType(ContentType.JSON)
            .body(bodyJson.toString())
            .pathParam("nodeId", component.getId())
            .log().all()
        .when()
            .post("/node/workspace/SpacesStore/{nodeId}/rmpermissions")
            .prettyPeek()
            .andReturn();
        rmRestWrapper.setStatusCode(Integer.toString(response.getStatusCode()));
    }
}