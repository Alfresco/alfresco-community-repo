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
package org.alfresco.rest.rm.community.base;

import static org.alfresco.rest.rm.community.base.TestData.CATEGORY_TITLE;
import static org.alfresco.rest.rm.community.base.TestData.FOLDER_TITLE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAspects.ASPECTS_CLOSED_RECORD;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_TYPE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createFilePlanComponentModel;
import static org.alfresco.rest.rm.community.utils.RMSiteUtil.createStandardRMSiteModel;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestAPIFactory;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.community.requests.igCoreAPI.FilePlanComponentAPI;
import org.alfresco.rest.rm.community.requests.igCoreAPI.RMSiteAPI;
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
public class BaseRMRestTest extends RestTest
{
    @Autowired
    private RestAPIFactory restAPIFactory;

    @Autowired
    private DataUser dataUser;

    /**
     * Gets the REST API Factory
     *
     * @return the restAPIFactory The REST API Factory
     */
    protected RestAPIFactory getRestAPIFactory()
    {
        return this.restAPIFactory;
    }

    /**
     * Gets the data user
     *
     * @return the dataUser The data user
     */
    protected DataUser getDataUser()
    {
        return this.dataUser;
    }

    /**
     * Asserts the given status code
     *
     * @param statusCode The status code to assert
     */
    protected void assertStatusCode(HttpStatus statusCode)
    {
        getRestAPIFactory().getRmRestWrapper().assertStatusCodeIs(statusCode);
    }

    /**
     * Gets the admin user
     *
     * @return The admin user
     */
    protected UserModel getAdminUser()
    {
        return getDataUser().getAdminUser();
    }

    /** Valid root containers where electronic and non-electronic records can be created */
    @DataProvider(name = "validRootContainers")
    public Object[][] getValidRootContainers() throws Exception
    {
        return new Object[][]
        {
            // an arbitrary record folder
            { createCategoryFolderInFilePlan() },
            // unfiled records root
            { getFilePlanComponent(UNFILED_RECORDS_CONTAINER_ALIAS) },
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
     * Helper method to create the RM Site via the POST request
     * if the site doesn't exist
     */
    public void createRMSiteIfNotExists() throws Exception
    {
        RMSiteAPI rmSiteAPI = getRestAPIFactory().getRMSiteAPI();

        // Check RM site doesn't exist
        if (!rmSiteAPI.existsRMSite())
        {
            // Create the RM site
            rmSiteAPI.createRMSite(createStandardRMSiteModel());

            // Verify the status code
            assertStatusCode(CREATED);
        }
    }

    /**
     * Helper method to create child category
     *
     * @param user The user under whose privileges this structure is going to be created
     * @param parentCategoryId The id of the parent category
     * @param categoryName The name of the category
     * @return The created category
     * @throws Exception on unsuccessful component creation
     */
    public FilePlanComponent createCategory(UserModel user, String parentCategoryId, String categoryName) throws Exception
    {
        return createComponent(user, parentCategoryId, categoryName, RECORD_CATEGORY_TYPE, CATEGORY_TITLE);
    }

    /**
     * Helper method to create child category as the admin user
     *
     * @param parentCategoryId The id of the parent category
     * @param categoryName The name of the category
     * @return The created category
     * @throws Exception on unsuccessful component creation
     */
    public FilePlanComponent createCategory(String parentCategoryId, String categoryName) throws Exception
    {
        return createCategory(getAdminUser(), parentCategoryId, categoryName);
    }

    /**
     * Helper method to create child folder
     *
     * @param user The user under whose privileges this structure is going to be created
     * @param parentCategoryId The id of the parent category
     * @param folderName The name of the category
     * @return The created category
     * @throws Exception on unsuccessful component creation
     */
    public FilePlanComponent createFolder(UserModel user, String parentCategoryId, String folderName) throws Exception
    {
        return createComponent(user, parentCategoryId, folderName, RECORD_FOLDER_TYPE, FOLDER_TITLE);
    }

    /**
     * Helper method to create child folder as the admin user
     *
     * @param parentCategoryId The id of the parent category
     * @param folderName The name of the category
     * @return The created category
     * @throws Exception on unsuccessful component creation
     */
    public FilePlanComponent createFolder(String parentCategoryId, String folderName) throws Exception
    {
        return createFolder(getAdminUser(), parentCategoryId, folderName);
    }

    /**
     * Helper method to create child unfiled record folder
     *
     * @param user The user under whose privileges this structure is going to be created
     * @param parentId The id of the parent folder
     * @param folderName The name of the folder
     * @return The created folder
     * @throws Exception on unsuccessful component creation
     */
    public FilePlanComponent createUnfiledRecordsFolder(UserModel user, String parentId, String folderName) throws Exception
    {
        return createComponent(user, parentId, folderName, UNFILED_RECORD_FOLDER_TYPE, FOLDER_TITLE);
    }

    /**
     * Helper method to create child unfiled record folder as the admin user
     *
     * @param parentId The id of the parent folder
     * @param folderName The name of the folder
     * @return The created folder
     * @throws Exception on unsuccessful component creation
     */
    public FilePlanComponent createUnfiledRecordsFolder(String parentId, String folderName) throws Exception
    {
        return createUnfiledRecordsFolder(getAdminUser(), parentId, folderName);
    }

    /**
     * Helper method to create generic child component
     *
     * @param user user under whose privileges this structure is going to be created
     * @param parentComponentId The id of the parent file plan component
     * @param componentName The name of the file plan component
     * @param componentType The type of the file plan component
     * @param componentTitle The title of the file plan component
     * @return The created file plan component
     * @throws Exception
     */
    private FilePlanComponent createComponent(UserModel user, String parentComponentId, String componentName, String componentType, String componentTitle) throws Exception
    {
        FilePlanComponent filePlanComponentModel = createFilePlanComponentModel(componentName, componentType, componentTitle);
        FilePlanComponent filePlanComponent = getRestAPIFactory().getFilePlanComponentsAPI(user).createFilePlanComponent(filePlanComponentModel, parentComponentId);
        assertStatusCode(CREATED);

        return filePlanComponent;
    }

    /**
     * Helper method to close folder
     *
     * @param folderId The id of the folder
     * @return The closed folder
     * @throws Exception
     */
    protected FilePlanComponent closeFolder(String folderId) throws Exception
    {
        // build file plan component + properties for update request
        FilePlanComponentProperties properties = new FilePlanComponentProperties();
        properties.setIsClosed(true);
        FilePlanComponent filePlanComponent = new FilePlanComponent();
        filePlanComponent.setProperties(properties);

        FilePlanComponent updatedComponent = getRestAPIFactory().getFilePlanComponentsAPI().updateFilePlanComponent(filePlanComponent, folderId);
        assertStatusCode(OK);
        return updatedComponent;
    }

    /**
     * Helper method to close record
     *
     * @param recordToClose Record to close
     * @return The closed record
     * @throws Exception
     */
    public FilePlanComponent closeRecord(FilePlanComponent recordToClose) throws Exception
    {
        FilePlanComponentAPI filePlanComponentsAPI = getRestAPIFactory().getFilePlanComponentsAPI();
        List<String> aspects = filePlanComponentsAPI.getFilePlanComponent(recordToClose.getId()).getAspectNames();
        // this operation is only valid for records
        assertTrue(aspects.contains(RECORD_TYPE));
        // a record mustn't be closed
        assertFalse(aspects.contains(ASPECTS_CLOSED_RECORD));
        
        // add closed record aspect
        aspects.add(ASPECTS_CLOSED_RECORD);
        
        FilePlanComponent updatedComponent = filePlanComponentsAPI.updateFilePlanComponent(FilePlanComponent.builder().aspectNames(aspects).build(), 
            recordToClose.getId());
        assertStatusCode(OK);
        return updatedComponent;
    }
    
    /**
     * Helper method to create a randomly-named <category>/<folder> structure in file plan
     *
     * @param user The user under whose privileges this structure is going to be created
     * @param parentId parent container id
     * @return record folder
     * @throws Exception on failed creation
     */
    public FilePlanComponent createCategoryFolderInFilePlan(UserModel user) throws Exception
    {
        // create root category
        FilePlanComponent recordCategory = createCategory(user, FILE_PLAN_ALIAS, "Category " + getRandomAlphanumeric());

        // and return a folder underneath
        return createFolder(user, recordCategory.getId(), "Folder " + getRandomAlphanumeric());
    }

    /**
     * Helper method to create a randomly-named <category>/<folder> structure in file plan as the admin user
     *
     * @param parentId parent container id
     * @return record folder
     * @throws Exception on failed creation
     */
    public FilePlanComponent createCategoryFolderInFilePlan() throws Exception
    {
        return createCategoryFolderInFilePlan(getAdminUser());
    }

    /**
     * Helper method to retrieve a file plan component with user's privilege
     *
     * @param user user under whose privileges a component is to be read
     * @param componentId id of the component to read
     * @return {@link FilePlanComponent} for given componentId
     * @throws Exception if user doesn't have sufficient privileges
     */
    public FilePlanComponent getFilePlanComponentAsUser(UserModel user, String componentId) throws Exception
    {
        return getRestAPIFactory().getFilePlanComponentsAPI(user).getFilePlanComponent(componentId);
    }

    /**
     * Helper method to retrieve a file plan component with user's privilege as the admin user
     *
     * @param componentId id of the component to read
     * @return {@link FilePlanComponent} for given componentId
     * @throws Exception if user doesn't have sufficient privileges
     */
    public FilePlanComponent getFilePlanComponent(String componentId) throws Exception
    {
        return getFilePlanComponentAsUser(getAdminUser(), componentId);
    }

}
