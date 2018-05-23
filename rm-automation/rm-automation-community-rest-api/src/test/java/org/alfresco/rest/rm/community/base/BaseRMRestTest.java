/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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

import static lombok.AccessLevel.PROTECTED;
import static org.alfresco.rest.rm.community.base.TestData.ELECTRONIC_RECORD_NAME;
import static org.alfresco.rest.rm.community.base.TestData.RECORD_CATEGORY_TITLE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAspects.ASPECTS_COMPLETED_RECORD;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createRecordCategoryChildModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createRecordCategoryModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createTempFile;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createUnfiledContainerChildModel;
import static org.alfresco.rest.rm.community.utils.RMSiteUtil.createStandardRMSiteModel;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.dataprep.ContentService;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestAPIFactory;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.rm.community.model.fileplan.FilePlan;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolder;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolderProperties;
import org.alfresco.rest.rm.community.model.site.RMSite;
import org.alfresco.rest.rm.community.model.transfercontainer.TransferContainer;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainer;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.requests.gscore.api.RMSiteAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordCategoryAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI;
import org.alfresco.rest.search.RestRequestQueryModel;
import org.alfresco.rest.search.SearchNodeModel;
import org.alfresco.rest.search.SearchRequest;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.SearchAPI;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import lombok.Getter;

/**
 * Base class for all GS REST API Tests
 *
 * @author Kristijan Conkas
 * @author Tuna Aksoy
 * @since 2.6
 */
public class BaseRMRestTest extends RestTest
{
    @Autowired
    @Getter (value = PROTECTED)
    private RestAPIFactory restAPIFactory;

    @Autowired
    @Getter (value = PROTECTED)
    private DataUser dataUser;

    @Autowired
    @Getter(value = PROTECTED)
    private ContentService contentService;

    @Autowired
    @Getter(value = PROTECTED)
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;

    @Autowired
    @Getter(value = PROTECTED)
    private SearchAPI searchApi;

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
    public String[][] getValidRootContainers() throws Exception
    {
        return new String[][]
        {
            // an arbitrary record folder
            { createCategoryFolderInFilePlan().getId(), RECORD_FOLDER_TYPE},
            // unfiled records root
            { UNFILED_RECORDS_CONTAINER_ALIAS, UNFILED_CONTAINER_TYPE},
            // an arbitrary unfiled records folder
            { createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, "Unfiled Folder " + getRandomAlphanumeric(), UNFILED_RECORD_FOLDER_TYPE).getId(), UNFILED_RECORD_FOLDER_TYPE }
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
     * Helper method to delete the RM site if exists and to create a new one
     */
    public void createRMSite(RMSite rmSiteModel) throws Exception
    {
        RMSiteAPI rmSiteAPI = getRestAPIFactory().getRMSiteAPI();
        if (rmSiteAPI.existsRMSite())
        {
            rmSiteAPI.deleteRMSite();
        }

        rmSiteAPI.createRMSite(rmSiteModel);
        assertStatusCode(CREATED);
    }

    /**
     * Helper method to create root category as the admin user
     *
     * @param categoryName The name of the category
     * @return The created category
     * @throws Exception on unsuccessful component creation
     */
    public RecordCategory createRootCategory(String categoryName) throws Exception
    {
        return createRootCategory(getAdminUser(), categoryName, RECORD_CATEGORY_TITLE);
    }

    /**
     * Helper method to create root category
     *
     * @param userModel The user under whose privileges this structure is going to be created
     * @param categoryName The name of the category
     * @return The created category
     * @throws Exception on unsuccessful component creation
     */
    public RecordCategory createRootCategory(UserModel userModel, String categoryName) throws Exception
    {
        return createRootCategory(userModel, categoryName, RECORD_CATEGORY_TITLE);
    }

    /**
     * Helper method to create root category as the admin user
     *
     * @param categoryName The name of the category
     * @param categoryTitle The title of the category
     * @return The created category
     * @throws Exception on unsuccessful component creation
     */
    public RecordCategory createRootCategory(String categoryName, String categoryTitle) throws Exception
    {
        return createRootCategory(getAdminUser(), categoryName, categoryTitle);
    }

    /**
     * Helper method to create root category
     *
     * @param userModel The user under whose privileges this structure is going to be created
     * @param categoryName The name of the category
     * @param categoryTitle The title of the category
     * @return The created category
     * @throws Exception on unsuccessful component creation
     */
    public RecordCategory createRootCategory(UserModel userModel, String categoryName, String categoryTitle) throws Exception
    {
        RecordCategory recordCategoryModel = createRecordCategoryModel(categoryName, categoryTitle);
        return getRestAPIFactory().getFilePlansAPI(userModel).createRootRecordCategory(recordCategoryModel, FILE_PLAN_ALIAS);
    }

    /**
     * Helper method to create a record category child
     *
     * @param user The user under whose privileges the node is going to be created
     * @param recordCategoryId The id of the record category
     * @param name The name of the record category child
     * @param type The type of the record category child
     * @return The created {@link RecordCategoryChild}
     * @throws Exception {@link RecordCategoryAPI#createRecordCategoryChild(RecordCategoryChild, String)}
     */
    public RecordCategoryChild createRecordCategoryChild(UserModel user, String recordCategoryId, String name, String type) throws Exception
    {
        RecordCategoryChild recordCategoryChildModel = createRecordCategoryChildModel(name, type);
        return getRestAPIFactory().getRecordCategoryAPI(user).createRecordCategoryChild(recordCategoryChildModel, recordCategoryId);
    }

    /**
     * Helper method to create a record category child as the admin user
     *
     * @param recordCategoryId The id of the record category
     * @param name The name of the record category child
     * @param type The type of the record category child
     * @return The created {@link RecordCategoryChild}
     * @throws Exception {@link RecordCategoryAPI#createRecordCategoryChild(RecordCategoryChild, String)}
     */
    public RecordCategoryChild createRecordCategoryChild(String recordCategoryId, String name, String type) throws Exception
    {
        return createRecordCategoryChild(getAdminUser(), recordCategoryId, name, type);
    }

    /**
     * Helper method to create a record category as the admin user
     *
     * @param recordCategoryId The id of the record category
     * @param name The name of the record category child
     * @return The created {@link RecordCategoryChild}
     * @throws Exception {@link RecordCategoryAPI#createRecordCategoryChild(RecordCategoryChild, String)}
     */
    public RecordCategoryChild createRecordCategory(String recordCategoryId, String name) throws Exception
    {
        return createRecordCategoryChild(getAdminUser(), recordCategoryId, name, RECORD_CATEGORY_TYPE);
    }

    /**
     * Helper method to create a record folder as the admin user
     *
     * @param recordCategoryId The id of the record category
     * @param name The name of the record category child
     * @return The created {@link RecordCategoryChild}
     * @throws Exception {@link RecordCategoryAPI#createRecordCategoryChild(RecordCategoryChild, String)}
     */
    public RecordCategoryChild createRecordFolder(String recordCategoryId, String name) throws Exception
    {
        return createRecordCategoryChild(getAdminUser(), recordCategoryId, name, RECORD_FOLDER_TYPE);
    }

    /**
     * Helper method to create record folder
     *
     * @param user The user under whose privileges this structure is going to be created
     * @param recordCategoryId The id of the record category
     * @param name The name of the folder
     * @return The created folder
     * @throws Exception on unsuccessful component creation
     */
    public RecordCategoryChild createFolder(UserModel user, String recordCategoryId, String name) throws Exception
    {
        RecordCategoryChild recordFolderModel = createRecordCategoryChildModel(name, RECORD_FOLDER_TYPE);
        return getRestAPIFactory().getRecordCategoryAPI(user).createRecordCategoryChild(recordFolderModel, recordCategoryId);
    }

    /**
     * Helper method to create record folder as the admin user
     *
     * @param recordCategoryId The id of the record category
     * @param name The name of the folder
     * @return The created folder
     * @throws Exception on unsuccessful component creation
     */
    public RecordCategoryChild createFolder(String recordCategoryId, String name) throws Exception
    {
        return createFolder(getAdminUser(), recordCategoryId, name);
    }

    /**
     * Helper method to create child unfiled record folder
     *
     *@param user The user under whose privileges this structure is going to be created
     * @param parentId The id of the parent folder
     * @param nodeType The child type
     * @return The created folder
     * @throws Exception on unsuccessful component creation
     */
    public UnfiledContainerChild createUnfiledRecordsFolderChild(UserModel user, String parentId, String childName, String nodeType) throws Exception
    {
        UnfiledContainerChild childModel = createUnfiledContainerChildModel(childName, nodeType);
        UnfiledContainerChild child = getRestAPIFactory().getUnfiledRecordFoldersAPI(user).createUnfiledRecordFolderChild(childModel, parentId);
        assertStatusCode(CREATED);

        return child;
    }

    /**
     * Helper method to create child unfiled record folder as the admin user
     *
     * @param parentId The id of the parent folder
     * @param nodeType The child type
     * @return The created folder
     * @throws Exception on unsuccessful component creation
     */
    public UnfiledContainerChild createUnfiledRecordsFolderChild(String parentId, String childName, String nodeType) throws Exception
    {
        return createUnfiledRecordsFolderChild(getAdminUser(), parentId, childName, nodeType);
    }

    /**
     * Helper method to create a child to an unfiled record container
     *
     * @param user The user under whose privileges this structure is going to be created
     * @param parentId The id of the parent container
     * @param childName The name of the child
     * @oaram nodeType the child type
     * @return The created chid
     * @throws Exception on unsuccessful child creation
     */
    public UnfiledContainerChild createUnfiledContainerChild(UserModel user, String parentId, String childName, String nodeType) throws Exception
    {
        UnfiledContainerChild child = null;
        UnfiledContainerChild childModel = createUnfiledContainerChildModel(childName, nodeType);

        if (FilePlanComponentType.CONTENT_TYPE.equals(nodeType))
        {
            child = getRestAPIFactory().getUnfiledContainersAPI(user).uploadRecord(childModel, parentId, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME));
        }
        else
        {
            child = getRestAPIFactory().getUnfiledContainersAPI(user).createUnfiledContainerChild(childModel, parentId);
        }
        assertStatusCode(CREATED);

        return child;
    }

    /**
     * Helper method to create a child to an unfiled record container as the admin user
     *
     * @param parentId The id of the parent container
     * @param childName The name of the child
     * @oaram nodeType the child type
     * @return The created chid
     * @throws Exception on unsuccessful child creation
     */
    public UnfiledContainerChild createUnfiledContainerChild(String parentId, String childName, String nodeType) throws Exception
    {
        return createUnfiledContainerChild(getAdminUser(), parentId, childName, nodeType);
    }

    /**
     * Helper method to close folder
     *
     * @param folderId The id of the folder
     * @return The closed folder
     * @throws Exception
     */
    protected RecordFolder closeFolder(String folderId) throws Exception
    {
        RecordFolder recordFolderModel = RecordFolder.builder()
                                            .properties(RecordFolderProperties.builder()
                                                    .isClosed(true)
                                                    .build())
                                            .build();
        RecordFolder updateRecordFolder = getRestAPIFactory().getRecordFolderAPI().updateRecordFolder(recordFolderModel, folderId);
        assertStatusCode(OK);

        return updateRecordFolder;
    }

    /**
     * Helper method to complete record
     *
     * @param recordId The id of the record to complete
     * @return The completed record
     * @throws Exception
     */
    public Record completeRecord(String recordId) throws Exception
    {
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        List<String> aspects = recordsAPI.getRecord(recordId).getAspectNames();

        // this operation is only valid for records
        assertTrue(aspects.contains(RECORD_TYPE));
        // a record mustn't be completed
        assertFalse(aspects.contains(ASPECTS_COMPLETED_RECORD));
        // add completed record aspect
        aspects.add(ASPECTS_COMPLETED_RECORD);

        Record updateRecord = recordsAPI.updateRecord(Record.builder().aspectNames(aspects).build(), recordId);
        assertStatusCode(OK);

        return updateRecord;
    }

    /**
     * Helper method to create a randomly-named <category>/<folder> structure in file plan
     *
     * @param user The user under whose privileges this structure is going to be created
     * @return {@link RecordCategoryChild} which represents the record folder
     * @throws Exception on failed creation
     */
    public RecordCategoryChild createCategoryFolderInFilePlan(UserModel user) throws Exception
    {
        // create root category
        RecordCategory recordCategory = createRootCategory(user, "Category " + getRandomAlphanumeric());

        // and return a folder underneath
        return createFolder(user, recordCategory.getId(), "Folder " + getRandomAlphanumeric());
    }

    /**
     * Helper method to create a randomly-named <category>/<folder> structure in file plan as the admin user
     *
     * @return {@link RecordCategoryChild} which represents the record folder
     * @throws Exception on failed creation
     */
    public RecordCategoryChild createCategoryFolderInFilePlan() throws Exception
    {
        return createCategoryFolderInFilePlan(getAdminUser());
    }

    public UnfiledContainer getUnfiledContainerAsUser(UserModel user, String componentId) throws Exception
    {
        return getRestAPIFactory().getUnfiledContainersAPI(user).getUnfiledContainer(componentId);
    }

    public UnfiledContainer getUnfiledContainer(String componentId) throws Exception
    {
        return getUnfiledContainerAsUser(getAdminUser(), componentId);
    }

    public TransferContainer getTransferContainerAsUser(UserModel user, String componentId) throws Exception
    {
        return getRestAPIFactory().getTransferContainerAPI(user).getTransferContainer(componentId);
    }

    public TransferContainer getTransferContainer(String componentId) throws Exception
    {
        return getTransferContainerAsUser(getAdminUser(), componentId);
    }

    public FilePlan getFilePlanAsUser(UserModel user, String componentId) throws Exception
    {
        return getRestAPIFactory().getFilePlansAPI(user).getFilePlan(componentId);
    }

    public FilePlan getFilePlan(String componentId) throws Exception
    {
        return getFilePlanAsUser(getAdminUser(), componentId);
    }

    /**
     * Recursively delete a folder
     *
     * @param siteModel
     * @param folder
     */
    public void deleteFolder(SiteModel siteModel, FolderModel folder)
    {
        contentService.deleteTree(getAdminUser().getUsername(), getAdminUser().getPassword(), siteModel.getId(),
                    folder.getName());
    }

    /**
     * Create an electronic record
     *
     * @param parentId the id of the parent
     * @param name the name of the record
     * @return the created record
     * @throws Exception
     */
    public Record createElectronicRecord(String parentId, String name) throws Exception
    {
       return createElectronicRecord(parentId, name ,null);
    }


    /**
     * Create an electronic record
     *
     * @param parentId the id of the parent
     * @param name     the name of the record
     * @return the created record
     * @throws Exception
     */
    public Record createElectronicRecord(String parentId, String name, UserModel user) throws Exception
    {
        RecordFolderAPI recordFolderAPI = restAPIFactory.getRecordFolderAPI(user);
        Record recordModel = Record.builder().name(name).nodeType(CONTENT_TYPE).build();
        return recordFolderAPI.createRecord(recordModel, parentId);
    }

    /**
     * Create a non-electronic record
     *
     * @param parentId the id of the parent
     * @param name     the name of the record
     * @return the created record
     * @throws Exception
     */
    public Record createNonElectronicRecord(String parentId, String name) throws Exception
    {
        return createNonElectronicRecord(parentId, name, null);
    }

    /**
     * Create a non-electronic record
     *
     * @param parentId the id of the parent
     * @param name     the name of the record
     * @param user the user who creates the  non-electronic record
     * @return the created record
     * @throws Exception
     */
    public Record createNonElectronicRecord(String parentId, String name, UserModel user) throws Exception
    {
        RecordFolderAPI recordFolderAPI = restAPIFactory.getRecordFolderAPI(user);
        Record recordModel = Record.builder().name(name).nodeType(NON_ELECTRONIC_RECORD_TYPE).build();
        return recordFolderAPI.createRecord(recordModel, parentId);
    }

    /**
     * Delete a record folder
     *
     * @param recordFolderId the id of the record folder to delete
     */
    public void deleteRecordFolder(String recordFolderId)
    {
        RecordFolderAPI recordFolderAPI = restAPIFactory.getRecordFolderAPI();
        recordFolderAPI.deleteRecordFolder(recordFolderId);
    }
    
    /**
     * Delete a record 
     *
     * @param recordId the id of the record to delete
     */
    public void deleteRecord(String recordId)
    {
        RecordsAPI recordsAPI = restAPIFactory.getRecordsAPI();
        recordsAPI.deleteRecord(recordId);
    }

    /**
     * Delete a record category
     *
     * @param recordCategoryId the id of the record category to delete
     */
    public void deleteRecordCategory(String recordCategoryId)
    {
        RecordCategoryAPI recordCategoryAPI = restAPIFactory.getRecordCategoryAPI();
        recordCategoryAPI.deleteRecordCategory(recordCategoryId);
    }

    /**
     * Assign filling permission on a record category and give the user RM_USER role
     *
     * @param user the user to assign the permission to
     * @param categoryId the id of the category to assign permissions for
     * @throws Exception
     */
    public void assignFillingPermissionsOnCategory(UserModel user, String categoryId,
                                                   String userPermission, String userRole) throws Exception
    {
        getRestAPIFactory().getRMUserAPI().addUserPermission(categoryId, user, userPermission);
        rmRolesAndActionsAPI.assignRoleToUser(getAdminUser().getUsername(),
                    getAdminUser().getPassword(), user.getUsername(), userRole);
    }

    /**
     * Returns search results for the given search term
     *
     * @param user
     * @param term
     * @return
     * @throws Exception
     */
    public List<String> searchForContentAsUser(UserModel user, String term) throws Exception
    {
        getRestAPIFactory().getRmRestWrapper().authenticateUser(user);
        RestRequestQueryModel queryReq = new RestRequestQueryModel();
        SearchRequest query = new SearchRequest(queryReq);
        queryReq.setQuery("cm:name:*" + term + "*");

        List<String> names = new ArrayList<>();
        // wait for solr indexing
        int counter = 0;
        int waitInMilliSeconds = 6000;
        while (counter < 3)
        {
            synchronized (this)
            {
                try
                {
                    this.wait(waitInMilliSeconds);
                } catch (InterruptedException e)
                {
                }
            }

            List<SearchNodeModel> searchResults = getRestAPIFactory().getRmRestWrapper().withSearchAPI().search(query)
                                                                     .getEntries();
            if ((searchResults != null && !searchResults.isEmpty()))
            {
                searchResults.forEach(childNode ->
                {
                    names.add(childNode.onModel().getName());
                });
                break;
            }
            else
            {
                counter++;
            }
            // double wait time to not overdo solr search
            waitInMilliSeconds = (waitInMilliSeconds * 2);
        }
        return names;
    }

    /**
     * Returns records search results for the given search term
     *
     * @param user
     * @param term
     * @param sortby
     * @param expectedResults
     * @return
     */
    public List<String> searchForRMContentAsUser(UserModel user, String term, String sortby, List<String> expectedResults)
    {
        List<String> results = new ArrayList<>();
        // wait for solr indexing
        int counter = 0;
        int waitInMilliSeconds = 6000;
        while (counter < 3)
        {
            synchronized (this)
            {
                try
                {
                    this.wait(waitInMilliSeconds);
                } catch (InterruptedException e)
                {
                }
            }
            
            results = searchApi.searchForRmContentAsUser(user.getUsername(), user.getPassword(), term, sortby);
            if (!results.isEmpty() && results.containsAll(expectedResults))
            {
                break;
            }
            else
            {
                counter++;
            }
            // double wait time to not overdo solr search
            waitInMilliSeconds = (waitInMilliSeconds * 2);
        }
        return results;
    }

    /**
     * Helper method to return site document library content model
     *
     * @return ContentModel
     * @throws Exception
     */
    public ContentModel getDocumentLibrary(UserModel usermodel, SiteModel testSite) throws Exception
    {
        ContentModel siteModel = new ContentModel();
        siteModel.setNodeRef(testSite.getGuid());

        restClient.authenticateUser(usermodel);

        List<RestNodeModel> nodes = restClient.withCoreAPI().usingNode(siteModel)
                                              .listChildren().getEntries().stream().collect(Collectors.toList());
        ContentModel documentLibrary = new ContentModel();
        documentLibrary.setName(nodes.get(0).onModel().getName());
        documentLibrary.setNodeRef(nodes.get(0).onModel().getId());
        return documentLibrary;
    }

}
