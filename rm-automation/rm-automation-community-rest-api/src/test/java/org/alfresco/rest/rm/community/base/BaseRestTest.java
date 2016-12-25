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

import static org.alfresco.rest.rm.community.base.TestData.CATEGORY_TITLE;
import static org.alfresco.rest.rm.community.base.TestData.FOLDER_TITLE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createFilePlanComponentModel;
import static org.alfresco.rest.rm.community.utils.RMSiteUtil.createStandardRMSiteModel;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RMRestWrapper;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentModel;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
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

    protected RMSite getRMSiteAPI()
    {
        return getRMSiteAPI(dataUser.getAdminUser());
    }

    protected RMSite getRMSiteAPI(UserModel userModel)
    {
        getRmRestWrapper().authenticateUser(userModel);
        return getRmRestWrapper().withIGCoreAPI().usingRMSite();
    }

    protected FilePlanComponents getFilePlanComponentsAPI()
    {
        return getFilePlanComponentsAPI(dataUser.getAdminUser());
    }

    protected FilePlanComponents getFilePlanComponentsAPI(UserModel userModel)
    {
        getRmRestWrapper().authenticateUser(userModel);
        return getRmRestWrapper().withIGCoreAPI().usingFilePlanComponents();
    }

    /**
     * @return the rmRestWrapper
     */
    protected RMRestWrapper getRmRestWrapper()
    {
        return this.rmRestWrapper;
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
     * FIXME!!!
     *
     * @param httpStatus FIXME!!!
     */
    protected void assertStatusCode(HttpStatus httpStatus)
    {
        getRmRestWrapper().assertStatusCodeIs(httpStatus);
    }

    /**
     * Helper method to create the RM Site via the POST request
     * if the site doesn't exist
     */
    public void createRMSiteIfNotExists() throws Exception
    {
        // Check RM site doesn't exist
        if (!getRMSiteAPI().existsRMSite())
        {
            // Create the RM site
            getRMSiteAPI().createRMSite(createStandardRMSiteModel());

            // Verify the status code
            assertStatusCode(CREATED);
        }
    }

    /**
     * Helper method to create child category
     *
     * @param user user under whose privileges this structure is going to be created
     * @param parentCategoryId The id of the parent category
     * @param categoryName     The name of the category
     * @return The created category
     * @throws Exception on unsuccessful component creation
     */
    public FilePlanComponentModel createCategory(UserModel user, String parentCategoryId, String categoryName) throws Exception
    {
        return createComponent(user, parentCategoryId, categoryName, RECORD_CATEGORY_TYPE, CATEGORY_TITLE);
    }

    /**
     * FIXME!!!
     *
     * @param parentCategoryId FIXME!!!
     * @param categoryName FIXME!!!
     * @return FIXME!!!
     * @throws Exception FIXME!!!
     */
    public FilePlanComponentModel createCategory(String parentCategoryId, String categoryName) throws Exception
    {
        return createCategory(dataUser.getAdminUser(), parentCategoryId, categoryName);
    }

    /**
     * Helper method to create child folder
     *
     * @param user user under whose privileges this structure is going to be created
     * @param parentCategoryId The id of the parent category
     * @param folderName       The name of the category
     * @return The created category
     * @throws Exception on unsuccessful component creation
     */
    public FilePlanComponentModel createFolder(UserModel user, String parentCategoryId, String folderName) throws Exception
    {
        return createComponent(user, parentCategoryId, folderName, RECORD_FOLDER_TYPE, FOLDER_TITLE);
    }

    /**
     * FIXME!!!
     *
     * @param parentCategoryId FIXME!!!
     * @param folderName FIXME!!!
     * @return FIXME!!!
     * @throws Exception FIXME!!!
     */
    public FilePlanComponentModel createFolder(String parentCategoryId, String folderName) throws Exception
    {
        return createFolder(dataUser.getAdminUser(), parentCategoryId, folderName);
    }

    /**
     * Helper method to create child unfiled record folder
     *
     * @param user user under whose privileges this structure is going to be created
     * @param parentId The id of the parent folder
     * @param folderName       The name of the folder
     * @return The created folder
     * @throws Exception on unsuccessful component creation
     */
    public FilePlanComponentModel createUnfiledRecordsFolder(UserModel user, String parentId, String folderName) throws Exception
    {
        return createComponent(user, parentId, folderName, UNFILED_RECORD_FOLDER_TYPE, FOLDER_TITLE);
    }

    /**
     * FIXME!!!
     *
     * @param parentId FIXME!!!
     * @param folderName FIXME!!!
     * @return FIXME!!!
     * @throws Exception FIXME!!!
     */
    public FilePlanComponentModel createUnfiledRecordsFolder(String parentId, String folderName) throws Exception
    {
        return createUnfiledRecordsFolder(dataUser.getAdminUser(), parentId, folderName);
    }

    /**
     * Helper method to create generic child component
     *
     * @param user user under whose privileges this structure is going to be created
     * @param parentComponentId The id of the parent file plan component
     * @param componentName     The name of the file plan component
     * @param componentType     The type of the file plan component
     * @param componentTitle    The title of the file plan component
     * @return The created file plan component
     * @throws Exception
     */
    private FilePlanComponentModel createComponent(UserModel user, String parentComponentId, String componentName, String componentType, String componentTitle) throws Exception
    {
        FilePlanComponentModel filePlanComponentModel = createFilePlanComponentModel(componentName, componentType, componentTitle);
        FilePlanComponentModel filePlanComponent = getFilePlanComponentsAPI(user).createFilePlanComponent(filePlanComponentModel, parentComponentId);
        assertStatusCode(CREATED);

        return filePlanComponent;
    }

    /**
     * Helper method to close folder
     * @param folderId
     * @return
     * @throws Exception
     */
    public FilePlanComponentModel closeFolder(String folderId) throws Exception
    {
        // build file plan component + properties for update request
        FilePlanComponentProperties properties = new FilePlanComponentProperties();
        properties.setIsClosed(true);
        FilePlanComponentModel filePlanComponent = new FilePlanComponentModel();
        filePlanComponent.setProperties(properties);

        FilePlanComponentModel updatedComponent = getFilePlanComponentsAPI().updateFilePlanComponent(filePlanComponent, folderId);
        assertStatusCode(OK);
        return updatedComponent;
    }

    /**
     * Helper method to create a randomly-named <category>/<folder> structure in file plan
     *
     * @param user user under whose privileges this structure is going to be created
     * @param parentId parent container id
     * @return record folder
     * @throws Exception on failed creation
     */
    public FilePlanComponentModel createCategoryFolderInFilePlan(UserModel user) throws Exception
    {
        // create root category
        FilePlanComponentModel recordCategory = createCategory(user, FILE_PLAN_ALIAS, "Category " + getRandomAlphanumeric());

        // and return a folder underneath
        return createFolder(user, recordCategory.getId(), "Folder " + getRandomAlphanumeric());
    }

    /**
     * FIXME!!!
     *
     * @param parentId FIXME!!!
     * @return FIXME!!!
     * @throws Exception FIXME!!!
     */
    public FilePlanComponentModel createCategoryFolderInFilePlan() throws Exception
    {
        return createCategoryFolderInFilePlan(dataUser.getAdminUser());
    }

    /**
     * Helper method to retrieve a file plan component with user's privilege
     * @param user user under whose privileges a component is to be read
     * @param componentId id of the component to read
     * @return {@link FilePlanComponent} for given componentId
     * @throws Exception if user doesn't have sufficient privileges
     */
    public FilePlanComponentModel getFilePlanComponentAsUser(UserModel user, String componentId) throws Exception
    {
        return getFilePlanComponentsAPI(user).getFilePlanComponent(componentId);
    }

    /**
     * FIXME!!!
     *
     * @param componentId FIXME!!!
     * @return FIXME!!!
     * @throws Exception FIXME!!!
     */
    public FilePlanComponentModel getFilePlanComponent(String componentId) throws Exception
    {
        return getFilePlanComponentAsUser(dataUser.getAdminUser(), componentId);
    }
}