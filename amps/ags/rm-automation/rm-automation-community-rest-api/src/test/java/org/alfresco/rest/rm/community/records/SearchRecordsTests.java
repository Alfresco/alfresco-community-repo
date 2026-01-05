/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

package org.alfresco.rest.rm.community.records;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.RecordContent;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledContainerAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.rest.v0.SearchAPI;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.UserModel;
import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.json.JSONArray;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.alfresco.rest.rm.community.base.TestData.ELECTRONIC_RECORD_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.*;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createTempFile;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.testng.Assert.assertTrue;
/**
 * Tests the search of records in Records Search page
 * @author Kavit Shah
 */
public class SearchRecordsTests extends BaseRMRestTest {

    private Optional<UserModel> nonRmSiteUser, rm_user_search, rm_manager, rm_admin_search;

    /** The default password used when creating test users. */
    public static final String ROLE_RM_MANAGER = "RecordsManager";
    private final String TEST_PREFIX = generateTestPrefix(SearchRecordsTests.class);
    private final String CATEGORY_ALL = TEST_PREFIX + "everybody's category";
    private final String FOLDER_SEARCH = TEST_PREFIX + "basic search folder";
    private final String FOLDER_ADMIN_ONLY = TEST_PREFIX + "rm admin category";
    private final String CATEGORY_ADMIN_ONLY = TEST_PREFIX + "rm admin category";
    public static final String ROLE_RM_USER = "User";
    public static final String ADMIN = "Administrator";
    private final String ELECTRONIC_RECORD = TEST_PREFIX + " Electronic";
    private final String UNFILED_ELECTRONIC_RECORD = TEST_PREFIX + " Unfiled Electronic";
    private final String NON_ELECTRONIC_RECORD = TEST_PREFIX + " Non-Electronic";
    private final String ADMIN_ELECTRONIC_RECORD = TEST_PREFIX + " admin Electronic";
    public static final String TITLE = "Title";
    public static final String DESCRIPTION = "Description";
    public static final String TEST_CONTENT = "This is some test content";
    private RecordCategory categoryAll, category_Admin_Only;
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    @Autowired
    private SearchAPI searchAPI;
    @Autowired
    private RecordsAPI recordsAPI;

    @BeforeClass (alwaysRun = true)
    public void createRecordsForSearch()
    {
        createRMSiteIfNotExists();
        nonRmSiteUser = Optional.ofNullable(getDataUser().createRandomTestUser());
        // create RM manager and RM user
        createRMManager();
        createRMUser();
        createRMAdmin();
        categoryAll = createCategoryIfDoesNotExist(CATEGORY_ALL);
        createRecordFolderInCategory(FOLDER_SEARCH, categoryAll);

        category_Admin_Only = createCategoryIfDoesNotExist(CATEGORY_ADMIN_ONLY);
        createRecordFolderInCategory(FOLDER_ADMIN_ONLY,category_Admin_Only);

        // upload records in folder in category and in Unfiled Records
        uploadElectronicRecordInContainer(ELECTRONIC_RECORD, FOLDER_SEARCH);
        createNonElectronicRecordInContainer(NON_ELECTRONIC_RECORD, CATEGORY_ALL, FOLDER_SEARCH);
        uploadElectronicRecordInContainer(ADMIN_ELECTRONIC_RECORD, FOLDER_ADMIN_ONLY);

        UnfiledContainerChild electronicRecord = UnfiledContainerChild.builder()
            .name(UNFILED_ELECTRONIC_RECORD)
            .nodeType(CONTENT_TYPE)
            .content(RecordContent.builder().mimeType("text/plain").build())
            .build();
        getRecordsFromUnfiledRecordsContainer(electronicRecord);
    }

    /**
     * Given I have created record category X which contains record folder Y which contains record Z
     * And I have selected to display record category id in the search results
     * When I issue a record search whose results will contain record X
     * Then record X is displayed in the results
     * And the record category X's ID is also displayed in search result meta-data for record X
     */
    @Test(priority = 1)
    public void searchResultsWithRecordCategoryIdentifier() {
        AtomicBoolean electronicRecordFound = new AtomicBoolean(false);
        AtomicReference<JSONArray> items = new AtomicReference<>();
        AtomicBoolean recordCategoryIdentifier = new AtomicBoolean(false);

        STEP("Open the record search page and search by the items created");
        try {
            Utility.sleep(1000, 40000, () -> {
                JSONObject searchResult = (searchAPI
                    .rmSearch(getDataUser().usingAdmin().getAdminUser().getUsername(),
                        getDataUser().usingAdmin().getAdminUser().getPassword(),
                        "rm",
                        "keywords:" + TEST_PREFIX + "*",
                        "records/true,undeclared/true,vital/false,folders/false,categories/false,frozen/false,cutoff/false",
                        "rma:identifier/asc"));
                items.set((JSONArray) searchResult.get("items"));
                assertFalse("Site Consumer not able to find the document.", ((JSONArray)searchResult.get("items")).isEmpty());
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }

        STEP("Check that the records from file plan have the record category identifier displayed");
        List searchList = IntStream.range(0, items.get().length()).mapToObj(i-> items.get().get(i)).collect(Collectors.toList());

        searchList.stream().forEach(x -> {
            Map<String, String> reconstructedUtilMap = Arrays.stream(x.toString().split(","))
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));
            if(reconstructedUtilMap.get("\"name\"").contains(TEST_PREFIX + " Electronic")) {
                electronicRecordFound.set(true);
            }
        });
        assertFalse("The File Name with the Prefix " + TEST_PREFIX + " as Electronic Record was not found.", !electronicRecordFound.get());

        STEP("Change the search filter to return only record folders and record categories");
        try {
            Utility.sleep(1000, 40000, () -> {
                JSONObject searchResult = (searchAPI
                    .rmSearch(getDataUser().usingAdmin().getAdminUser().getUsername(),
                        getDataUser().usingAdmin().getAdminUser().getPassword(),
                        "rm",
                        "keywords:" + TEST_PREFIX + "*",
                        "records/false,undeclared/true,vital/false,folders/true,categories/true,frozen/false,cutoff/false",
                        "rma:identifier/asc"));
                items.set((JSONArray) searchResult.get("items"));
                assertFalse("Site Consumer not able to find the document.", ((JSONArray)searchResult.get("items")).isEmpty());
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }
        STEP("Check that the records folders and categories don't have a record category identifier displayed");
        List recordFolderSearchList = IntStream.range(0, items.get().length()).mapToObj(i-> items.get().get(i)).collect(Collectors.toList());

        recordFolderSearchList.stream().forEach(x -> {
            Map<String, String> reconstructedUtilMap = Arrays.stream(x.toString().split(","))
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));
            if(null != reconstructedUtilMap.get("\"rma_recordCategoryIdentifier\"")) {
                recordCategoryIdentifier.set(true);
            }
        });
        assertFalse("Record Category Identifier displayed for " + TEST_PREFIX + ".", recordCategoryIdentifier.get());
    }

    /**
     * User with RM User role can see the records he has permission over and all in Unfiled Records
     * <p>
     * Given that I am a RM User
     * I can see only the records in File Plan I have permission over and all in Unfiled Records
     */
    @Test (priority = 2)
    public void nonRMUserSearchResults() {
        try {
            Utility.sleep(1000, 40000, () -> {
                List<String> stringList = (searchAPI
                    .searchForDocumentsAsUser(nonRmSiteUser.get().getUsername(),
                        nonRmSiteUser.get().getPassword(),
                        ELECTRONIC_RECORD));
                assertFalse("The file with search term " + ELECTRONIC_RECORD + " was found using RM Not Site User "+ nonRmSiteUser.get().getUsername(),getResult(ELECTRONIC_RECORD,stringList));
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }

        try {
            Utility.sleep(1000, 40000, () -> {
                List<String> stringList = (searchAPI
                    .searchForDocumentsAsUser(nonRmSiteUser.get().getUsername(),
                        nonRmSiteUser.get().getPassword(),
                        UNFILED_ELECTRONIC_RECORD));
                assertFalse("The file with search term " + UNFILED_ELECTRONIC_RECORD + " was not found using RM Not Site User "+ nonRmSiteUser.get().getUsername(),getResult(UNFILED_ELECTRONIC_RECORD,stringList));
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }

        try {
            Utility.sleep(1000, 40000, () -> {
                List<String> stringList = (searchAPI
                    .searchForDocumentsAsUser(nonRmSiteUser.get().getUsername(),
                        nonRmSiteUser.get().getPassword(),
                        NON_ELECTRONIC_RECORD));
                assertFalse("The file with search term " + NON_ELECTRONIC_RECORD + " was not found using RM Not Site User "+ nonRmSiteUser.get().getUsername(),getResult(NON_ELECTRONIC_RECORD,stringList));
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }

        try {
            Utility.sleep(1000, 40000, () -> {
                List<String> stringList = searchAPI
                    .searchForDocumentsAsUser(nonRmSiteUser.get().getUsername(),
                        nonRmSiteUser.get().getPassword(),
                        ADMIN_ELECTRONIC_RECORD);
                assertFalse("The file with search term " + ADMIN_ELECTRONIC_RECORD + " was not found using RM Not Site User "+ nonRmSiteUser.get().getUsername(),getResult(ADMIN_ELECTRONIC_RECORD,stringList));
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }
    }

    /**
     * User with RM User role can see the records he has permission over and all in Unfiled Records
     * <p>
     * Given that I am a RM User
     * I can see only the records in File Plan I have permission over and all in Unfiled Records
     */
    @Test (priority = 3)
    public void rmUserSearchResults() {
        getRestAPIFactory().getRMUserAPI().addUserPermission(categoryAll.getId(), rm_user_search.get(), PERMISSION_READ_RECORDS);
        getRestAPIFactory().getRMUserAPI().addUserPermission(categoryAll.getId(), rm_user_search.get(), PERMISSION_FILE_RECORDS);

        try {
            Utility.sleep(1000, 40000, () -> {
                List<String> stringList = (searchAPI
                    .searchForDocumentsAsUser(rm_user_search.get().getUsername(),
                        rm_user_search.get().getPassword(),
                        ELECTRONIC_RECORD));
                assertTrue(getResult(ELECTRONIC_RECORD,stringList),"The file with search term" + ELECTRONIC_RECORD + " was not found using RM User "+ rm_user_search.get().getUsername());
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }

        try {
            Utility.sleep(1000, 40000, () -> {
                List<String> stringList = (searchAPI
                    .searchForDocumentsAsUser(rm_user_search.get().getUsername(),
                        rm_user_search.get().getPassword(),
                        UNFILED_ELECTRONIC_RECORD));
                assertTrue(getResult(UNFILED_ELECTRONIC_RECORD,stringList),"The file with search term" + UNFILED_ELECTRONIC_RECORD + " was not found using RM User "+ rm_user_search.get().getUsername());
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }

        try {
            Utility.sleep(1000, 40000, () -> {
                List<String> stringList = (searchAPI
                    .searchForDocumentsAsUser(rm_user_search.get().getUsername(),
                        rm_user_search.get().getPassword(),
                        NON_ELECTRONIC_RECORD));
                assertTrue(getResult(NON_ELECTRONIC_RECORD,stringList),"The file with search term" + NON_ELECTRONIC_RECORD + " was not found using RM User "+ rm_user_search.get().getUsername());
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }

        try {
            Utility.sleep(1000, 40000, () -> {
                List<String> stringList = searchAPI
                    .searchForDocumentsAsUser(rm_user_search.get().getUsername(),
                        rm_user_search.get().getPassword(),
                        ADMIN_ELECTRONIC_RECORD);
                assertFalse("The file with search term" + ADMIN_ELECTRONIC_RECORD + " was not found using RM User "+ rm_user_search.get().getUsername(),getResult(ADMIN_ELECTRONIC_RECORD,stringList));
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }
    }

    /**
     * User with RM Manager role can see the records he has permission over and all in Unfiled Records
     * <p>
     * Given that I am a RM Manager
     * I can see only the records in File Plan I have permission over and all in Unfiled Records
     */
    @Test (priority = 4)
    public void rmManagerSearchResults() {
        getRestAPIFactory().getRMUserAPI().addUserPermission(categoryAll.getId(), rm_manager.get(), PERMISSION_READ_RECORDS);

        try {
            Utility.sleep(1000, 40000, () -> {
                List<String> stringList = (searchAPI
                    .searchForDocumentsAsUser(rm_manager.get().getUsername(),
                        rm_manager.get().getPassword(),
                        ELECTRONIC_RECORD));
                assertTrue(getResult(ELECTRONIC_RECORD,stringList),"The file with search term " + ELECTRONIC_RECORD + " was not found using RM manager User "+ rm_manager.get().getUsername());
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }

        try {
            Utility.sleep(1000, 40000, () -> {
                List<String> stringList = (searchAPI
                    .searchForDocumentsAsUser(rm_manager.get().getUsername(),
                        rm_manager.get().getPassword(),
                        UNFILED_ELECTRONIC_RECORD));
                assertTrue(getResult(UNFILED_ELECTRONIC_RECORD,stringList),"The file with search term " + UNFILED_ELECTRONIC_RECORD + " was not found using RM manager User "+ rm_manager.get().getUsername());
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }

        try {
            Utility.sleep(1000, 40000, () -> {
                List<String> stringList = (searchAPI
                    .searchForDocumentsAsUser(rm_manager.get().getUsername(),
                        rm_manager.get().getPassword(),
                        NON_ELECTRONIC_RECORD));
                assertTrue(getResult(NON_ELECTRONIC_RECORD,stringList),"The file with search term " + NON_ELECTRONIC_RECORD + " was not found using RM manager User "+ rm_manager.get().getUsername());
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }

        try {
            Utility.sleep(1000, 40000, () -> {
                List<String> stringList = searchAPI
                    .searchForDocumentsAsUser(rm_manager.get().getUsername(),
                        rm_manager.get().getPassword(),
                        ADMIN_ELECTRONIC_RECORD);
                assertFalse("The file with search term" + ADMIN_ELECTRONIC_RECORD + " was found using RM manager User "+ rm_manager.get().getUsername(),getResult(ADMIN_ELECTRONIC_RECORD,stringList));
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }
    }

    /**
     * User with RM Administrator role can see all the records
     *
     * Given that I am a RM Administrator
     * I can see all the records in File Plan and Unfiled Records through RM Search and Advanced Search
     */
    @Test(priority = 5)
    public void rmAdminSearchResults() {
        try {
            Utility.sleep(1000, 40000, () -> {
                List<String> stringList = (searchAPI
                    .searchForDocumentsAsUser(rm_admin_search.get().getUsername(),
                        rm_admin_search.get().getPassword(),
                        ELECTRONIC_RECORD));
                assertTrue(getResult(ELECTRONIC_RECORD,stringList),"The file with search term " + ELECTRONIC_RECORD + " was not found using RM Admin User "+ rm_admin_search.get().getUsername());
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }

        try {
            Utility.sleep(1000, 40000, () -> {
                List<String> stringList = (searchAPI
                    .searchForDocumentsAsUser(rm_admin_search.get().getUsername(),
                        rm_admin_search.get().getPassword(),
                        UNFILED_ELECTRONIC_RECORD));
                assertTrue(getResult(UNFILED_ELECTRONIC_RECORD,stringList),"The file with search term " + UNFILED_ELECTRONIC_RECORD + " was not found using RM Admin User "+ rm_admin_search.get().getUsername());
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }

        try {
            Utility.sleep(1000, 40000, () -> {
                List<String> stringList = (searchAPI
                    .searchForDocumentsAsUser(rm_admin_search.get().getUsername(),
                        rm_admin_search.get().getPassword(),
                        NON_ELECTRONIC_RECORD));
                assertTrue(getResult(NON_ELECTRONIC_RECORD,stringList),"The file with search term " + NON_ELECTRONIC_RECORD + " was not found using RM Admin User "+ rm_admin_search.get().getUsername());
            });
        }
        catch (InterruptedException e) {
            fail("InterruptedException received while waiting for results.");
        }
    }

    private void createRMManager() {
        // create RM manager
        rm_manager = Optional.ofNullable(getDataUser().createRandomTestUser());
        rmRolesAndActionsAPI.assignRoleToUser(
            getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            rm_manager.get().getUsername(),
            ROLE_RM_MANAGER
        );
    }

    private void createRMUser() {
        // create RM manager
        rm_user_search = Optional.ofNullable(getDataUser().createRandomTestUser());
        rmRolesAndActionsAPI.assignRoleToUser(
            getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            rm_user_search.get().getUsername(),
            ROLE_RM_USER
        );
    }

    private void createRMAdmin() {
        // create RM Admin
        rm_admin_search = Optional.ofNullable(getDataUser().createRandomTestUser());
        rmRolesAndActionsAPI.assignRoleToUser(
            getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            rm_admin_search.get().getUsername(),
            ADMIN
        );
    }

    private RecordCategory createCategoryIfDoesNotExist(String CATEGORY_ALL) {
        return createRootCategory(getDataUser().usingAdmin().getAdminUser(), CATEGORY_ALL);
    }

    private RecordCategoryChild createRecordFolderInCategory(String FOLDER_SEARCH, RecordCategory recordCategory) {
        return createFolder(getDataUser().usingAdmin().getAdminUser(), recordCategory.getId(), FOLDER_SEARCH);
    }

    private void uploadElectronicRecordInContainer(String electronic_record, String folder_search) {
        recordsAPI.uploadElectronicRecord(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            getDefaultElectronicRecordProperties(electronic_record), folder_search, CMISUtil.DocumentType.TEXT_PLAIN);
    }


    protected HttpResponse createNonElectronicRecordInContainer(String name, String categoryName, String folderName) {
        Map<BaseAPI.RMProperty, String> defaultProperties = new HashMap<>();
        defaultProperties.put(BaseAPI.RMProperty.NAME, name);
        defaultProperties.put(BaseAPI.RMProperty.TITLE, TITLE);
        defaultProperties.put(BaseAPI.RMProperty.DESCRIPTION, DESCRIPTION);

        return recordsAPI.createNonElectronicRecord(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), defaultProperties, categoryName, folderName);
    }

    public Map<BaseAPI.RMProperty, String> getDefaultElectronicRecordProperties(String recordName) {
        Map<BaseAPI.RMProperty, String> defaultProperties = new HashMap<>();
        defaultProperties.put(BaseAPI.RMProperty.NAME, recordName);
        defaultProperties.put(BaseAPI.RMProperty.TITLE, TITLE);
        defaultProperties.put(BaseAPI.RMProperty.DESCRIPTION, DESCRIPTION);
        defaultProperties.put(BaseAPI.RMProperty.CONTENT, TEST_CONTENT);
        return defaultProperties;
    }

    @AfterClass(alwaysRun = true)
    public void standardSearchTeardown() {
        // delete categories
        deleteRecordCategory(categoryAll.getId());
        deleteRecordCategory(category_Admin_Only.getId());

        // delete users
        Optional.of(nonRmSiteUser).ifPresent(x -> getDataUser().deleteUser(x.get()));
        Optional.of(rm_user_search).ifPresent(x -> getDataUser().deleteUser(x.get()));
        Optional.of(rm_manager).ifPresent(x -> getDataUser().deleteUser(x.get()));
        Optional.of(rm_admin_search).ifPresent(x -> getDataUser().deleteUser(x.get()));
    }

    private boolean getResult(String partialRecordName, List<String> searchResults) {
        if(null != searchResults) {
            for (String searchResult : searchResults) {
                if (searchResult.startsWith(partialRecordName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Object[][] getRecordsFromUnfiledRecordsContainer(UnfiledContainerChild electronicRecord)
    {
        UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();
        return new String[][] {
            { unfiledContainersAPI.uploadRecord(electronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS,
                createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME)).getId()}
        };
    }
}
